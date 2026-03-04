# ToolController API 文档

## 通用说明
- 基础路径：`/tool`，所有接口均返回 JSON。
- 鉴权：需在查询参数中携带对应的 `token`（默认值仅用于本地调试，正式环境请替换为前端可配置的 Dify API Token）。
- 若未显式声明 HTTP 方法，请默认使用 `POST`。上传文件接口使用 `multipart/form-data`，纯 JSON 接口使用 `application/json`。
- 返回体中的 `code` 字段为业务状态码（`200` 表示成功），HTTP 状态码仍以 Spring Boot 默认值为准。

## 接口详情

### 更新知识库文档
- **URL**：`POST /tool/updateDocument`
- **说明**：向 Dify 数据集同步本地文件并轮询索引状态。
- **参数**

| 参数 | 位置 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| datasetId | query | string | 是 | Dify 数据集 ID。 |
| documentId | query | string | 是 | 目标文档 ID。 |
| filePath | query | string | 是 | 后端可访问的文件绝对路径。 |
| token | query | string | 是 | Dify 数据集写入 Token。 |

- **响应示例**

```json
{
  "code": 200,
  "message": "索引完成",
  "batch": "doc_batch_123",
  "status": "completed"
}
```

失败时返回 `code`（如 400、404、500）与 `message` 描述异常。

### 文本提取并写入图谱
- **URL**：`POST /tool/extractAndInsert`
- **说明**：调用 Dify Workflow 解析输入文本、写入 Neo4j，并返回写入统计。
- **参数**

| 参数 | 位置 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| token | query | string | 否 | Workflow Token，默认值用于开发环境。 |
| text | body | string | 是 | 需要抽取的原始文本。 |
| 其他字段 | body | any | 否 | 会原样透传给 workflow 的 `inputs`。 |

- **响应示例**

```json
{
  "code": 200,
  "message": "处理完成",
  "successNodeCount": 2,
  "failedNodeCount": 0,
  "successRelationCount": 1,
  "failedRelationCount": 0,
  "successNodes": [
    { "id": 101, "labels": ["Person"], "properties": { "name": "张三" } }
  ],
  "successRelations": [
    {
      "type": "KNOWS",
      "start": { "id": 101, "name": "张三", "labels": ["Person"] },
      "end": { "id": 102, "name": "李四", "labels": ["Person"] },
      "properties": { "source": "workflow" }
    }
  ],
  "errors": []
}
```

### 图片提取并写入图谱
- **URL**：`POST /tool/extractAndInsertFromImage`
- **说明**：上传图片，由 workflow 提取节点并写入 Neo4j。
- **参数**

| 参数 | 位置 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| file | form-data | file | 是 | 单张图片文件。 |
| token | query | string | 否 | 图片提取 workflow Token。 |

- **响应示例**

```json
{
  "code": 200,
  "message": "处理完成",
  "successNodeCount": 3,
  "failedNodeCount": 0,
  "successNodes": [
    { "id": 201, "labels": ["Device"], "properties": { "name": "传感器A" } }
  ],
  "errors": []
}
```

### TXT 批量提取并写入图谱
- **URL**：`POST /tool/extractAndInsertFromFile`
- **说明**：按行读取上传的 `.txt` 文件，逐行调用 `extractAndInsert` 并累计结果。
- **参数**

| 参数 | 位置 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| file | form-data | file | 是 | UTF-8 文本文件，每行一条记录。 |
| token | query | string | 否 | Workflow Token。 |

- **响应示例**

```json
{
  "code": 200,
  "message": "文件处理完成",
  "fileName": "batch.txt",
  "totalLines": 10,
  "successLines": 9,
  "failedLines": 1,
  "totalSuccessNodes": 20,
  "totalSuccessRelations": 8,
  "lineResults": [
    {
      "lineNumber": 1,
      "lineText": "故障: 传感器A 与 控制器B 失联",
      "successNodeCount": 3,
      "successRelationCount": 1,
      "code": 200
    }
  ],
  "errors": [
    "第 5 行处理失败: workflow 返回空结果"
  ]
}
```

### 文本→冲突消解→写入→导出
- **URL**：`POST /tool/extractResolveAndInsert`
- **说明**：完整流水线：文本提取 → 冲突消解 → 更新数据集文件 → 写入 Neo4j → 导出。
- **参数**

| 参数 | 位置 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| extractToken | query | string | 否 | 提取 workflow Token。 |
| resolveToken | query | string | 否 | 冲突消解 workflow Token。 |
| datasetId / documentId / filePath / fileToken | query | string | 否 | 若提供则同步更新 Dify 文件。`filePath` 需为后端可访问路径。 |
| text | body | string | 是 | 待处理文本。 |
| 其他字段 | body | any | 否 | 透传给提取 workflow。 |

- **响应示例**

```json
{
  "code": 200,
  "message": "处理完成",
  "successNodeCount": 4,
  "failedNodeCount": 0,
  "successRelationCount": 2,
  "failedRelationCount": 0,
  "resolvedNodeCount": 1,
  "insertedNodeCount": 3,
  "successNodes": [...],
  "successRelations": [...],
  "fileUpdateResult": {
    "code": 200,
    "message": "索引完成",
    "batch": "doc_batch_456",
    "status": "completed"
  },
  "errors": []
}
```

### 图片→冲突消解→写入→导出
- **URL**：`POST /tool/extractResolveAndInsertFromImage`
- **说明**：与上一步流程一致，但输入为图片文件。
- **参数**：同上，额外需要 `file`（form-data）。
- **响应**：结构同 `extractResolveAndInsert`，不包含 `successRelations` 字段时默认为空数组。

### 仅提取图片节点
- **URL**：`POST /tool/extractNodesFromImage`
- **说明**：上传图片，仅返回 workflow 提取出的节点列表，**不会写入 Neo4j**。
- **参数**

| 参数 | 位置 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| file | form-data | file | 是 | 图片文件。 |
| token | query | string | 否 | Workflow Token。 |

- **响应示例**

```json
[
  { "id": null, "labels": ["Equipment"], "properties": { "name": "控制器B" } }
]
```

### 仅提取文本节点与关系
- **URL**：`POST /tool/extractFromText`
- **说明**：调用 workflow 提取但不写入数据库，用于前端预览。
- **参数**：同 `extractAndInsert`。
- **响应示例**

```json
{
  "code": 200,
  "message": "提取成功",
  "nodeCount": 2,
  "relationCount": 1,
  "nodes": [
    { "id": null, "labels": ["Event"], "properties": { "name": "通信中断" } }
  ],
  "relations": [
    {
      "type": "CAUSES",
      "start": {
        "name": "传感器A",
        "labels": ["Device"]
      },
      "end": {
        "name": "通信中断",
        "labels": ["Event"]
      },
      "properties": { "confidence": "0.82" }
    }
  ]
}
```

### 冲突消解
- **URL**：`POST /tool/resolveConflict`
- **说明**：调用冲突消解 workflow，判断节点是否需要合并，返回对照结果。
- **参数**

| 参数 | 位置 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| token | query | string | 否 | 冲突消解 workflow Token。 |
| target | body | string | 是 | JSON 字符串，包含目标节点属性（组件会直接透传）。 |

- **响应示例**

```json
{
  "target": {
    "id": null,
    "labels": ["model", "算法模型类"],
    "properties": {
      "name": "Trajectory-KF轨迹预测算法模型",
      "type": "算法模型类",
      "algorithm": "卡尔曼滤波",
      "input_parameters": "位置坐标、速度向量和加速度",
      "output": "预测轨迹坐标和置信度",
      "application": "无人机轨迹跟踪和预测场景",
      "update_frequency": "10Hz",
      "process_noise": "0.1",
      "observation_noise": "1.0"
    }
  },
  "similar_target": {
    "type": "node",
    "id": "245",
    "labels": ["model", "算法模型类"],
    "properties": {
      "name": "故障诊断算法模型6-2",
      "type": "算法模型类",
      "推荐算法模型": "支持向量机",
      "算法模型参数设置": "核函数：RBF",
      "关联事件": "无人机屏蔽器损坏",
      "执行条件": "探测精度降低至45%"
    }
  },
  "judge": "否",
  "judge_reason": "两个知识代表不同的算法模型实体：目标知识是'Trajectory-KF轨迹预测算法模型'，基于卡尔曼滤波用于轨迹预测；相似知识是'故障诊断算法模型6-2'，基于支持向量机用于故障诊断。两者在应用场景、输入输出、算法类型和属性上无重叠、冲突或互补信息，属于无关实体。",
  "finalKg": null
}
```

**字段说明：**
- `target`：解析后的目标节点对象（原始输入）
- `similar_target`：知识库中最相似的节点对象（如果找到）
- `judge`：判断结果，可能返回 `"是"`（需要合并）或 `"否"`（不需要合并，直接添加）
- `judge_reason`：判断理由的详细说明
- `finalKg`：最终知识图谱节点，当 `judge` 为 `"是"` 时包含合并后的节点信息，为 `"否"` 时为 `null`

### TXT 批量仅提取
- **URL**：`POST /tool/extractFromFile`
- **说明**：按行提取文本，仅返回节点/关系，**不写 Neo4j**。
- **参数**：同 `extractAndInsertFromFile`。
- **响应示例**

```json
{
  "code": 200,
  "message": "文件提取完成",
  "fileName": "draft.txt",
  "totalLines": 5,
  "successLines": 5,
  "failedLines": 0,
  "totalNodeCount": 12,
  "totalRelationCount": 4,
  "nodes": [...],
  "relations": [...],
  "lineResults": [
    {
      "lineNumber": 1,
      "lineText": "设备A 与 设备B 建立连接",
      "nodeCount": 2,
      "relationCount": 1,
      "status": "success"
    }
  ],
  "errors": []
}
```

## 前端集成提示
- 文件上传接口需使用 `multipart/form-data`，同时可透传业务字段（如 `userId`）在 query 或 header。
- 所有接口都返回结构化错误信息（`code` + `message` 或 `errors` 数组），前端应优先展示 `message`。
- `successNodes` / `successRelations` / `finalKg` 中的节点均遵循 `BasicNode` 结构：`id`（Long，可为空）、`labels`（List\<String\>）、`properties`（Map）。
- 若需使用默认 token，请通过环境配置注入，避免在前端代码中硬编码。
