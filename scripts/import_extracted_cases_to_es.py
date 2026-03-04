#!/usr/bin/env python3
import argparse
import base64
import json
import ssl
import sys
from urllib.parse import urlparse
import urllib.error
import urllib.request
from pathlib import Path


CATEGORY_MAP = {
    "symptoms": ("entity", "Symptom"),
    "check_steps": ("entity", "CheckStep"),
    "observations": ("entity", "Observation"),
    "root_causes": ("entity", "RootCause"),
    "recoveries": ("entity", "Recovery"),
}


def parse_args():
    p = argparse.ArgumentParser(
        description="Transform extracted_cases.json into ES documents and bulk index them."
    )
    p.add_argument(
        "--input",
        default="/Users/ethanyuan/works/kb_tools/extracted_cases.json",
        help="Path to extracted_cases.json",
    )
    p.add_argument("--url", default="https://localhost:9200", help="Elasticsearch base URL")
    p.add_argument("--index", default="kb_nodes_1", help="Target ES index")
    p.add_argument("--user", default="elastic", help="ES username")
    p.add_argument("--password", default="", help="ES password (omit when security is disabled)")
    p.add_argument(
        "--disable-proxy",
        action="store_true",
        help="Disable system proxy for this script (recommended for localhost imports)",
    )
    p.add_argument(
        "--insecure",
        action="store_true",
        help="Disable TLS certificate verification (useful for local ES 8)",
    )
    p.add_argument(
        "--ca-cert",
        default="",
        help="Path to http_ca.crt (optional, preferred over --insecure)",
    )
    p.add_argument(
        "--recreate-index",
        action="store_true",
        help="Delete and recreate the target index before import",
    )
    return p.parse_args()


def make_ssl_context(args):
    if args.url.startswith("https://"):
        if args.ca_cert:
            return ssl.create_default_context(cafile=args.ca_cert)
        if args.insecure:
            ctx = ssl.create_default_context()
            ctx.check_hostname = False
            ctx.verify_mode = ssl.CERT_NONE
            return ctx
        return ssl.create_default_context()
    return None


def request_json(method, url, payload, headers, ssl_context):
    data = None if payload is None else json.dumps(payload).encode("utf-8")
    req = urllib.request.Request(url, data=data, method=method)
    for k, v in headers.items():
        req.add_header(k, v)
    with urllib.request.urlopen(req, context=ssl_context) as resp:
        body = resp.read().decode("utf-8")
        return resp.getcode(), body


def request_raw(method, url, body_bytes, headers, ssl_context):
    req = urllib.request.Request(url, data=body_bytes, method=method)
    for k, v in headers.items():
        req.add_header(k, v)
    with urllib.request.urlopen(req, context=ssl_context) as resp:
        body = resp.read().decode("utf-8")
        return resp.getcode(), body


def auth_headers(user, password):
    if not password:
        return {}
    token = base64.b64encode(f"{user}:{password}".encode("utf-8")).decode("ascii")
    return {"Authorization": f"Basic {token}"}


def maybe_disable_proxy(base_url, disable_proxy_flag):
    host = (urlparse(base_url).hostname or "").lower()
    is_local = host in {"localhost", "127.0.0.1"}
    if disable_proxy_flag or is_local:
        urllib.request.install_opener(urllib.request.build_opener(urllib.request.ProxyHandler({})))


def load_source(path):
    with open(path, "r", encoding="utf-8") as f:
        return json.load(f)


def normalize_docs(payload):
    docs = []
    nodes = payload.get("nodes", {})
    for group, items in nodes.items():
        base_label, sub_label = CATEGORY_MAP.get(group, ("entity", group))
        for item in items or []:
            item = dict(item)
            kg_id = str(item.get("id", ""))
            name = str(item.get("name", kg_id or "unknown"))
            props = dict(item)
            props["kg_id"] = kg_id
            props["type"] = sub_label
            props["category"] = group
            if "name" not in props:
                props["name"] = name
            docs.append(
                {
                    "_id": kg_id or None,
                    "name": name,
                    "labels": [base_label, sub_label],
                    "properties": props,
                }
            )
    return docs


def make_bulk_ndjson(index, docs):
    lines = []
    for doc in docs:
        meta = {"index": {"_index": index}}
        if doc.get("_id"):
            meta["index"]["_id"] = doc["_id"]
        lines.append(json.dumps(meta, ensure_ascii=False))
        body = {k: v for k, v in doc.items() if k != "_id"}
        lines.append(json.dumps(body, ensure_ascii=False))
    return ("\n".join(lines) + "\n").encode("utf-8")


def recreate_index(base_url, index, headers, ssl_context):
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
        request_json("DELETE", f"{base_url}/{index}", None, headers, ssl_context)
    except urllib.error.HTTPError as e:
        if e.code != 404:
            raise
    request_json("PUT", f"{base_url}/{index}", mapping, headers, ssl_context)


def main():
    args = parse_args()
    src_path = Path(args.input).expanduser().resolve()
    if not src_path.exists():
        print(f"Input file not found: {src_path}", file=sys.stderr)
        return 1

    ssl_context = make_ssl_context(args)
    maybe_disable_proxy(args.url, args.disable_proxy)
    headers = auth_headers(args.user, args.password)
    headers_json = dict(headers)
    headers_json["Content-Type"] = "application/json"

    try:
        if args.recreate_index:
            recreate_index(args.url.rstrip("/"), args.index, headers_json, ssl_context)

        payload = load_source(str(src_path))
        docs = normalize_docs(payload)
        if not docs:
            print("No documents generated from input.")
            return 0

        bulk_body = make_bulk_ndjson(args.index, docs)
        bulk_headers = dict(headers)
        bulk_headers["Content-Type"] = "application/x-ndjson"
        status, body = request_raw(
            "POST",
            f"{args.url.rstrip('/')}/_bulk?refresh=true",
            bulk_body,
            bulk_headers,
            ssl_context,
        )
        resp = json.loads(body)
        if resp.get("errors"):
            print(json.dumps(resp, ensure_ascii=False, indent=2))
            print("Bulk import completed with errors.", file=sys.stderr)
            return 2

        print(f"Indexed {len(docs)} docs into {args.index} (HTTP {status}).")
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
