#!/usr/bin/env bash
set -euo pipefail

# Clean local-only artifacts that should never be versioned.
find . -type f \( -name '._*' -o -name '.DS_Store' -o -name '~$*' -o -name '*.log' \) -delete
rm -rf .pycache .m2repo

# Common build artifacts in sub-projects
rm -rf kg-server-2/target
rm -rf kortex-all/node_modules kortex-all/dist

echo "Local artifacts cleaned."
