# OpsKb 部署说明
OpsKb项目构建了面向运维领域的3套知识库，涵盖服务依赖、部署架构、故障处理、日志等方面的知识，能够用于基于大模型的智能化运维,并提供可视化展示工具

## 0. 概述

项目所需的环境信息如下：

| 组件 | 当前版本 / 状态 |
| --- | --- |
| 操作系统主机名 | `k8s-node-2` |
| JDK | `17.0.18` |
| Maven | `3.6.3` |
| Node.js | `v20.20.0` |
| npm | `10.8.2` |
| Neo4j | `5.14.0` |
| Elasticsearch | `8.11.0` |
| Docker | `27.5.1` |
| 后端端口 | `8052` |
| 前端开发端口 | `5173` |
| Neo4j HTTP / Bolt | `7474 / 7687` |
| Elasticsearch | `9200 / 9300` |

## 1. 项目结构

仓库主要分为三部分：

- `kg-server-2`：Spring Boot 后端，负责知识库读写、Neo4j 查询、ES 查询、文件导入、工作流接口等。
- `kortex-all`：Vue 3 + Vite 前端。
- `PromCopilot`：用于构建系统上下文知识库的离线脚本，负责把 CSV 导入 Neo4j / ES。

建议把这三部分理解成两条链路：

1. 在线服务链路  
   前端 `kortex-all` -> 后端 `kg-server-2` -> Neo4j / Elasticsearch

2. 离线构建链路  
   `PromCopilot` 数据脚本 -> Neo4j / Elasticsearch

## 2. 当前运行架构

当前代码里，前后端和存储的连接关系如下：

- 前端通过 Vite 开发服务器启动，默认监听 `0.0.0.0:5173`
- 前端把 `/knowledgebase` 和 `/question` 代理到 `http://127.0.0.1:8052`
- 后端默认监听 `0.0.0.0:8052`
- 后端通过 `bolt://localhost:7687` 访问 Neo4j
- 后端通过 `http://localhost:9200` 访问 Elasticsearch
- Neo4j 承担可编辑知识库的主存储
- Elasticsearch 承担只读检索和部分知识库目录化浏览

当前服务器上已确认以下端口处于监听状态：

- `5173`：前端开发服务
- `8052`：Spring Boot 后端
- `7474`：Neo4j Browser
- `7687`：Neo4j Bolt
- `9200`：Elasticsearch HTTP

## 3. 环境要求

按当前代码，最低建议环境如下：

- JDK `17`
- Maven `3.6.3` 及以上
- Node.js `16.20.0` 及以上
- npm `10.x`
- Neo4j Community `5.x`
- Elasticsearch `8.11.x`
- Docker `20+`

如果要完全对齐当前服务器，建议直接使用：

- JDK `17.0.18`
- Maven `3.6.3`
- Node.js `20.20.0`
- npm `10.8.2`
- Neo4j `5.14.0`
- Elasticsearch `8.11.0`

## 4. 代码中的关键配置入口

### 4.1 后端配置

后端主配置文件：

- `kg-server-2/src/main/resources/application.properties`

当前关键配置如下：

```properties
spring.data.neo4j.uri=bolt://localhost:7687
spring.data.neo4j.username=neo4j
spring.data.neo4j.password=promcopilot

server.port=8052
server.address=0.0.0.0

spring.elasticsearch.rest.uris=http://localhost:9200

kb.read.es.index1=kb_nodes_1
kb.read.es.index2=kb_nodes_2,api,container,deployment,label_value_pair,metric,namespace,node,pod,replicaset,service,statefulset
kb.read.es.index3=openstack_parameters,openstack_templates,openstack_workflows,hdfs_parameters,hdfs_templates,hdfs_workflows,openssh_parameters,openssh_templates,openssh_workflows,trainticket_parameters,trainticket_templates,trainticket_workflows

kb.fault.dropin.dir=/Users/ethanyuan/works/kb_tools/kb_inputs/fault-dropin
kb.fault.processed.dir=/Users/ethanyuan/works/kb_tools/kb_inputs/fault-dropin-processed
```

### 4.2 前端代理配置

前端代理配置在：

- `kortex-all/vite.config.js`

当前开发代理规则：

```js
proxy: {
  '/knowledgebase': {
    target: 'http://127.0.0.1:8052',
    changeOrigin: true,
  },
  '/question': {
    target: 'http://127.0.0.1:8052',
    changeOrigin: true,
  },
}
```

这意味着：

- 前端开发环境默认假设后端和前端在同一台机器上
- 如果前后端不在同机，必须改 `vite.config.js`

### 4.3 Neo4j 导入目录配置

`MainServiceImpl` 中的 CSV / JSON 导入目录不是写在 `application.properties` 里，而是通过 `@Value` 带默认值：

```java
@Value("${neo4j.import.csv-dir:/Users/ethanyuan/tools/neo4j-community-5/import/csv/}")
private String csvFolder;

@Value("${neo4j.import.json-dir:/Users/ethanyuan/tools/neo4j-community-5/import/json/}")
private String jsonFolder;
```

如果部署机器不是开发者本机路径，这两个值必须显式覆盖。推荐直接在 `application.properties` 里补上：

```properties
neo4j.import.csv-dir=/var/lib/neo4j/import/csv/
neo4j.import.json-dir=/var/lib/neo4j/import/json/
```

否则：

- `/main/loadFromCSV` 会把文件写到错误路径
- 节点/关系 CSV 导入功能会直接失败

## 5. 知识库与存储映射

当前后端定义了三套知识库：

### 5.1 `fault-kb`

- 名称：故障知识库
- 默认存储：`NEO4J`
- ES 只读索引：`kb_nodes_1`

### 5.2 `promcopilot`

- 名称：系统上下文知识库
- 默认存储：`NEO4J`
- ES 只读索引：
  - `kb_nodes_2`
  - `api`
  - `container`
  - `deployment`
  - `label_value_pair`
  - `metric`
  - `namespace`
  - `node`
  - `pod`
  - `replicaset`
  - `service`
  - `statefulset`
- 预留数据源：`PROMETHEUS`

### 5.3 `logcopilot`

- 名称：日志知识库
- 默认存储：`ES`
- ES 只读索引：
  - `openstack_parameters`
  - `openstack_templates`
  - `openstack_workflows`
  - `hdfs_parameters`
  - `hdfs_templates`
  - `hdfs_workflows`
  - `openssh_parameters`
  - `openssh_templates`
  - `openssh_workflows`
  - `trainticket_parameters`
  - `trainticket_templates`
  - `trainticket_workflows`

### 5.4 读写规则

当前代码的实际行为是：

- `NEO4J` 源允许增删改
- `ES` 源只读
- 前端切到 `ES` 或 `PROMETHEUS` 时，知识库会提示只读

也就是说，ES 在这套系统里承担的是：

- 快速检索
- 目录型浏览
- 补充型只读数据源

不是主写库。

## 6. Neo4j 部署与配置

### 6.1 安装版本

当前服务器使用的是：

- Neo4j Community `5.14.0`

### 6.2 关键配置文件

当前服务器配置文件位置：

- `/etc/neo4j/neo4j.conf`

已确认配置：

```properties
server.directories.import=/var/lib/neo4j/import
dbms.security.procedures.unrestricted=gds.*,apoc.*
```

注意：

- `initial.dbms.default_database=...` 当前仍是注释状态
- `dbms.security.allow_csv_import_from_file_urls=true` 当前也是注释状态

### 6.3 推荐配置

部署时建议在 `/etc/neo4j/neo4j.conf` 中确认或补充以下配置：

```properties
server.directories.import=/var/lib/neo4j/import
dbms.security.procedures.unrestricted=gds.*,apoc.*
apoc.import.file.enabled=true
```

如果需要使用文件导入、APOC 导出，还建议检查：

```properties
dbms.security.allow_csv_import_from_file_urls=true
```

不同 Neo4j 5.x 小版本对配置项命名有变化，实际以 `neo4j.conf` 模板和启动日志为准。如果某个配置项不识别，以官方 5.14 配置格式调整，不要照搬旧版 3.x/4.x 参数。

### 6.4 插件

当前服务器 `/var/lib/neo4j/plugins` 中已存在：

- `apoc-5.14.0-core.jar`
- `neo4j-graph-data-science-2.5.5.jar`

这两个插件和当前服务逻辑直接相关：

- `apoc`：用于 `apoc.export.json.all("data.txt", {})`
- `gds`：当前配置已放开调用权限

### 6.5 导入目录准备

后端依赖 Neo4j 导入目录内的两个子目录：

- `/var/lib/neo4j/import/csv/`
- `/var/lib/neo4j/import/json/`

如果目录不存在，请先创建：

```bash
sudo mkdir -p /var/lib/neo4j/import/csv
sudo mkdir -p /var/lib/neo4j/import/json
sudo chown -R neo4j:neo4j /var/lib/neo4j/import
```

### 6.6 Neo4j 启停与验证

```bash
sudo systemctl status neo4j
sudo systemctl restart neo4j
```

验证方式：

- 浏览器访问：`http://<server-ip>:7474`
- Bolt 端口：`7687`

## 7. Elasticsearch 部署与配置

### 7.1 当前部署方式

当前服务器上的 ES 通过 Docker 运行，版本是：

- Elasticsearch `8.11.0`

容器名：

- `es`

### 7.2 当前挂载配置

当前容器挂载关系已确认：

- 宿主机：`/root/es/elasticsearch.yml`
- 容器内：`/usr/share/elasticsearch/config/elasticsearch.yml`

当前配置文件内容非常简洁：

```yaml
xpack.security.enabled: false
http.host: 0.0.0.0
```

这说明当前 ES：

- 已关闭安全认证
- 直接暴露 HTTP 接口
- 后端使用 `http://localhost:9200` 即可连接

### 7.3 推荐启动命令

如果需要在新机器上按当前方式重建 ES：

```bash
mkdir -p /root/es
cat >/root/es/elasticsearch.yml <<'EOF'
xpack.security.enabled: false
http.host: 0.0.0.0
discovery.type: single-node
EOF

docker run -d \
  --name es \
  -p 9200:9200 \
  -p 9300:9300 \
  -m 3g \
  -v /root/es/elasticsearch.yml:/usr/share/elasticsearch/config/elasticsearch.yml \
  docker.elastic.co/elasticsearch/elasticsearch:8.11.0
```

建议把 `discovery.type: single-node` 也加上，单机部署更稳，少踩集群发现问题。

### 7.4 验证

```bash
curl http://127.0.0.1:9200
```

返回版本号为 `8.11.0` 即表示正常。

## 8. Neo4j 与 ES 的对接方式

这套系统里，Neo4j 和 ES 不是主从同步关系，而是“并行读源”。

### 8.1 后端层面的对接

后端同时加载：

- Neo4j OGM / Driver
- Spring Data Elasticsearch

因此：

- 一部分接口查 Neo4j
- 一部分接口查 ES
- 同一个知识库可以切换不同存储源

例如：

- `fault-kb` 默认查 Neo4j
- 如果前端切换到 `storage=ES`，则改为查 `kb_nodes_1`

### 8.2 ES 文档结构

后端中 ES 文档实体为：

- `kg-server-2/src/main/java/com/xidian/kg/entity/EsNodeDocument.java`

结构大致如下：

- `id`
- `name`
- `labels`
- `properties`

这份结构是按“尽量兼容 Neo4j 节点”设计的，因此前端切换数据源时不需要彻底换渲染逻辑。

### 8.3 目前没有自动双写

当前代码没有看到“Neo4j 写入后自动同步 ES”的完整链路。真实情况是：

- Neo4j 写入由后端接口负责
- ES 数据主要通过离线脚本或预处理导入
- 两边的数据一致性需要靠导入流程维护

这是部署时最需要注意的一点：

- Neo4j 改了，不代表 ES 自动更新
- ES 改了，也不会自动回写 Neo4j

如果上线后需要强一致，必须单独补同步机制。

## 9. 后端部署

### 9.1 打包

```bash
cd /path/to/kb_tools/kg-server-2
mvn clean package
```

打包完成后产物位于：

- `target/kg-0.0.1-SNAPSHOT.jar`

### 9.2 启动

```bash
cd /path/to/kb_tools/kg-server-2
nohup java -jar target/kg-0.0.1-SNAPSHOT.jar > app.log 2>&1 &
```

### 9.3 推荐上线前补齐的配置

建议把以下内容补进 `application.properties`，避免依赖本机默认路径：

```properties
spring.data.neo4j.uri=bolt://localhost:7687
spring.data.neo4j.username=neo4j
spring.data.neo4j.password=promcopilot

spring.elasticsearch.rest.uris=http://localhost:9200

neo4j.import.csv-dir=/var/lib/neo4j/import/csv/
neo4j.import.json-dir=/var/lib/neo4j/import/json/

kb.fault.dropin.dir=/data/kb_inputs/fault-dropin
kb.fault.processed.dir=/data/kb_inputs/fault-dropin-processed
```

### 9.4 启动验证

```bash
curl http://127.0.0.1:8052/knowledgebase/list
```

当前服务器已验证该接口可正常返回知识库列表。

## 10. 前端部署

### 10.1 安装依赖

```bash
cd /path/to/kb_tools/kortex-all
npm install
```

### 10.2 开发模式启动

```bash
npm run dev
```

默认监听：

- `0.0.0.0:5173`

### 10.3 生产模式

当前仓库已具备标准 Vite 构建命令：

```bash
npm run build
npm run preview
```

但目前线上实际运行的是开发模式 `5173`，不是标准静态站点部署。如果要走生产部署，建议：

1. `npm run build`
2. 用 Nginx 托管 `dist`
3. 把 API 反向代理到 `8052`

## 11. PromCopilot 数据构建

`PromCopilot` 主要负责构造系统上下文知识库数据。

### 11.1 Python 依赖

```bash
cd /path/to/kb_tools/PromCopilot
python3 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
```

当前关键依赖包括：

- `elasticsearch==8.13.0`
- `neo4j==5.19.0`

### 11.2 配置项

ES 配置文件：

- `PromCopilot/constant/es.py`

Neo4j 配置文件：

- `PromCopilot/constant/kg.py`

默认写的是远程 IP，需要根据部署环境改：

```python
ES_URL = 'http://<your-es-host>:9200'
NEO4J_URL = 'neo4j://<your-neo4j-host>:7687'
NEO4J_USER = 'neo4j'
NEO4J_PASSWORD = '...'
```

### 11.3 构建 Neo4j 数据

```bash
cd /path/to/kb_tools/PromCopilot
python3 build_kg.py
```

### 11.4 构建 ES 数据

```bash
cd /path/to/kb_tools/PromCopilot
python3 build_es.py
```

### 11.5 ES 索引说明

`PromCopilot/constant/es.py` 中定义了多个索引映射，常见包括：

- `api`
- `container`
- `deployment`
- `metric`
- `label_value_pair`
- `namespace`
- `node`
- `pod`
- `replicaset`
- `service`
- `statefulset`

这些索引与后端 `kb.read.es.index2` 保持一致，属于系统上下文知识库的 ES 侧目录数据。

## 12. 故障知识库导入

### 12.1 目录导入

后端支持读取 `extracted_cases` 风格的 JSON，并导入到 Neo4j。

对应接口：

```http
POST /knowledgebase/admin/fault/importDropin
```

默认目录来自：

- `kb.fault.dropin.dir`
- `kb.fault.processed.dir`

处理流程是：

1. 扫描待导入目录内的 `*.json`
2. 创建节点与关系
3. 自动补 `kb_id=fault-kb`
4. 导入完成后把文件移动到 processed 目录

### 12.2 CSV 导入

后端也支持通过 `/main/loadFromCSV` 导入节点或关系 CSV。

依赖前提：

- Neo4j import 目录存在 `csv/` 与 `json/`
- `neo4j.import.csv-dir` 和 `neo4j.import.json-dir` 配对正确
- APOC / 文件导入能力可用

## 13. 常用接口验证

### 13.1 查看知识库列表

```bash
curl http://127.0.0.1:8052/knowledgebase/list
```

### 13.2 按名称查节点

```bash
curl "http://127.0.0.1:8052/knowledgebase/queryByName?kbId=fault-kb&storage=NEO4J&name=test"
```

### 13.3 查 ES 节点

```bash
curl "http://127.0.0.1:8052/es/search?keyword=test"
```

### 13.4 查看 ES 状态

```bash
curl http://127.0.0.1:9200
```

### 13.5 查看 Neo4j 状态

```bash
systemctl status neo4j
```

## 14. 部署时最容易出问题的地方

### 14.1 `loadFromCSV` 默认路径是开发机路径

`MainServiceImpl` 里 CSV/JSON 目录默认还是：

- `/Users/ethanyuan/tools/neo4j-community-5/import/csv/`
- `/Users/ethanyuan/tools/neo4j-community-5/import/json/`

这在服务器上一定不对，必须覆盖。

### 14.2 `ToolController` 的默认索引文件路径与当前 Neo4j 路径不一致

`ToolController` 里写死的默认路径是：

- `/opt/neo4j-community-5/import/data.txt`

但当前服务器 Neo4j import 目录实际是：

- `/var/lib/neo4j/import`

如果要使用 `/tool/updateDocument` 之类的能力，先把这个默认路径改掉，或者调用接口时显式传 `filePath`。

### 14.3 APOC 导出依赖插件和文件权限

后端在节点或关系变更后会调用：

```cypher
CALL apoc.export.json.all("data.txt", {})
```

如果出现导出失败，优先检查：

- APOC 插件是否存在
- `neo4j.conf` 是否允许相关过程
- Neo4j 对 import 目录是否有写权限

### 14.4 ES 与 Neo4j 不自动同步

这是设计层面的注意事项，不是配置问题。

如果你只改了 Neo4j：

- 前端切到 `storage=ES` 时，看到的内容可能还是旧的

如果你只更新了 ES：

- Neo4j 图谱接口也不会自动反映变化

### 14.5 前端现在跑的是 dev server

当前服务器监听了 `5173`，说明线上仍是 Vite 开发模式。优点是方便，缺点也明显：

- 进程稳定性一般
- 没有标准静态资源托管
- 不适合作为长期生产方案

## 15. 推荐的部署顺序

建议按下面顺序落地：

1. 安装并启动 Neo4j
2. 配置 Neo4j import 目录、APOC、GDS
3. 安装并启动 Elasticsearch
4. 修改后端 `application.properties`
5. 显式补上 `neo4j.import.csv-dir` 与 `neo4j.import.json-dir`
6. 打包并启动后端
7. 启动前端
8. 使用 `/knowledgebase/list`、`/es/search`、Neo4j Browser、ES `curl` 做联调
9. 再执行 `PromCopilot` 数据构建或故障 JSON 导入

## 16. 当前服务器已确认的运行状态

`2026-04-15` 检查结果：

- Neo4j 服务处于 `active (running)`
- Elasticsearch 容器 `es` 正常运行
- 后端 `kg-0.0.1-SNAPSHOT.jar` 正在运行
- 前端开发服务正在监听 `5173`
- `/knowledgebase/list` 可正常返回三套知识库定义

如果只是做日常维护，说明现有服务已经能跑；真正需要优先补的是配置收口和路径去硬编码，而不是重新搭环境。
