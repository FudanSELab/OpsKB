# ToolController API 文档

## 概述

`ToolController` 提供知识图谱工具相关的API接口，包括文本、图片、文件的实体关系提取与插入、文档更新等功能。

- **Base URL**: `/tool`
- **支持跨域**: 是 (CORS)
- **返回格式**: JSON
- 记得打开neo4j的文件导出功能，下面data.txt就是导出的文件
- 要替换的内容
  - 知识抽取的token
  - 冲突消解的token
  - 数据库的token
  - 数据库的id
  - 文件的id
  - neo4j的文件导出地址
  - dify的url


---

## 目录

1. [文档管理](#1-文档管理)
2. [简单提取与插入](#2-简单提取与插入不含冲突消解)
3. [完整流程（含冲突消解）](#3-完整流程含冲突消解)

---

## 1. 文档管理

### 1.1 更新文档并检查索引状态

**接口地址**: `POST /tool/updateDocument`

**功能描述**: 更新文件服务器中的文档并检查索引状态

**请求参数**:

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| datasetId | String | 否 | 1acb70bf-ac30-4ab1-a20e-45ee5a09b813 | 数据集ID |
| documentId | String | 否 | 66d46d95-3ccc-4145-8f29-e9ee5021433d | 文档ID |
| filePath | String | 否 | C:\Users\shx\IdeaProjects\neo4j-community-3.5.31\import\data.txt | 本地文件路径 |
| token | String | 否 | dataset-rq4IF05IL7l4qY7aqaxCUMUL//数据库的token | 授权token |

**请求示例**:

```bash
POST http://localhost:8052/tool/updateDocument?datasetId=xxx&documentId=xxx&filePath=xxx&token=xxx
```

**响应示例**:

```json
{
  "code": 200,
  "message": "文档更新成功",
  "status": "completed"
}
```

---

## 2. 简单提取与插入（不含冲突消解）

### 2.1 从文本提取实体关系并插入

**接口地址**: `POST /tool/extractAndInsert`

**功能描述**: 通过Dify工作流从文本中提取实体和关系，并直接插入到Neo4j图数据库

**请求参数**:

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| inputParams | JSON Object | 是 | - | 输入参数，必须包含text字段 |
| token | String | 否 | app-u0GLt5Xl10a6UStzI5ObouBu知识抽取的token | 工作流授权token |

**请求示例**:

```bash
POST http://localhost:8052/tool/extractAndInsert?token=app-u0GLt5Xl10a6UStzI5ObouBu
Content-Type: application/json

{
  "text": "张三是李四的朋友，他们都在北京大学学习。"//从测试中拿，无关的内容llm不会抽取
}
```

**响应示例**:

```json
{
  "code": 200,
  "message": "处理完成",
  "successNodeCount": 3,
  "failedNodeCount": 0,
  "successRelationCount": 2,
  "failedRelationCount": 0,
  "successNodes": [
    {
      "id": 123,
      "labels": ["Person"],
      "properties": {
        "name": "张三"
      }
    }
  ],
  "successRelations": [
    {
      "start": {...},
      "end": {...},
      "type": "朋友",
      "properties": {}
    }
  ]
}
```

---

### 2.2 从图片提取实体并插入

**接口地址**: `POST /tool/extractAndInsertFromImage`

**功能描述**: 上传图片，通过Dify工作流提取实体信息并插入到图数据库（仅提取节点，不提取关系）

**请求参数**:

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| file | File | 是 | - | 图片文件 |
| token | String | 否 | app-u0GLt5Xl10a6UStzI5ObouBu知识抽取的token | 工作流授权token |

**请求示例**:

```bash
POST http://localhost:8052/tool/extractAndInsertFromImage
Content-Type: multipart/form-data

file: [图片文件]
token: app-u0GLt5Xl10a6UStzI5ObouBu
```

**响应示例**:

```json
{
  "code": 200,
  "message": "处理完成",
  "successNodeCount": 5,
  "failedNodeCount": 0,
  "successNodes": [
    {
      "id": 456,
      "labels": ["Object"],
      "properties": {
        "name": "书桌"
      }
    }
  ]
}
```

---

### 2.3 从txt文件按行提取实体关系并插入

**接口地址**: `POST /tool/extractAndInsertFromFile`

**功能描述**: 上传txt文件，按行读取并处理，每一行作为独立的文本调用工作流提取实体关系并插入（不做冲突消解）

**特点**:
- 自动跳过空行
- 每行独立处理，互不影响
- 返回每行的详细处理结果
- 自动统计总体数据

**请求参数**:

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| file | File | 是 | - | txt文件（UTF-8编码） |
| token | String | 否 | app-u0GLt5Xl10a6UStzI5ObouBu//知识抽取的token | 工作流授权token |

**请求示例**:

```bash
POST http://localhost:8080/tool/extractAndInsertFromFile
Content-Type: multipart/form-data

file: [txt文件]
token: app-u0GLt5Xl10a6UStzI5ObouBu
```

**响应示例**:

```json
{
  "code": 200,
  "message": "文件处理完成",
  "fileName": "test.txt",
  "totalLines": 10,
  "successLines": 9,
  "failedLines": 1,
  "totalSuccessNodes": 45,
  "totalFailedNodes": 2,
  "totalSuccessRelations": 30,
  "totalFailedRelations": 1,
  "lineResults": [
    {
      "lineNumber": 1,
      "lineText": "张三是李四的朋友...",
      "code": 200,
      "successNodeCount": 5,
      "failedNodeCount": 0,
      "successRelationCount": 3,
      "failedRelationCount": 0
    },
    {
      "lineNumber": 2,
      "lineText": "...",
      "code": 500,
      "message": "处理失败: ..."
    }
  ],
  "errors": [
    "第 2 行处理失败: xxx"
  ]
}
```

**响应字段说明**:

| 字段名 | 类型 | 说明 |
|--------|------|------|
| totalLines | Integer | 总有效行数（不含空行） |
| successLines | Integer | 成功处理的行数 |
| failedLines | Integer | 失败的行数 |
| totalSuccessNodes | Integer | 所有行累计成功插入的节点数 |
| totalFailedNodes | Integer | 所有行累计失败的节点数 |
| totalSuccessRelations | Integer | 所有行累计成功插入的关系数 |
| totalFailedRelations | Integer | 所有行累计失败的关系数 |
| lineResults | Array | 每一行的详细处理结果 |
| errors | Array | 错误信息列表 |

---

## 3. 完整流程（含冲突消解）

### 3.1 从文本提取实体关系（完整流程）

**接口地址**: `POST /tool/extractResolveAndInsert`

**功能描述**: 完整流程处理文本，包括：提取实体关系 → 抽取完成立即更新文档 → 冲突消解 → 插入数据库

**流程步骤**:
1. 调用Dify工作流提取实体和关系
2. 分离节点和关系
3. **抽取完成后立即更新文档到文件服务器** ⭐
4. 对每个节点进行冲突消解（判断是更新还是插入）
5. 插入关系
6. 执行数据导出

**请求参数**:

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| inputParams | JSON Object | 是 | - | 输入参数，必须包含text字段 |
| extractToken | String | 否 | app-u0GLt5Xl10a6UStzI5ObouBu | 提取工作流token |
| resolveToken | String | 否 | app-bk9ZVk1iv5bEzH3JypwcthKf | 冲突消解工作流token |
| datasetId | String | 否 | 1acb70bf-ac30-4ab1-a20e-45ee5a09b813 | 数据集ID |
| documentId | String | 否 | 66d46d95-3ccc-4145-8f29-e9ee5021433d | 文档ID |
| filePath | String | 否 | C:\Users\shx\IdeaProjects\neo4j-community-3.5.31\import\data.txt | 文件路径 |
| fileToken | String | 否 | dataset-rq4IF05IL7l4qY7aqaxCUMUL//数据库的token | 文件服务授权token |

**请求示例**:

```bash
POST http://localhost:8080/tool/extractResolveAndInsert?extractToken=xxx&resolveToken=xxx&datasetId=xxx&documentId=xxx&filePath=xxx&fileToken=xxx
Content-Type: application/json

{
  "text": "张三是李四的朋友，他们都在北京大学学习。"
}
```

**响应示例**:

```json
{
  "code": 200,
  "message": "处理完成",
  "successNodeCount": 3,
  "failedNodeCount": 0,
  "successRelationCount": 2,
  "failedRelationCount": 0,
  "resolvedNodeCount": 1,
  "insertedNodeCount": 2,
  "successNodes": [...],
  "successRelations": [...],
  "fileUpdateResult": {
    "code": 200,
    "message": "文档更新成功",
    "status": "completed"
  }
}
```

**响应字段说明**:

| 字段名 | 类型 | 说明 |
|--------|------|------|
| successNodeCount | Integer | 成功处理的节点总数 |
| failedNodeCount | Integer | 失败的节点数 |
| successRelationCount | Integer | 成功插入的关系数 |
| failedRelationCount | Integer | 失败的关系数 |
| resolvedNodeCount | Integer | 通过冲突消解更新的节点数 |
| insertedNodeCount | Integer | 新插入的节点数 |
| fileUpdateResult | Object | 文档更新结果 |
| errors | Array | 错误信息列表（如有） |

---

### 3.2 从图片提取实体（完整流程）

**接口地址**: `POST /tool/extractResolveAndInsertFromImage`

**功能描述**: 完整流程处理图片，包括：上传图片 → 提取实体 → 抽取完成立即更新文档 → 冲突消解 → 插入数据库

**流程步骤**:
1. 上传图片到Dify
2. 调用工作流提取实体
3. 解析提取结果
4. **抽取完成后立即更新文档到文件服务器** ⭐
5. 对每个节点进行冲突消解（判断是更新还是插入）
6. 执行数据导出

**请求参数**:

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| file | File | 是 | - | 图片文件 |
| extractToken | String | 否 | app-u0GLt5Xl10a6UStzI5ObouBu | 提取工作流token |
| resolveToken | String | 否 | app-bk9ZVk1iv5bEzH3JypwcthKf | 冲突消解工作流token |
| datasetId | String | 否 | 1acb70bf-ac30-4ab1-a20e-45ee5a09b813 | 数据集ID |
| documentId | String | 否 | 66d46d95-3ccc-4145-8f29-e9ee5021433d | 文档ID |
| filePath | String | 否 | C:\Users\shx\IdeaProjects\neo4j-community-3.5.31\import\data.txt | 文件路径 |
| fileToken | String | 否 | dataset-rq4IF05IL7l4qY7aqaxCUMUL | 文件服务授权token |

**请求示例**:

```bash
POST http://localhost:8080/tool/extractResolveAndInsertFromImage
Content-Type: multipart/form-data

file: [图片文件]
extractToken: app-u0GLt5Xl10a6UStzI5ObouBu
resolveToken: app-bk9ZVk1iv5bEzH3JypwcthKf
datasetId: 1acb70bf-ac30-4ab1-a20e-45ee5a09b813
documentId: 66d46d95-3ccc-4145-8f29-e9ee5021433d
filePath: C:\Users\shx\IdeaProjects\neo4j-community-3.5.31\import\data.txt
fileToken: dataset-rq4IF05IL7l4qY7aqaxCUMUL
```

**响应示例**:

```json
{
  "code": 200,
  "message": "处理完成",
  "successNodeCount": 5,
  "failedNodeCount": 0,
  "resolvedNodeCount": 2,
  "insertedNodeCount": 3,
  "successNodes": [...],
  "fileUpdateResult": {
    "code": 200,
    "message": "文档更新成功",
    "status": "completed"
  }
}
```

---

## 通用说明

### 错误码

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误或数据格式错误 |
| 500 | 服务器内部错误 |

### 数据结构说明

#### BasicNode（节点）

```json
{
  "id": 123,
  "labels": ["Person", "Student"],
  "properties": {
    "name": "张三",
    "age": "20",
    "gender": "男"
  }
}
```

#### Relation（关系）

```json
{
  "start": {
    "id": 123,
    "labels": ["Person"],
    "properties": {...}
  },
  "end": {
    "id": 456,
    "labels": ["Organization"],
    "properties": {...}
  },
  "type": "就职于",
  "properties": {
    "since": "2020"
  }
}
```

---

## 使用建议

### 1. 选择合适的接口

| 场景 | 推荐接口 | 说明 |
|------|---------|------|
| 简单文本提取 | `/extractAndInsert` | 快速提取和插入，不做冲突处理 |
| 批量文本处理 | `/extractAndInsertFromFile` | 适合处理包含多行文本的txt文件 |
| 需要冲突消解 | `/extractResolveAndInsert` | 完整流程，避免重复数据 |
| 图片识别 | `/extractAndInsertFromImage` | 简单图片处理 |
| 图片识别+冲突消解 | `/extractResolveAndInsertFromImage` | 图片完整流程 |

### 2. Token配置

需要配置的Token类型：
- **extractToken**: Dify提取工作流的API Token
- **resolveToken**: Dify冲突消解工作流的API Token
- **fileToken**: 文件服务的授权Token（用于更新文档）

### 3. 文件要求

- **txt文件**: UTF-8编码，每行一个文本单元
- **图片文件**: 支持常见图片格式（jpg, png等）
- **文件大小**: 建议不超过10MB

### 4. 性能考虑

- 文件按行处理时，每行都会调用一次工作流API，大文件可能耗时较长
- 建议分批处理大量数据
- 冲突消解会增加处理时间，但能保证数据质量

---

## 配置说明

### 需要配置的参数

在实际使用时，需要修改以下默认值：

1. **Dify相关**
   - 工作流URL: `http://localhost/v1/workflows/run`
   - 文件上传URL: `http://localhost/v1/files/upload`
   - extractToken: 提取工作流的API Token
   - resolveToken: 冲突消解工作流的API Token

2. **文件服务相关**
   - datasetId: 数据集ID
   - documentId: 文档ID
   - filePath: 本地文件路径
   - fileToken: 文件服务授权Token

3. **Neo4j相关**
   - 在 `application.properties` 中配置Neo4j连接信息



