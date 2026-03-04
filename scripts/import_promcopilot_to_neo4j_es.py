#!/usr/bin/env python3
import argparse
import csv
import json
import sys
import urllib.error
import urllib.request
from pathlib import Path
from urllib.parse import urlparse


def parse_args():
    p = argparse.ArgumentParser(
        description="Import PromCopilot entities+relations into Neo4j and Elasticsearch"
    )
    p.add_argument(
        "--base-dir",
        default="/Users/ethanyuan/works/kb_tools/PromCopilot/data/import",
        help="PromCopilot import base dir (contains entity/ and relationship/)",
    )

    p.add_argument("--neo4j-http-url", default="http://localhost:7474", help="Neo4j HTTP base URL")
    p.add_argument("--neo4j-user", default="neo4j", help="Neo4j username")
    p.add_argument("--neo4j-password", default="123456", help="Neo4j password")
    p.add_argument("--skip-neo4j", action="store_true", help="Skip Neo4j import")

    p.add_argument("--es-url", default="http://localhost:9200", help="Elasticsearch base URL")
    p.add_argument("--es-node-index", default="kb_nodes_2", help="ES index for node docs")
    p.add_argument("--es-edge-index", default="kb_edges_2", help="ES index for relation docs")
    p.add_argument("--recreate-es-index", action="store_true", help="Delete and recreate ES indices")
    p.add_argument("--skip-es", action="store_true", help="Skip Elasticsearch import")

    p.add_argument("--disable-proxy", action="store_true", help="Disable system proxy for localhost")
    return p.parse_args()


def disable_proxy_if_needed(urls, force=False):
    localhost_hosts = {"localhost", "127.0.0.1"}
    should_disable = force
    for u in urls:
        host = (urlparse(u).hostname or "").lower()
        if host in localhost_hosts:
            should_disable = True
            break
    if should_disable:
        urllib.request.install_opener(urllib.request.build_opener(urllib.request.ProxyHandler({})))


def request_json(method, url, payload=None, auth=None):
    data = None if payload is None else json.dumps(payload).encode("utf-8")
    req = urllib.request.Request(url, data=data, method=method)
    req.add_header("Content-Type", "application/json")
    if auth:
        req.add_header("Authorization", f"Basic {auth}")
    with urllib.request.urlopen(req) as resp:
        return resp.getcode(), resp.read().decode("utf-8")


def request_raw(method, url, body_bytes, content_type):
    req = urllib.request.Request(url, data=body_bytes, method=method)
    req.add_header("Content-Type", content_type)
    with urllib.request.urlopen(req) as resp:
        return resp.getcode(), resp.read().decode("utf-8")


def b64_basic_auth(user, password):
    import base64

    token = f"{user}:{password}".encode("utf-8")
    return base64.b64encode(token).decode("ascii")


def read_entities(entity_dir: Path):
    entities = {}
    for csv_path in sorted(entity_dir.glob("*.csv")):
        entity_type = csv_path.stem
        rows = []
        with open(csv_path, "r", encoding="utf-8") as f:
            reader = csv.DictReader(f)
            for row in reader:
                clean = {k: (v if v is not None else "") for k, v in row.items()}
                rows.append(clean)
        entities[entity_type] = rows
    return entities


def relation_meta_from_filename(filename):
    # format: src-dst-relType-srcCol-dstCol.csv
    parts = filename[:-4].split("-")
    if len(parts) != 5:
        raise ValueError(f"Unexpected relation filename format: {filename}")
    return {
        "src_type": parts[0],
        "dst_type": parts[1],
        "rel_type": parts[2].upper(),
        "src_col": parts[3],
        "dst_col": parts[4],
    }


def read_relations(relationship_dir: Path):
    all_relations = []
    for csv_path in sorted(relationship_dir.glob("*.csv")):
        meta = relation_meta_from_filename(csv_path.name)
        with open(csv_path, "r", encoding="utf-8") as f:
            reader = csv.DictReader(f)
            for row in reader:
                clean = {k: (v if v is not None else "") for k, v in row.items()}
                src_key_col = "src" if meta["src_type"] == meta["dst_type"] else meta["src_type"]
                dst_key_col = "dst" if meta["src_type"] == meta["dst_type"] else meta["dst_type"]
                all_relations.append(
                    {
                        **meta,
                        "src_value": clean.get(src_key_col, ""),
                        "dst_value": clean.get(dst_key_col, ""),
                        "source_file": csv_path.name,
                    }
                )
    return all_relations


def neo4j_run_statements(neo4j_http_url, auth_b64, statements):
    base = neo4j_http_url.rstrip("/")
    if base.endswith("/db/data/transaction/commit") or base.endswith("/db/neo4j/tx/commit"):
        url = base
    else:
        # Neo4j 5 default transactional endpoint.
        url = f"{base}/db/neo4j/tx/commit"
    payload = {"statements": statements}
    _, body = request_json("POST", url, payload, auth=auth_b64)
    resp = json.loads(body)
    errors = resp.get("errors", [])
    if errors:
        raise RuntimeError(json.dumps(errors, ensure_ascii=False))
    return resp


def import_to_neo4j(args, entities, relations):
    auth_b64 = b64_basic_auth(args.neo4j_user, args.neo4j_password)

    # nodes
    node_count = 0
    for entity_type, rows in entities.items():
        key_field = "id" if entity_type == "container" else "name"
        statements = []
        for row in rows:
            key_val = (row.get(key_field) or "").strip()
            if not key_val:
                continue
            props = dict(row)
            props["entity_type"] = entity_type
            props["kb_id"] = "promcopilot"
            if "name" not in props or not str(props.get("name", "")).strip():
                props["name"] = key_val
            statements.append(
                {
                    "statement": f"MERGE (n:entity:`{entity_type}` {{{key_field}: $key}}) "
                    "SET n += $props",
                    "parameters": {"key": key_val, "props": props},
                }
            )
            node_count += 1

            if len(statements) >= 500:
                neo4j_run_statements(args.neo4j_http_url, auth_b64, statements)
                statements = []

        if statements:
            neo4j_run_statements(args.neo4j_http_url, auth_b64, statements)

    # edges
    edge_count = 0
    statements = []
    for rel in relations:
        if not rel["src_value"] or not rel["dst_value"]:
            continue
        statements.append(
            {
                "statement": (
                    f"MATCH (s:`{rel['src_type']}` {{{rel['src_col']}: $src_val}}) "
                    f"MATCH (d:`{rel['dst_type']}` {{{rel['dst_col']}: $dst_val}}) "
                    f"MERGE (s)-[r:{rel['rel_type']}]->(d) "
                    "SET r.kb_id = 'promcopilot', r.source_file = $source_file"
                ),
                "parameters": {
                    "src_val": rel["src_value"],
                    "dst_val": rel["dst_value"],
                    "source_file": rel["source_file"],
                },
            }
        )
        edge_count += 1
        if len(statements) >= 500:
            neo4j_run_statements(args.neo4j_http_url, auth_b64, statements)
            statements = []

    if statements:
        neo4j_run_statements(args.neo4j_http_url, auth_b64, statements)

    return node_count, edge_count


def recreate_es_indices(base_url, node_index, edge_index):
    node_mapping = {
        "mappings": {
            "properties": {
                "name": {"type": "text", "fields": {"keyword": {"type": "keyword"}}},
                "labels": {"type": "keyword"},
                "properties": {"type": "object", "enabled": True},
            }
        }
    }
    edge_mapping = {
        "mappings": {
            "properties": {
                "relation_type": {"type": "keyword"},
                "source": {"type": "keyword"},
                "target": {"type": "keyword"},
                "source_name": {"type": "text", "fields": {"keyword": {"type": "keyword"}}},
                "target_name": {"type": "text", "fields": {"keyword": {"type": "keyword"}}},
                "kb_id": {"type": "keyword"},
                "source_file": {"type": "keyword"},
            }
        }
    }

    for index, mapping in ((node_index, node_mapping), (edge_index, edge_mapping)):
        try:
            request_json("DELETE", f"{base_url}/{index}")
        except urllib.error.HTTPError as e:
            if e.code != 404:
                raise
        request_json("PUT", f"{base_url}/{index}", mapping)


def bulk_index(base_url, index, docs):
    lines = []
    for doc in docs:
        lines.append(json.dumps({"index": {"_index": index, "_id": doc["_id"]}}, ensure_ascii=False))
        lines.append(json.dumps(doc["_source"], ensure_ascii=False))
    body = ("\n".join(lines) + "\n").encode("utf-8")
    _, response = request_raw("POST", f"{base_url}/_bulk?refresh=true", body, "application/x-ndjson")
    payload = json.loads(response)
    if payload.get("errors"):
        raise RuntimeError("ES bulk import has errors")


def import_to_es(args, entities, relations):
    node_docs = []
    seen = set()
    for entity_type, rows in entities.items():
        for i, row in enumerate(rows, start=1):
            key = row.get("id") if entity_type == "container" else row.get("name")
            key = (key or "").strip() or f"{entity_type}-{i}"
            doc_id = f"{entity_type}:{key}"
            suffix = 2
            while doc_id in seen:
                doc_id = f"{entity_type}:{key}#{suffix}"
                suffix += 1
            seen.add(doc_id)

            props = dict(row)
            props["entity_type"] = entity_type
            props["kb_id"] = "promcopilot"
            if "name" not in props or not str(props.get("name", "")).strip():
                props["name"] = key

            node_docs.append(
                {
                    "_id": doc_id,
                    "_source": {
                        "name": props["name"],
                        "labels": ["entity", entity_type, "promcopilot"],
                        "properties": props,
                    },
                }
            )

    edge_docs = []
    for idx, rel in enumerate(relations, start=1):
        if not rel["src_value"] or not rel["dst_value"]:
            continue
        doc_id = f"{rel['src_type']}:{rel['src_value']}->{rel['rel_type']}->{rel['dst_type']}:{rel['dst_value']}#{idx}"
        edge_docs.append(
            {
                "_id": doc_id,
                "_source": {
                    "relation_type": rel["rel_type"],
                    "source": f"{rel['src_type']}:{rel['src_value']}",
                    "target": f"{rel['dst_type']}:{rel['dst_value']}",
                    "source_name": rel["src_value"],
                    "target_name": rel["dst_value"],
                    "kb_id": "promcopilot",
                    "source_file": rel["source_file"],
                },
            }
        )

    if node_docs:
        bulk_index(args.es_url.rstrip("/"), args.es_node_index, node_docs)
    if edge_docs:
        bulk_index(args.es_url.rstrip("/"), args.es_edge_index, edge_docs)

    return len(node_docs), len(edge_docs)


def main():
    args = parse_args()
    base_dir = Path(args.base_dir).expanduser().resolve()
    entity_dir = base_dir / "entity"
    relationship_dir = base_dir / "relationship"

    if not entity_dir.exists() or not relationship_dir.exists():
        print(f"Invalid PromCopilot import dir: {base_dir}", file=sys.stderr)
        return 1

    disable_proxy_if_needed([args.neo4j_http_url, args.es_url], args.disable_proxy)

    entities = read_entities(entity_dir)
    relations = read_relations(relationship_dir)

    try:
        if not args.skip_neo4j:
            n_count, r_count = import_to_neo4j(args, entities, relations)
            print(f"Neo4j import done: nodes={n_count}, relations={r_count}")

        if not args.skip_es:
            if args.recreate_es_index:
                recreate_es_indices(args.es_url.rstrip("/"), args.es_node_index, args.es_edge_index)
            node_docs, edge_docs = import_to_es(args, entities, relations)
            print(f"ES import done: node_docs={node_docs}, edge_docs={edge_docs}, node_index={args.es_node_index}, edge_index={args.es_edge_index}")

        return 0
    except urllib.error.HTTPError as e:
        detail = e.read().decode("utf-8", errors="ignore")
        print(f"HTTPError {e.code}: {detail}", file=sys.stderr)
        return 1
    except Exception as e:
        print(f"Failed: {e}", file=sys.stderr)
        return 1


if __name__ == "__main__":
    sys.exit(main())
