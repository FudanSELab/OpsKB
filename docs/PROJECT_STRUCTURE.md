# Project Structure

This repository contains multiple sub-projects for knowledge-base tooling.

## Top-level modules

- `kg-server-2/`: Java Spring Boot backend (Neo4j/ES integration).
- `kortex-all/`: Vue frontend project.
- `PromCopilot/`: Python tooling and data processing experiments.
- `scripts/`: cross-project import and automation scripts.
- `kb_inputs/`: input datasets for ingestion pipelines.
- `配置部署说明/`: deployment-related documents and third-party support files.

## Recommended maintenance rules

- Keep source code and docs in module directories.
- Keep local caches/build outputs untracked (`target/`, `node_modules/`, `.pycache/`, `.m2repo/`).
- Run `scripts/clean_local_artifacts.sh` before committing when your workspace has many temp files.
- Do not commit OS metadata files (e.g., `.DS_Store`, `._*`).
