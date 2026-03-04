import { ref, nextTick } from 'vue';
import * as echarts from 'echarts';
import { getTree } from '../../api/decisionAPI.js';
import { convertToEChartsData, findPathToStrategy } from '../../utils/treeDataUtils.js';

/**
 * 树图管理组合式函数
 */
export function useTreeChart() {
  const treeData = ref([]);
  const treeChartRef = ref(null);
  const loading = ref(false);
  const error = ref(null);
  let chartInstance = null;
  let fullscreenChartInstance = null;

  /**
   * 加载决策树数据
   */
  const loadTreeData = async (scenarioId, treeVersion) => {
    loading.value = true;
    error.value = null;
    try {
      const response = await getTree(scenarioId, treeVersion);
      if (response.code === 200) {
        treeData.value = response.data || [];
      } else {
        throw new Error(response.message || '获取决策树失败');
      }
    } catch (err) {
      error.value = err.message;
      console.error('加载决策树失败:', err);
    } finally {
      loading.value = false;
    }
  };

  /**
   * 渲染树图的通用函数
   * @param {Object} chartInstanceToUse - ECharts实例
   * @param {string|null} targetTreeId - 要高亮的树节点ID
   */
  const renderTreeChart = (chartInstanceToUse, targetTreeId = null) => {
    if (!chartInstanceToUse || !treeData.value.length) {
      console.warn('图表实例或树数据不存在');
      return;
    }

    // 首先找到高亮路径
    const tempTreeRoot = convertToEChartsData(treeData.value, [], null);
    const highlightPath = targetTreeId ? findPathToStrategy(tempTreeRoot, targetTreeId) : [];

    // 使用高亮路径重新转换树数据
    const treeRoot = convertToEChartsData(treeData.value, highlightPath, targetTreeId);
    const treeOption = {
      series: [
        {
          type: 'tree',
          data: [treeRoot],
          left: '5%',
          right: '5%',
          top: '5%',
          bottom: '5%',
          orient: 'horizontal',
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

    chartInstanceToUse.setOption(treeOption);
  };

  /**
   * 初始化树图
   */
  const initTreeChart = (targetTreeId = null) => {
    if (!treeChartRef.value || !treeData.value.length) return;

    if (!chartInstance) {
      chartInstance = echarts.init(treeChartRef.value);
    }

    renderTreeChart(chartInstance, targetTreeId);
  };

  /**
   * 调整图表大小
   */
  const resizeChart = () => {
    if (chartInstance) {
      chartInstance.resize();
    }
  };

  /**
   * 销毁图表实例
   */
  const disposeChart = () => {
    if (chartInstance) {
      chartInstance.dispose();
      chartInstance = null;
    }
  };

  /**
   * 初始化全屏树图
   * @param {HTMLElement} containerElement - 全屏图表容器元素
   * @param {string|null} targetTreeId - 要高亮的树节点ID
   */
  const initFullscreenTreeChart = (containerElement, targetTreeId = null) => {
    if (!containerElement) {
      console.warn('全屏图表容器不存在');
      return;
    }

    if (!treeData.value || treeData.value.length === 0) {
      console.warn('树数据为空');
      return;
    }

    // 先销毁旧的全屏图表实例
    if (fullscreenChartInstance) {
      fullscreenChartInstance.dispose();
      fullscreenChartInstance = null;
    }

    // 创建新的全屏图表实例
    fullscreenChartInstance = echarts.init(containerElement);

    // 渲染图表
    renderTreeChart(fullscreenChartInstance, targetTreeId);
  };

  /**
   * 销毁全屏图表实例
   */
  const disposeFullscreenChart = () => {
    if (fullscreenChartInstance) {
      fullscreenChartInstance.dispose();
      fullscreenChartInstance = null;
    }
  };

  return {
    treeData,
    treeChartRef,
    loading,
    error,
    loadTreeData,
    initTreeChart,
    resizeChart,
    disposeChart,
    initFullscreenTreeChart,
    disposeFullscreenChart,
  };
}
