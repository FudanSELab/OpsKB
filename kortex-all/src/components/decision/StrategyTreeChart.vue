<template>
  <!-- 策略树可视化 -->
  <div class="bg-white rounded-md mb-4 border border-gray-300 relative">
    <!-- 折叠标题栏 -->
    <div
      class="flex items-center justify-between p-3 border-b border-gray-200 cursor-pointer hover:bg-gray-50 transition-colors"
      @click="isCollapsed = !isCollapsed"
    >
      <div class="flex items-center gap-2">
        <h5 class="font-medium text-gray-800">策略树</h5>
        <div class="badge badge-xs badge-info">{{ treeVersion }}</div>
        <span class="text-xs text-gray-500">(鼠标滚轮缩放 · 拖拽移动)</span>
      </div>
      <div class="flex items-center gap-1">
        <svg
          class="w-4 h-4 transform transition-transform duration-200"
          :class="{ 'rotate-180': isCollapsed }"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
        >
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M19 9l-7 7-7-7"
          ></path>
        </svg>
      </div>
    </div>

    <!-- 图表内容区域 -->
    <div
      class="transition-all duration-300 ease-in-out overflow-hidden"
      :class="isCollapsed ? 'h-0' : 'h-[300px]'"
    >
      <div class="p-4 h-full relative">
        <!-- 空状态提示 -->
        <div
          v-if="scenarioId === null || scenarioId === undefined || !treeVersion"
          class="absolute inset-0 flex items-center justify-center"
        >
          <div class="text-center text-gray-400">
            <svg
              class="w-16 h-16 mx-auto mb-3 text-gray-300"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="1.5"
                d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
              ></path>
            </svg>
            <p class="text-base font-medium">请先选择场景和版本</p>
            <p class="text-sm mt-1">从左侧选择一个决策场景</p>
          </div>
        </div>
        <!-- 加载状态 -->
        <div
          v-else-if="loading"
          class="absolute inset-0 flex items-center justify-center bg-white bg-opacity-75"
        >
          <div class="flex flex-col items-center gap-2">
            <span class="loading loading-spinner loading-lg"></span>
            <p class="text-sm text-gray-600">加载策略树中...</p>
          </div>
        </div>
        <!-- 错误状态 -->
        <div v-else-if="error" class="absolute inset-0 flex items-center justify-center">
          <div class="text-red-500 text-center">
            <p class="font-bold">策略树加载失败</p>
            <p class="text-sm">{{ error }}</p>
          </div>
        </div>
        <!-- 图表渲染区 -->
        <div ref="chartRef" class="w-full h-full"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, nextTick, onMounted, onUnmounted, computed } from 'vue';
import * as echarts from 'echarts';
import { getTree } from '../../api/decisionAPI.js';
import { strategyNameMap } from '../../config/strategyConfig.js';

const props = defineProps({
  scenarioId: {
    type: Number,
    required: false,
    default: null,
  },
  treeVersion: {
    type: String,
    required: false,
    default: '',
  },
  strategies: {
    type: Array,
    default: () => [],
  },
  selectedStrategyIds: {
    type: Array,
    default: () => [],
  },
  visible: {
    type: Boolean,
    default: false,
  },
});

// ECharts相关变量
const chartRef = ref(null);
let chart = null;

// 状态
const isCollapsed = ref(false);
const treeData = ref([]);
const loading = ref(false);
const error = ref(null);

// 获取所有搜索到的策略ID
const allStrategyIds = computed(() => {
  return props.strategies.map((s) => s.strategyId);
});

// 格式化条件对象
const formatConditions = (conditions) => {
  if (!conditions) return '';

  if (conditions.type && conditions.value !== undefined && conditions.value !== null) {
    return `${conditions.type}: ${conditions.value}`;
  }

  const nonNullEntries = Object.entries(conditions)
    .filter(([key, value]) => value !== null && value !== undefined)
    .map(([key, value]) => {
      const valueStr = Array.isArray(value) ? `[${value.join(', ')}]` : String(value);
      return `${key}: ${valueStr}`;
    });

  return nonNullEntries.join('\n');
};

// 查找从根节点到目标策略节点的路径
const findPathToStrategy = (root, targetStrategyId) => {
  if (!root || !targetStrategyId) return [];

  const findPath = (node, target, currentPath = []) => {
    if (!node) return null;

    const newPath = [...currentPath, node.value];

    if (node.nodeType === 'node' && node.strategyId === target) {
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

  const result = findPath(root, targetStrategyId);
  return result || [];
};

// 将扁平数据转换为ECharts树图数据格式
const convertToEChartsData = (data, highlightPaths = [], selectedStrategyIds = []) => {
  const nodeMap = new Map();

  // 合并所有高亮路径
  const allHighlightNodes = new Set();
  highlightPaths.forEach((path) => {
    path.forEach((nodeId) => allHighlightNodes.add(nodeId));
  });

  data.forEach((item) => {
    const isCurrentNodeHighlighted = allHighlightNodes.has(item.treeId);
    const isParentHighlighted = item.parentId !== null && allHighlightNodes.has(item.parentId);
    const isSelectedStrategy =
      item.nodeType === 'node' && selectedStrategyIds.includes(item.strategyId);
    const isInStrategyList =
      item.nodeType === 'node' && allStrategyIds.value.includes(item.strategyId);

    const shouldHighlightLine = isCurrentNodeHighlighted && isParentHighlighted;

    // 确定节点颜色和样式
    let nodeColor = '#409EFF';
    let borderColor = '#409EFF';
    let symbol = 'emptyCircle';

    if (isSelectedStrategy) {
      // UCB选中的策略节点：深蓝色实心圆
      nodeColor = '#1890FF';
      borderColor = '#1890FF';
      symbol = 'circle';
    } else if (isInStrategyList) {
      // 搜索到的策略节点：橙色实心圆
      nodeColor = '#FFA500';
      borderColor = '#FFA500';
      symbol = 'circle';
    } else if (isCurrentNodeHighlighted) {
      // 高亮路径上的节点：浅蓝色
      nodeColor = '#409EFF';
      symbol = 'circle';
      borderColor = '#409EFF';
    } else if (item.nodeType === 'node' && item.strategyId) {
      // 其他策略节点：绿色
      nodeColor = '#67C23A';
    }

    // 确定节点名称
    let nodeName = '';
    if (item.nodeType === 'node' && item.strategyId) {
      const strategyName = strategyNameMap[item.strategyId];
      nodeName = strategyName ? `${strategyName}` : `${item.strategyId}`;
    } else if (item.nodeType === 'tree') {
      nodeName = '策略树根节点';
    } else if (item.nodeType === 'branch' && item.conditions) {
      nodeName = formatConditions(item.conditions) || `分支${item.treeId}`;
    } else {
      nodeName = `节点${item.treeId}`;
    }

    nodeMap.set(item.treeId, {
      name: nodeName,
      value: item.treeId,
      nodeType: item.nodeType,
      strategyId: item.strategyId,
      conditions: item.conditions,
      children: [],
      parentId: item.parentId,
      symbol: symbol,
      symbolSize: item.nodeType === 'tree' ? 15 : 12,
      itemStyle: {
        color: nodeColor,
        borderColor: borderColor,
        borderWidth: 2,
      },
      label: {
        position: 'bottom',
        distance: 10,
        fontSize: item.nodeType === 'branch' ? 11 : 12,
        color: '#333',
        formatter: '{b}',
      },
      ...(shouldHighlightLine && {
        lineStyle: {
          color: '#1890FF',
          width: 3,
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
      if (parent) {
        parent.children.push(node);
      }
    }
  });

  return root;
};

// 加载策略树数据
const loadTreeData = async () => {
  if (props.scenarioId === null || props.scenarioId === undefined || !props.treeVersion) {
    return;
  }

  loading.value = true;
  error.value = null;

  try {
    const response = await getTree(props.scenarioId, props.treeVersion);
    if (response.code === 200) {
      treeData.value = response.data || [];
      nextTick(() => {
        initChart();
      });
    } else {
      throw new Error(response.message || '获取策略树失败');
    }
  } catch (err) {
    error.value = err.message;
    console.error('加载策略树失败:', err);
  } finally {
    loading.value = false;
  }
};

// 初始化图表
const initChart = () => {
  if (!chartRef.value || treeData.value.length === 0) return;

  if (chart) {
    chart.dispose();
  }

  chart = echarts.init(chartRef.value);

  // 查找所有搜索到的策略的路径
  const tempTreeRoot = convertToEChartsData(treeData.value, [], []);
  const highlightPaths = [];

  // 为搜索到的每个策略找到路径
  allStrategyIds.value.forEach((strategyId) => {
    const path = findPathToStrategy(tempTreeRoot, strategyId);
    if (path.length > 0) {
      highlightPaths.push(path);
    }
  });

  // 使用高亮路径重新转换树数据
  const treeRoot = convertToEChartsData(treeData.value, highlightPaths, props.selectedStrategyIds);

  const treeOption = {
    tooltip: {
      trigger: 'item',
      triggerOn: 'mousemove',
      formatter: function (params) {
        const data = params.data;
        const typeMap = {
          tree: '树根节点',
          branch: '条件分支',
          node: '策略节点',
        };

        let content = `<div style="padding: 8px;">
          <div style="margin-bottom: 4px;"><strong>节点类型:</strong> ${typeMap[data.nodeType] || data.nodeType}</div>`;

        if (data.strategyId) {
          const strategyName = strategyNameMap[data.strategyId];
          content += `<div style="margin-bottom: 4px;"><strong>策略ID:</strong> ${data.strategyId}</div>`;
          if (strategyName) {
            content += `<div style="margin-bottom: 4px;"><strong>策略名称:</strong> ${strategyName}</div>`;
          }
        }

        if (data.conditions) {
          content += `<div style="margin-bottom: 4px;"><strong>条件:</strong></div>`;
          Object.entries(data.conditions)
            .filter(([key, value]) => value !== null && value !== undefined)
            .forEach(([key, value]) => {
              const valueStr = Array.isArray(value) ? `[${value.join(', ')}]` : String(value);
              content += `<div style="margin-left: 12px; margin-bottom: 2px; font-size: 12px;">${key}: ${valueStr}</div>`;
            });
        }
        content += `<div style="margin-top: 4px; color: #999; font-size: 12px;">节点ID: ${data.value}</div>`;
        content += '</div>';
        return content;
      },
    },
    series: [
      {
        type: 'tree',
        data: [treeRoot],
        left: '5%',
        right: '5%',
        top: '5%',
        bottom: '5%',
        orient: 'vertical',
        expandAndCollapse: true,
        initialTreeDepth: 8,
        layout: 'orthogonal',
        roam: true,
        scaleLimit: {
          min: 0.3,
          max: 3,
        },
        lineStyle: {
          color: '#ccc',
          width: 1.5,
          curveness: 0.5,
        },
        emphasis: {
          focus: 'descendant',
          itemStyle: {
            color: '#c23531',
            borderColor: '#c23531',
            borderWidth: 2,
          },
          lineStyle: {
            color: '#c23531',
            width: 2,
          },
        },
      },
    ],
  };

  chart.setOption(treeOption);
};

// 窗口大小改变时重新调整图表
const resizeChart = () => {
  if (chart) {
    chart.resize();
  }
};

// 监听visible、scenarioId、treeVersion变化，加载数据
watch(
  [() => props.visible, () => props.scenarioId, () => props.treeVersion],
  ([newVisible, newScenarioId, newTreeVersion]) => {
    if (newVisible && newScenarioId !== null && newScenarioId !== undefined && newTreeVersion) {
      loadTreeData();
    }
  },
  { immediate: true },
);

// 监听策略数据变化，重新渲染树图
watch(
  [() => props.strategies, () => props.selectedStrategyIds],
  () => {
    if (chart && treeData.value.length > 0) {
      nextTick(() => {
        initChart();
      });
    }
  },
  { deep: true },
);

// 监听树图折叠状态变化
watch(isCollapsed, (collapsed) => {
  if (!collapsed) {
    nextTick(() => {
      setTimeout(() => {
        resizeChart();
      }, 300);
    });
  }
});

// 组件挂载时添加resize监听
onMounted(() => {
  window.addEventListener('resize', resizeChart);
});

// 组件卸载时清理
onUnmounted(() => {
  if (chart) {
    chart.dispose();
  }
  window.removeEventListener('resize', resizeChart);
});
</script>

<style scoped>
/* 组件样式 */
</style>
