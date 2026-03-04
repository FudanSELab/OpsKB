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
    p = argparse.ArgumentParser(description="Import PromCopilot entity CSVs into a single ES index.")
    p.add_argument(
        "--entity-dir",
        default="/Users/ethanyuan/works/kb_tools/PromCopilot/data/import/entity",
        help="PromCopilot entity CSV directory",
    )
    p.add_argument("--url", default="http://localhost:9200", help="Elasticsearch base URL")
    p.add_argument("--index", default="kb_nodes_2", help="Target ES index for PromCopilot KB")
    p.add_argument("--recreate-index", action="store_true", help="Delete and recreate index before import")
    p.add_argument("--disable-proxy", action="store_true", help="Disable system proxy (recommended for localhost)")
    return p.parse_args()


def disable_proxy_if_needed(base_url, force=False):
    host = (urlparse(base_url).hostname or "").lower()
    if force or host in {"localhost", "127.0.0.1"}:
        urllib.request.install_opener(urllib.request.build_opener(urllib.request.ProxyHandler({})))


def request_json(method, url, payload=None):
    data = None if payload is None else json.dumps(payload).encode("utf-8")
    req = urllib.request.Request(url, data=data, method=method)
    req.add_header("Content-Type", "application/json")
    with urllib.request.urlopen(req) as resp:
        return resp.getcode(), resp.read().decode("utf-8")


def request_raw(method, url, body_bytes, content_type):
    req = urllib.request.Request(url, data=body_bytes, method=method)
    req.add_header("Content-Type", content_type)
    with urllib.request.urlopen(req) as resp:
        return resp.getcode(), resp.read().decode("utf-8")


def recreate_index(base_url, index):
    mapping = {
        "mappings": {
            "properties": {
                "name": {"type": "text", "fields": {"keyword": {"type": "keyword"}}},
                "labels": {"type": "keyword"},
                "properties": {"type": "object", "enabled": True},
            }
        }
    }
    try:
        request_json("DELETE", f"{base_url}/{index}")
    except urllib.error.HTTPError as e:
        if e.code != 404:
            raise
    request_json("PUT", f"{base_url}/{index}", mapping)


def read_entity_docs(entity_dir: Path):
    docs = []
    seen_ids = set()
    for csv_path in sorted(entity_dir.glob("*.csv")):
        entity_type = csv_path.stem
        with open(csv_path, "r", encoding="utf-8") as f:
            reader = csv.DictReader(f)
            for i, row in enumerate(reader, start=1):
                clean = {k: (v if v is not None else "") for k, v in row.items()}
                name = (clean.get("name") or "").strip()
                if not name:
                    first_key = next(iter(clean.keys()), "row")
                    name = str(clean.get(first_key, f"{entity_type}-{i}"))
                clean["entity_type"] = entity_type
                clean["source_dataset"] = "PromCopilot"
                base_id = f"{entity_type}:{name}"
                doc_id = base_id
                suffix = 2
                while doc_id in seen_ids:
                    doc_id = f"{base_id}#{suffix}"
                    suffix += 1
                seen_ids.add(doc_id)
                docs.append(
                    {
                        "_id": doc_id,
                        "name": name,
                        "labels": ["entity", entity_type, "PromCopilot"],
                        "properties": clean,
                    }
                )
    return docs


def bulk_index(base_url, index, docs):
    lines = []
    for doc in docs:
        lines.append(json.dumps({"index": {"_index": index, "_id": doc["_id"]}}, ensure_ascii=False))
        lines.append(json.dumps({"name": doc["name"], "labels": doc["labels"], "properties": doc["properties"]}, ensure_ascii=False))
    body = ("\n".join(lines) + "\n").encode("utf-8")
    return request_raw("POST", f"{base_url}/_bulk?refresh=true", body, "application/x-ndjson")


def main():
    args = parse_args()
    disable_proxy_if_needed(args.url, args.disable_proxy)
    entity_dir = Path(args.entity_dir).expanduser().resolve()
    if not entity_dir.exists():
        print(f"Entity dir not found: {entity_dir}", file=sys.stderr)
        return 1
    try:
        if args.recreate_index:
            recreate_index(args.url.rstrip("/"), args.index)
        docs = read_entity_docs(entity_dir)
        if not docs:
            print("No docs found.")
            return 0
        status, body = bulk_index(args.url.rstrip("/"), args.index, docs)
        resp = json.loads(body)
        if resp.get("errors"):
            print(json.dumps(resp, ensure_ascii=False, indent=2))
            print("Bulk import completed with errors.", file=sys.stderr)
            return 2
        print(f"Indexed {len(docs)} PromCopilot docs into {args.index} (HTTP {status}).")
        return 0
    except urllib.error.HTTPError as e:
        print(f"HTTPError {e.code}: {e.read().decode('utf-8', errors='ignore')}", file=sys.stderr)
        return 1
    except Exception as e:
        print(f"Failed: {e}", file=sys.stderr)
        return 1


if __name__ == "__main__":
    sys.exit(main())
