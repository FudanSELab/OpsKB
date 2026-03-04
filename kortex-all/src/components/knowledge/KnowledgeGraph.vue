<template>
  <div
    ref="chartContainer"
    class="w-full h-full min-h-[300px] bg-[#fafafa] rounded-lg border border-[#e8e8e8]"
  ></div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue';
import * as echarts from 'echarts';

// Props
const props = defineProps({
  selectedNode: {
    type: Object,
    default: null,
  },
  nodeRelations: {
    type: Array,
    default: () => [],
  },
  allNodes: {
    type: Array,
    default: () => [],
  },
  allRelations: {
    type: Array,
    default: () => [],
  },
  isOverviewMode: {
    type: Boolean,
    default: false,
  },
});

// Emits
const emit = defineEmits(['node-click']);

// Refs
const chartContainer = ref(null);
let chartInstance = null;
let updateTimer = null; // 用于防抖的计时器
let resizeObserver = null; // 用于监听容器尺寸变化

// 固定的三种主类别及其颜色（唯一的颜色定义源）
const MAIN_CATEGORIES = {
  entity: { name: 'entity', color: '#40a9ff', show: '实体' }, // 蓝色
  event: { name: 'event', color: '#73d13d', show: '事件' }, // 绿色
  model: { name: 'model', color: '#ff7875', show: '模型' }, // 红色
  tree: { name: 'tree', color: '#ffc83d', show: '策略' }, // 紫色
};

// 从 MAIN_CATEGORIES 自动生成图谱类别配置（用于图例）
const CATEGORIES = Object.keys(MAIN_CATEGORIES).map((key) => ({
  name: key, // 内部使用英文进行数据匹配
  itemStyle: { color: MAIN_CATEGORIES[key].color },
}));

// 从节点labels中提取主类别（entity/event/model）
const getMainCategory = (labels) => {
  if (!labels || !Array.isArray(labels)) return null;

  for (const label of labels) {
    const lowerLabel = label.toLowerCase();
    if (
      lowerLabel === 'entity' ||
      lowerLabel === 'event' ||
      lowerLabel === 'model' ||
      lowerLabel === 'tree'
    ) {
      return lowerLabel;
    }
  }
  return null;
};

const createGraphNode = (node, options = {}) => {
  const {
    symbolSize = 40,
    fontSize = 12,
    fontWeight = 'normal',
    position = null,
    fixed = false,
    color = '#333',
  } = options;

  const nodeId = node.id.toString();
  const mainCategory = getMainCategory(node.labels) || 'entity';

  return {
    id: nodeId,
    name: node.properties?.name || `节点${node.id}`,
    symbolSize,
    value: node.properties?.name || `节点${node.id}`,
    itemStyle: {
      color: MAIN_CATEGORIES[mainCategory]?.color || '#40a9ff',
    },
    label: {
      fontSize,
      fontWeight,
      color,
    },
    category: mainCategory,
    categoryIndex: CATEGORIES.findIndex((cat) => cat.name === mainCategory),
    ...(position && { x: position.x, y: position.y }),
    ...(fixed && { fixed: true }),
    data: node,
  };
};

const createGraphLink = (relation, options = {}) => {
  const { showLabel = true, fontSize = 10, curveness = 0.1, lineWidth = 2, opacity = 1 } = options;

  const relationType = relation.relation?.type || '未知关系';

  return {
    source: relation.start.id.toString(),
    target: relation.end.id.toString(),
    value: relationType,
    label: {
      show: showLabel,
      formatter: relationType,
      fontSize,
    },
    lineStyle: {
      color: '#595959',
      width: lineWidth,
      curveness,
      opacity,
    },
    symbol: ['none', 'arrow'],
    symbolSize: [0, 8],
    emphasis: {
      lineStyle: {
        width: 4,
        opacity: 1,
      },
      label: {
        show: true,
        fontSize: fontSize + 2,
      },
      symbolSize: [0, 10],
    },
    data: relation,
  };
};

const prepareGraphData = () => {
  // 总览模式：显示所有节点和关系
  if (props.isOverviewMode && props.allNodes.length > 0) {
    return prepareOverviewModeData();
  }

  // 普通模式：显示选中节点及其关系
  return prepareNormalModeData();
};

/**
 * 准备总览模式的数据
 */
const prepareOverviewModeData = () => {
  const nodes = [];
  const links = [];
  const nodeIds = new Set();

  // 性能优化：限制显示的节点数量，避免渲染过多节点导致卡顿
  const MAX_NODES = 500;
  const nodesToDisplay = props.allNodes.slice(0, MAX_NODES);

  // 添加所有节点
  nodesToDisplay.forEach((node) => {
    const nodeIdStr = node.id.toString();
    if (!nodeIds.has(nodeIdStr)) {
      nodes.push(
        createGraphNode(node, {
          symbolSize: 40,
          fontSize: 12,
          color: '#333',
        }),
      );
      nodeIds.add(nodeIdStr);
    }
  });

  // 添加所有关系
  props.allRelations.forEach((relationData) => {
    const startId = relationData.start?.id?.toString();
    const endId = relationData.end?.id?.toString();

    // 确保起点和终点节点都存在
    if (startId && endId && nodeIds.has(startId) && nodeIds.has(endId)) {
      links.push(
        createGraphLink(relationData, {
          showLabel: false, // 总览模式下默认不显示关系标签
          fontSize: 8,
          curveness: 0,
          lineWidth: 2,
          opacity: 0.6,
        }),
      );
    }
  });

  return { nodes, links };
};

/**
 * 准备普通模式的数据
 */
const prepareNormalModeData = () => {
  if (!props.selectedNode || !props.nodeRelations) {
    return { nodes: [], links: [] };
  }

  const nodes = [];
  const links = [];
  const nodeIds = new Set();

  // 判断是否为tree节点的多跳查询（有额外的节点数据）
  const hasExtraNodes = props.allNodes && props.allNodes.length > 0;

  // 添加选中的节点（中心节点）
  const centerNode = createGraphNode(props.selectedNode, {
    symbolSize: hasExtraNodes ? 50 : 40,
    fontSize: hasExtraNodes ? 16 : 14,
    fontWeight: 'bold',
    position: hasExtraNodes ? null : { x: 0, y: 0 },
    fixed: hasExtraNodes ? false : true,
  });
  nodes.push(centerNode);
  nodeIds.add(centerNode.id);

  if (hasExtraNodes) {
    // Tree节点模式：使用queryGraph返回的所有节点
    props.allNodes.forEach((node) => {
      const nodeIdStr = node.id.toString();
      if (!nodeIds.has(nodeIdStr)) {
        nodes.push(
          createGraphNode(node, {
            symbolSize: 30,
            fontSize: 11,
            color: '#333',
          }),
        );
        nodeIds.add(nodeIdStr);
      }
    });

    // 添加所有关系
    props.nodeRelations.forEach((relation) => {
      if (relation.relation) {
        links.push(
          createGraphLink(relation, {
            showLabel: true,
            fontSize: 10,
            curveness: 0.05,
            lineWidth: 2,
          }),
        );
      }
    });
  } else {
    // 普通节点模式：使用环形布局
    const radius = 150;
    props.nodeRelations.forEach((relation, index) => {
      const angle = (2 * Math.PI * index) / props.nodeRelations.length;
      const position = {
        x: Math.cos(angle) * radius,
        y: Math.sin(angle) * radius,
      };

      // 添加起始节点（如果不是中心节点）
      if (relation.start && !nodeIds.has(relation.start.id.toString())) {
        nodes.push(
          createGraphNode(relation.start, {
            symbolSize: 25,
            fontSize: 10,
            position,
          }),
        );
        nodeIds.add(relation.start.id.toString());
      }

      // 添加结束节点（如果不是中心节点）
      if (relation.end && !nodeIds.has(relation.end.id.toString())) {
        nodes.push(
          createGraphNode(relation.end, {
            symbolSize: 25,
            fontSize: 10,
            position,
          }),
        );
        nodeIds.add(relation.end.id.toString());
      }

      // 添加关系边
      if (relation.relation) {
        links.push(
          createGraphLink(relation, {
            showLabel: true,
            fontSize: 10,
            curveness: 0.1,
          }),
        );
      }
    });
  }

  return { nodes, links };
};

// 生成图表配置
const getChartOption = (graphData = { nodes: [], links: [] }) => {
  // 判断是否为tree节点查询（有额外节点数据）
  const hasExtraNodes = !props.isOverviewMode && props.allNodes && props.allNodes.length > 0;

  return {
    title: {
      text: '知识总览',
      top: '2%',
      left: 'center',
      textStyle: {
        fontSize: 20,
        color: '#000000',
        fontWeight: 'bold',
      },
    },
    legend: {
      orient: 'horizontal',
      left: 'center',
      bottom: '5%',
      data: CATEGORIES.filter((cat) => cat.name === 'entity'),
      textStyle: {
        fontSize: 12,
      },
      formatter: (name) => {
        // 将英文类别名转换为中文显示
        return MAIN_CATEGORIES[name]?.show || name;
      },
    },
    animation: !props.isOverviewMode,
    animationDuration: props.isOverviewMode ? 0 : 1000,
    animationDurationUpdate: props.isOverviewMode ? 300 : 500,
    animationEasingUpdate: 'quinticInOut',
    thumbnail: {
      show: true,
      right: 10,
      bottom: 10,
      width: '20%',
      height: '20%',
      itemStyle: {
        borderColor: '#fff',
        borderRadius: 10,
        borderWidth: 0,
        shadowColor: 'rgba(0, 0, 0, 0.2)',
        shadowBlur: 10,
      },
    },
    series: [
      {
        name: '知识图谱',
        type: 'graph',
        layout: 'force',
        data: graphData.nodes || [],
        links: graphData.links || [],
        categories: CATEGORIES,
        roam: true,
        draggable: true,
        zoom: props.isOverviewMode ? 0.7 : 1,
        center: ['50%', '50%'],
        scaleLimit: {
          min: 0.3,
          max: 3,
        },
        focusNodeAdjacency: true,
        itemStyle: {
          borderColor: '#fff',
          borderWidth: props.isOverviewMode ? 0 : 1,
          shadowBlur: props.isOverviewMode ? 0 : 10,
          shadowColor: props.isOverviewMode ? 'transparent' : 'rgba(0, 0, 0, 0.3)',
        },
        label: {
          show: true,
          position: 'inside',
          formatter: '{b}',
          fontSize: 10,
        },
        lineStyle: {
          color: 'source',
          curveness: props.isOverviewMode ? 0 : 0.1,
          opacity: props.isOverviewMode ? 0.6 : 1,
        },
        edgeSymbol: ['none', 'arrow'],
        edgeSymbolSize: [0, props.isOverviewMode ? 6 : 8],
        force: {
          repulsion: props.isOverviewMode ? 300 : hasExtraNodes ? 800 : 1000,
          gravity: props.isOverviewMode ? 0.1 : hasExtraNodes ? 0.15 : 0.2,
          edgeLength: props.isOverviewMode ? [20, 80] : hasExtraNodes ? [80, 150] : [50, 200],
          layoutAnimation: true,
          friction: props.isOverviewMode ? 0.8 : 0.6,
        },
        emphasis: {
          focus: 'none',
          label: {
            show: true,
          },
          lineStyle: {
            width: 4,
          },
          itemStyle: {
            shadowBlur: 10,
            shadowColor: 'rgba(0, 0, 0, 0.5)',
          },
          edgeSymbolSize: [0, 10],
        },
      },
    ],
  };
};

// 初始化图表
const initChart = async () => {
  if (!chartContainer.value || chartInstance) return;

  await nextTick();

  chartInstance = echarts.init(chartContainer.value);

  // 使用统一的配置生成函数，初始化时不传数据
  chartInstance.setOption(getChartOption());

  // 添加点击事件
  chartInstance.on('click', (params) => {
    if (params.dataType === 'node') {
      emit('node-click', params.data.data);
    }
  });

  // 窗口大小变化时重新调整图表
  const resizeHandler = () => {
    if (chartInstance) {
      chartInstance.resize();
    }
  };
  window.addEventListener('resize', resizeHandler);

  // 存储清理函数
  chartInstance._resizeHandler = resizeHandler;

  // 使用 ResizeObserver 监听容器尺寸变化
  resizeObserver = new ResizeObserver(() => {
    if (chartInstance) {
      chartInstance.resize();
    }
  });
  resizeObserver.observe(chartContainer.value);
};

// 更新图表数据
const updateChart = () => {
  if (!chartInstance) return;

  const graphData = prepareGraphData();

  // 数据验证：确保graphData有效
  if (!graphData || !graphData.nodes || !graphData.links) {
    console.warn('图谱数据无效，跳过更新');
    return;
  }

  // 使用统一的配置生成函数，传入实际数据
  chartInstance.setOption(getChartOption(graphData), {
    notMerge: true, // 强制替换而不是合并
    lazyUpdate: props.isOverviewMode, // 总览模式使用延迟更新
  });

  console.log('图表已更新');
};

// 防抖更新图表，避免频繁更新导致卡顿
const debouncedUpdateChart = () => {
  if (updateTimer) {
    clearTimeout(updateTimer);
  }

  updateTimer = setTimeout(
    () => {
      updateChart();
    },
    props.isOverviewMode ? 100 : 0,
  ); // 总览模式下延迟100ms更新
};

// 监听isOverviewMode变化，需要重新调整图表尺寸
watch(
  () => props.isOverviewMode,
  async () => {
    // 等待DOM更新完成后再调整图表尺寸
    await nextTick();
    if (chartInstance) {
      chartInstance.resize();
    }
    // 然后更新图表数据
    debouncedUpdateChart();
  },
);

// 监听其他属性变化
watch(
  [
    () => props.selectedNode,
    () => props.nodeRelations,
    () => props.allNodes,
    () => props.allRelations,
  ],
  () => {
    // 使用防抖更新，避免频繁更新
    debouncedUpdateChart();
  },
  { deep: true },
);

// 生命周期
onMounted(async () => {
  await initChart();
  updateChart();
});

onUnmounted(() => {
  // 清理防抖计时器
  if (updateTimer) {
    clearTimeout(updateTimer);
  }

  // 清理 ResizeObserver
  if (resizeObserver) {
    resizeObserver.disconnect();
    resizeObserver = null;
  }

  if (chartInstance) {
    if (chartInstance._resizeHandler) {
      window.removeEventListener('resize', chartInstance._resizeHandler);
    }
    chartInstance.dispose();
    chartInstance = null;
  }
});

// 暴露方法
defineExpose({
  refreshChart: updateChart,
  resizeChart: () => {
    if (chartInstance) {
      chartInstance.resize();
    }
  },
});
</script>

<style scoped></style>
