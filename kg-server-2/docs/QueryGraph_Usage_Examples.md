# 图查询接口使用示例

## 快速开始

本文档提供了 `/node/queryGraph` 接口的使用示例和最佳实践。

## 基础用法

### 使用 cURL 请求

#### 1. 查询一跳关系
```bash
curl -X GET "http://localhost:8052/node/queryGraph?name=传感器&type=1"
```

#### 2. 查询两跳关系
```bash
curl -X GET "http://localhost:8052/node/queryGraph?name=传感器&type=2"
```

#### 3. 查询全部关系
```bash
curl -X GET "http://localhost:8052/node/queryGraph?name=传感器&type=3"
```

### 使用 JavaScript (Fetch API)

```javascript
// 查询一跳关系
async function queryOneHopGraph(nodeName) {
  const response = await fetch(
    `http://localhost:8052/node/queryGraph?name=${encodeURIComponent(nodeName)}&type=1`
  );
  const result = await response.json();
  
  if (result.flag) {
    console.log('节点数量:', result.data.nodeCount);
    console.log('关系数量:', result.data.relationCount);
    console.log('节点列表:', result.data.nodes);
    console.log('关系列表:', result.data.relations);
  } else {
    console.error('查询失败:', result.data);
  }
}

// 查询两跳关系
async function queryTwoHopGraph(nodeName) {
  const response = await fetch(
    `http://localhost:8052/node/queryGraph?name=${encodeURIComponent(nodeName)}&type=2`
  );
  const result = await response.json();
  return result;
}

// 查询全部关系
async function queryAllGraph(nodeName) {
  const response = await fetch(
    `http://localhost:8052/node/queryGraph?name=${encodeURIComponent(nodeName)}&type=3`
  );
  const result = await response.json();
  return result;
}

// 使用示例
queryOneHopGraph('传感器');
```

### 使用 Python (requests)

```python
import requests

BASE_URL = "http://localhost:8052"

def query_graph(node_name, query_type):
    """
    查询图数据
    
    Args:
        node_name: 节点名称
        query_type: 查询类型 (1, 2, 或 3)
    
    Returns:
        查询结果字典
    """
    url = f"{BASE_URL}/node/queryGraph"
    params = {
        "name": node_name,
        "type": query_type
    }
    
    response = requests.get(url, params=params)
    result = response.json()
    
    if result["flag"]:
        print(f"查询成功！")
        print(f"节点数量: {result['data']['nodeCount']}")
        print(f"关系数量: {result['data']['relationCount']}")
        return result["data"]
    else:
        print(f"查询失败: {result['data']}")
        return None

# 使用示例
if __name__ == "__main__":
    # 查询一跳关系
    data = query_graph("传感器", 1)
    
    if data:
        print("\n节点列表:")
        for node in data["nodes"]:
            print(f"  - ID: {node['id']}, Name: {node['properties'].get('name')}")
        
        print("\n关系列表:")
        for rel in data["relations"]:
            print(f"  - {rel['type']}: {rel['startNodeId']} -> {rel['endNodeId']}")
```

### 使用 Java (Spring RestTemplate)

```java
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import com.xidian.kg.controller.util.Result;

public class GraphQueryExample {
    
    private static final String BASE_URL = "http://localhost:8052";
    private RestTemplate restTemplate = new RestTemplate();
    
    /**
     * 查询图数据
     */
    public Result queryGraph(String nodeName, Integer type) {
        String url = UriComponentsBuilder
            .fromHttpUrl(BASE_URL + "/node/queryGraph")
            .queryParam("name", nodeName)
            .queryParam("type", type)
            .toUriString();
        
        return restTemplate.getForObject(url, Result.class);
    }
    
    public static void main(String[] args) {
        GraphQueryExample example = new GraphQueryExample();
        
        // 查询一跳关系
        Result result = example.queryGraph("传感器", 1);
        
        if (result.isFlag()) {
            System.out.println("查询成功！");
            // 处理返回数据
        } else {
            System.out.println("查询失败: " + result.getData());
        }
    }
}
```

## 数据可视化示例

### 使用 D3.js 可视化图结构

```javascript
async function visualizeGraph(nodeName, type) {
  const response = await fetch(
    `http://localhost:8052/node/queryGraph?name=${encodeURIComponent(nodeName)}&type=${type}`
  );
  const result = await response.json();
  
  if (!result.flag) {
    console.error('查询失败:', result.data);
    return;
  }
  
  const graphData = {
    nodes: result.data.nodes.map(node => ({
      id: node.id,
      name: node.properties.name,
      labels: node.labels
    })),
    links: result.data.relations.map(rel => ({
      source: rel.startNodeId,
      target: rel.endNodeId,
      type: rel.type
    }))
  };
  
  // 使用 D3.js 绘制图
  renderGraph(graphData);
}

function renderGraph(data) {
  // D3.js 可视化代码
  console.log('图数据:', data);
  // 在这里添加 D3.js 绘图逻辑
}
```

### 使用 ECharts 可视化

```javascript
async function renderGraphWithECharts(nodeName, type) {
  const response = await fetch(
    `http://localhost:8052/node/queryGraph?name=${encodeURIComponent(nodeName)}&type=${type}`
  );
  const result = await response.json();
  
  if (!result.flag) {
    console.error('查询失败:', result.data);
    return;
  }
  
  const option = {
    title: {
      text: `${nodeName} 的关系图 (Type=${type})`
    },
    tooltip: {},
    series: [{
      type: 'graph',
      layout: 'force',
      data: result.data.nodes.map(node => ({
        id: node.id,
        name: node.properties.name,
        symbolSize: 30
      })),
      links: result.data.relations.map(rel => ({
        source: rel.startNodeId,
        target: rel.endNodeId,
        label: {
          show: true,
          formatter: rel.type
        }
      })),
      roam: true,
      force: {
        repulsion: 100
      }
    }]
  };
  
  const chart = echarts.init(document.getElementById('graph-container'));
  chart.setOption(option);
}
```

## 实际应用场景

### 场景 1: 知识图谱导航

```javascript
// 用户点击节点时加载其邻居节点
function onNodeClick(nodeName) {
  queryGraph(nodeName, 1).then(result => {
    if (result.flag) {
      updateGraphView(result.data);
    }
  });
}

// 展开节点（显示两跳关系）
function expandNode(nodeName) {
  queryGraph(nodeName, 2).then(result => {
    if (result.flag) {
      updateGraphView(result.data);
    }
  });
}
```

### 场景 2: 影响分析

```python
def analyze_node_impact(node_name):
    """分析节点的影响范围"""
    result = query_graph(node_name, 3)
    
    if result:
        impact_nodes = result["nodes"]
        impact_relations = result["relations"]
        
        print(f"\n节点 '{node_name}' 的影响分析:")
        print(f"  直接或间接影响的节点数: {len(impact_nodes)}")
        print(f"  涉及的关系数: {len(impact_relations)}")
        
        # 统计关系类型
        relation_types = {}
        for rel in impact_relations:
            rel_type = rel["type"]
            relation_types[rel_type] = relation_types.get(rel_type, 0) + 1
        
        print("\n  关系类型分布:")
        for rel_type, count in relation_types.items():
            print(f"    {rel_type}: {count}")
```

### 场景 3: 路径查找

```javascript
function findPath(startNode, targetNodeId, maxHops = 3) {
  return queryGraph(startNode, maxHops).then(result => {
    if (!result.flag) {
      return null;
    }
    
    // 检查目标节点是否在结果中
    const targetNode = result.data.nodes.find(n => n.id === targetNodeId);
    
    if (targetNode) {
      console.log(`在 ${maxHops} 跳内找到目标节点`);
      return {
        found: true,
        data: result.data
      };
    } else {
      console.log(`未在 ${maxHops} 跳内找到目标节点`);
      return {
        found: false
      };
    }
  });
}
```

## 性能优化建议

1. **限制查询深度**: 
   - Type=3 可能返回大量数据，建议根据实际需求选择 Type=1 或 Type=2
   
2. **分页加载**:
   - 对于大型图，考虑实现分页或懒加载机制
   
3. **缓存策略**:
   - 对于频繁查询的节点，可以在前端实现缓存
   
4. **增量加载**:
   - 先加载一跳关系，根据用户交互逐步加载更多数据

```javascript
// 增量加载示例
class GraphNavigator {
  constructor() {
    this.loadedNodes = new Set();
    this.graphData = { nodes: [], relations: [] };
  }
  
  async loadNodeNeighbors(nodeName) {
    if (this.loadedNodes.has(nodeName)) {
      return; // 已加载过
    }
    
    const result = await queryGraph(nodeName, 1);
    if (result.flag) {
      // 合并新数据到现有图
      this.mergeGraphData(result.data);
      this.loadedNodes.add(nodeName);
    }
  }
  
  mergeGraphData(newData) {
    // 去重合并节点
    const existingNodeIds = new Set(this.graphData.nodes.map(n => n.id));
    newData.nodes.forEach(node => {
      if (!existingNodeIds.has(node.id)) {
        this.graphData.nodes.push(node);
      }
    });
    
    // 去重合并关系
    const existingRelIds = new Set(this.graphData.relations.map(r => r.id));
    newData.relations.forEach(rel => {
      if (!existingRelIds.has(rel.id)) {
        this.graphData.relations.push(rel);
      }
    });
  }
}
```

## 错误处理

```javascript
async function robustQueryGraph(nodeName, type) {
  try {
    const response = await fetch(
      `http://localhost:8052/node/queryGraph?name=${encodeURIComponent(nodeName)}&type=${type}`
    );
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    
    const result = await response.json();
    
    if (!result.flag) {
      // 业务逻辑错误
      console.error('查询失败:', result.data);
      
      // 根据错误信息采取不同措施
      if (result.data.includes('未找到')) {
        alert('节点不存在，请检查节点名称');
      } else if (result.data.includes('type参数')) {
        alert('查询类型参数错误');
      }
      
      return null;
    }
    
    return result.data;
    
  } catch (error) {
    console.error('请求异常:', error);
    alert('网络错误，请稍后重试');
    return null;
  }
}
```

## 总结

本接口提供了灵活的图数据查询能力，通过不同的 type 参数可以满足各种应用场景的需求。合理使用查询类型和优化策略，可以构建高效的知识图谱应用。

