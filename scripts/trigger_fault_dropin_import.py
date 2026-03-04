#!/usr/bin/env python3
import argparse
import json
import sys
import urllib.request


def main():
    p = argparse.ArgumentParser(description="Trigger backend import for fault knowledge JSON drop-in directory.")
    p.add_argument("--base-url", default="http://127.0.0.1:8052", help="Backend base URL")
    args = p.parse_args()
    url = args.base_url.rstrip("/") + "/knowledgebase/admin/fault/importDropin"
    req = urllib.request.Request(url, method="POST")
    try:
        with urllib.request.urlopen(req) as resp:
            print(json.dumps(json.loads(resp.read().decode("utf-8")), ensure_ascii=False, indent=2))
        return 0
    except Exception as e:
        print(f"Failed: {e}", file=sys.stderr)
        return 1


if __name__ == "__main__":
    sys.exit(main())
