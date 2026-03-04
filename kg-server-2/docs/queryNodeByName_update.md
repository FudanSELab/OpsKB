# queryNodeByName API 更新说明

## 功能说明

`/node/queryByName` 接口新增了一个可选参数 `exactMatch`，用于控制节点名称的匹配方式。

## API 接口

### 请求地址
```
GET /node/queryByName
```

### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| name | String | 是 | - | 要查询的节点名称 |
| exactMatch | Boolean | 否 | false | 是否精确匹配。true：全字符匹配；false：模糊匹配 |

### 参数说明

#### 1. 模糊匹配（默认行为）
当不传 `exactMatch` 参数或 `exactMatch=false` 时，使用模糊匹配（contains）。

**示例请求：**
```
GET /node/queryByName?name=测试
```
或
```
GET /node/queryByName?name=测试&exactMatch=false
```

**匹配结果：**
- "测试节点"
- "这是测试"
- "测试"
- 所有包含"测试"的节点名称

#### 2. 精确匹配
当 `exactMatch=true` 时，使用精确匹配（等于）。

**示例请求：**
```
GET /node/queryByName?name=测试&exactMatch=true
```

**匹配结果：**
- 只返回名称完全等于"测试"的节点
- 不返回"测试节点"、"这是测试"等包含"测试"的节点

## 返回格式

```json
{
  "flag": true,
  "data": [
    {
      "id": 123,
      "labels": ["entity", "resource"],
      "properties": {
        "name": "测试",
        "type": "示例类型",
        ...
      }
    }
  ]
}
```

## 使用场景

### 场景1：搜索建议（使用模糊匹配）
用户输入"传感"，希望看到所有包含"传感"的节点，如"传感器"、"传感探测装置"等。

```
GET /node/queryByName?name=传感
```

### 场景2：精确查询（使用精确匹配）
需要查询名称为"传感器"的特定节点，不希望返回"红外传感器"、"传感器系统"等节点。

```
GET /node/queryByName?name=传感器&exactMatch=true
```

## 向后兼容性

此更新完全向后兼容，所有现有的 API 调用不需要修改即可正常工作，默认行为保持不变（模糊匹配）。

