import { strategyNameMap } from '../config/strategyConfig.js';

// 条件键名的中文映射
const conditionKeyMap = {
  sizeMax: '目标最大尺寸',
  speedAvg: '目标平均速度',
  speedMax: '目标最快速度',
  heightAvg: '目标平均高度',
  heightMax: '目标最大高度',
  targetNum: '目标数量',
  threatAvg: '目标平均威胁等级',
  threatMax: '目标最大威胁等级',
  visibility: '环境能见度',
};

/**
 * 获取条件中不为null的键名
 */
export const getConditionKeys = (conditions) => {
  if (!conditions) return [];
  return Object.entries(conditions)
    .filter(([key, value]) => value !== null && value !== undefined)
    .map(([key]) => key);
};

/**
 * 格式化条件值
 */
export const formatConditionValue = (value) => {
  if (Array.isArray(value)) {
    if (value.length === 1 && typeof value[0] === 'string') {
      return value[0];
    }
    return `[${value.join(', ')}]`;
  }
  return String(value);
};

/**
 * 获取branch节点应该显示的名称
 */
export const getBranchNodeName = (data, branchNode) => {
  if (!branchNode || !branchNode.childIds || branchNode.childIds.length === 0) {
    return '无条件';
  }

  const children = branchNode.childIds
    .map((childId) => data.find((item) => item.treeId === childId))
    .filter((child) => child !== undefined);

  const allConditionKeys = new Set();
  children.forEach((child) => {
    if (child.conditions) {
      const keys = getConditionKeys(child.conditions);
      keys.forEach((key) => allConditionKeys.add(key));
    }
  });

  if (allConditionKeys.size === 0) {
    return '无条件';
  }

  return Array.from(allConditionKeys)
    .map((key) => conditionKeyMap[key] || key)
    .join(', ');
};

/**
 * 获取边标签
 */
export const getEdgeLabel = (childNode) => {
  if (!childNode || !childNode.conditions) {
    return '';
  }

  const nonNullConditions = Object.entries(childNode.conditions)
    .filter(([key, value]) => value !== null && value !== undefined)
    .map(([key, value]) => formatConditionValue(value));

  return nonNullConditions.join(', ');
};

/**
 * 将扁平数据转换为ECharts树图数据格式
 */
export const convertToEChartsData = (data, highlightPath = [], targetTreeId = null) => {
  const nodeMap = new Map();
  let edgeNodeIdCounter = 100000;

  data.forEach((item) => {
    const isCurrentNodeHighlighted = highlightPath.includes(item.treeId);
    const isParentHighlighted = item.parentId !== null && highlightPath.includes(item.parentId);
    const isTargetNode = item.treeId === targetTreeId;
    const shouldHighlightLine = isCurrentNodeHighlighted && isParentHighlighted;

    let nodeColor = '#409EFF';
    let borderColor = '#c23531';
    let symbol = 'emptyCircle';

    if (isTargetNode) {
      nodeColor = '#1890FF';
      borderColor = '#1890FF';
      symbol = 'circle';
    } else if (isCurrentNodeHighlighted) {
      nodeColor = '#1890FF';
      symbol = 'circle';
      borderColor = '#1890FF';
    } else if (item.nodeType === 'node' && item.strategyId) {
      nodeColor = '#67C23A';
    }

    let nodeName = '';
    if (item.nodeType === 'node' && item.strategyId) {
      const strategyName = strategyNameMap[item.strategyId];
      nodeName = strategyName ? `${strategyName}` : `${item.strategyId}`;
    } else if (item.nodeType === 'tree') {
      nodeName = '根节点';
    } else if (item.nodeType === 'branch') {
      nodeName = getBranchNodeName(data, item);
    } else {
      nodeName = `节点${item.treeId}`;
    }

    const edgeLabel = getEdgeLabel(item);

    nodeMap.set(item.treeId, {
      name: nodeName,
      value: item.treeId,
      nodeType: item.nodeType,
      strategyId: item.strategyId,
      conditions: item.conditions,
      children: [],
      parentId: item.parentId,
      edgeLabelText: edgeLabel,
      symbol: symbol,
      symbolSize: item.nodeType === 'tree' ? 15 : 12,
      itemStyle: {
        color: nodeColor,
        borderColor: borderColor,
        borderWidth: 2,
      },
      label: {
        position: 'right',
        distance: 10,
        fontSize: 13,
        color: '#333',
        formatter: '{b}',
      },
      ...(shouldHighlightLine && {
        lineStyle: {
          color: '#1890FF',
          width: 2,
        },
      }),
    });
  });

  // 构建树结构
  let root = null;
  data.forEach((item) => {
    const node = nodeMap.get(item.treeId);
    if (item.parentId === null) {
      root = node;
    } else {
      const parent = nodeMap.get(item.parentId);
      if (parent && node) {
        const edgeNodeId = edgeNodeIdCounter++;
        const labelText = node.edgeLabelText || '无要求';

        const isParentHighlighted = highlightPath.includes(item.parentId);
        const isChildHighlighted = highlightPath.includes(item.treeId);
        const shouldHighlightEdge = isParentHighlighted && isChildHighlighted;

        const edgeNode = {
          name: labelText,
          value: edgeNodeId,
          nodeType: 'edgeLabel',
          children: [node],
          symbol: 'diamond',
          symbolSize: 6,
          itemStyle: {
            color: shouldHighlightEdge ? '#1890FF' : '#E6A23C',
            borderColor: shouldHighlightEdge ? '#1890FF' : '#E6A23C',
            borderWidth: 1,
            opacity: shouldHighlightEdge ? 1 : 0.8,
          },
          label: {
            position: 'top',
            distance: 3,
            fontSize: 13,
            color: shouldHighlightEdge ? '#1890FF' : '#E6A23C',
            fontWeight: 'bold',
            formatter: '{b}',
          },
          lineStyle: {
            color: shouldHighlightEdge ? '#1890FF' : '#ccc',
            width: shouldHighlightEdge ? 3 : 1,
          },
        };
        parent.children.push(edgeNode);
      }
    }
  });

  return root;
};

/**
 * 查找从根节点到目标节点的路径
 */
export const findPathToStrategy = (root, targetTreeId) => {
  if (!root || !targetTreeId) return [];

  const findPath = (node, target, currentPath = []) => {
    if (!node) return null;

    if (node.nodeType === 'edgeLabel') {
      if (node.children && node.children.length > 0) {
        for (const child of node.children) {
          const result = findPath(child, target, currentPath);
          if (result) {
            return result;
          }
        }
      }
      return null;
    }

    const newPath = [...currentPath, node.value];

    if (node.value === target) {
      return newPath;
    }

    if (node.children && node.children.length > 0) {
      for (const child of node.children) {
        const result = findPath(child, target, newPath);
        if (result) {
          return result;
        }
      }
    }

    return null;
  };

  const result = findPath(root, targetTreeId);
  return result || [];
};
