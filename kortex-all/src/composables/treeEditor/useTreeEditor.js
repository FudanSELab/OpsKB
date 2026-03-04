import { ref, computed } from 'vue';
import * as echarts from 'echarts';
import { getTree } from '../../api/decisionAPI.js';
import { convertToEChartsData } from '../../utils/treeDataUtils.js';
import { generateBatchInsertSQL } from '../../utils/sqlGenerator.js';

/**
 * 树编辑器组合式函数
 */
export function useTreeEditor() {
  // 数据状态
  const treeData = ref([]);
  const originalTreeData = ref([]);
  const currentScenarioId = ref(null);
  const currentScenarioName = ref('');
  const currentTreeVersion = ref('');
  const loading = ref(false);
  const error = ref(null);

  // 编辑状态
  const showEditDialog = ref(false);
  const editingNode = ref(null);
  const hasChanges = ref(false);
  const sqlStatements = ref([]);

  // 图表实例
  let chartInstance = null;

  /**
   * 计算是否有变更
   */
  const checkHasChanges = () => {
    hasChanges.value = JSON.stringify(treeData.value) !== JSON.stringify(originalTreeData.value);
  };

  /**
   * 加载决策树数据
   */
  const loadTreeData = async (scenarioId, treeVersion) => {
    loading.value = true;
    error.value = null;
    try {
      const response = await getTree(scenarioId, treeVersion);
      if (response.code === 200) {
        treeData.value = JSON.parse(JSON.stringify(response.data || []));
        originalTreeData.value = JSON.parse(JSON.stringify(response.data || []));
        currentScenarioId.value = scenarioId;
        currentTreeVersion.value = treeVersion;
        hasChanges.value = false;
        sqlStatements.value = [];
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
   * 获取下一个可用的treeId
   */
  const getNextTreeId = () => {
    if (treeData.value.length === 0) return 1;
    const maxId = Math.max(...treeData.value.map((node) => node.treeId));
    return maxId + 1;
  };

  /**
   * 初始化图表
   */
  const initChart = (containerElement) => {
    console.log('初始化图表，容器:', containerElement, '数据长度:', treeData.value.length);
    if (!containerElement || treeData.value.length === 0) {
      console.warn('容器不存在或数据为空，跳过初始化');
      return;
    }

    // 销毁旧实例
    if (chartInstance) {
      console.log('销毁旧图表实例');
      chartInstance.dispose();
      chartInstance = null;
    }

    // 创建新实例
    chartInstance = echarts.init(containerElement);
    console.log('图表实例已创建');

    // 渲染图表
    renderChart();

    // 绑定事件
    bindChartEvents();
  };

  /**
   * 渲染图表
   */
  const renderChart = () => {
    if (!chartInstance || treeData.value.length === 0) return;

    const treeRoot = convertToEChartsData(treeData.value, [], null);
    const treeOption = {
      tooltip: {
        trigger: 'item',
        triggerOn: 'mousemove',
        formatter: (params) => {
          const data = params.data;
          if (!data || data.nodeType === 'edgeLabel') return '';

          let html = `<div style="padding: 5px;">`;
          html += `<strong>节点ID:</strong> ${data.value}<br/>`;
          html += `<strong>类型:</strong> ${data.nodeType}<br/>`;

          if (data.strategyId) {
            html += `<strong>策略ID:</strong> ${data.strategyId}<br/>`;
          }

          if (data.conditions) {
            html += `<strong>条件:</strong><br/>`;
            Object.entries(data.conditions).forEach(([key, value]) => {
              if (value !== null && value !== undefined) {
                html += `&nbsp;&nbsp;${key}: ${JSON.stringify(value)}<br/>`;
              }
            });
          }

          html += `</div>`;
          return html;
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
          orient: 'horizontal',
          expandAndCollapse: false, // 禁用点击展开/收缩功能，避免与编辑功能冲突
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

    chartInstance.setOption(treeOption);
  };

  /**
   * 绑定图表事件
   */
  const bindChartEvents = () => {
    if (!chartInstance) {
      console.warn('图表实例不存在，无法绑定事件');
      return;
    }

    // 清除之前的所有事件监听
    chartInstance.off('click');
    chartInstance.off('dblclick');
    chartInstance.off('contextmenu');

    // 单击：编辑节点
    chartInstance.on('click', (params) => {
      console.log('点击节点:', params);
      if (params.data && params.data.nodeType && params.data.nodeType !== 'edgeLabel') {
        // 阻止默认的展开/收缩行为
        if (params.event) {
          params.event.stop();
        }
        console.log('打开编辑对话框，节点ID:', params.data.value);
        openEditDialog(params.data.value);
      }
    });

    // 双击：删除节点
    chartInstance.on('dblclick', (params) => {
      console.log('双击节点:', params);
      if (params.data && params.data.nodeType && params.data.nodeType !== 'edgeLabel') {
        // 阻止默认的展开/收缩行为
        if (params.event) {
          params.event.stop();
        }
        if (confirm(`确定要删除节点 ${params.data.value} 及其所有子节点吗？`)) {
          deleteNode(params.data.value);
        }
      }
    });

    // 右键：添加子节点
    chartInstance.on('contextmenu', (params) => {
      console.log('右键点击节点:', params);
      if (params.data && params.data.nodeType && params.data.nodeType !== 'edgeLabel') {
        // 阻止浏览器默认右键菜单
        if (params.event && params.event.event) {
          params.event.event.preventDefault();
          params.event.event.stopPropagation();
        }

        const parentNode = treeData.value.find((n) => n.treeId === params.data.value);
        if (parentNode && parentNode.nodeType !== 'node') {
          // 只有根节点和分支节点才能添加子节点，直接添加
          console.log('在节点', params.data.value, '下添加子节点');
          addChildNode(params.data.value, 'branch');
        } else if (parentNode && parentNode.nodeType === 'node') {
          alert('叶子节点（策略节点）不能添加子节点');
        }
      }
    });

    console.log('图表事件已绑定');
  };

  /**
   * 打开编辑对话框
   */
  const openEditDialog = (treeId) => {
    console.log('openEditDialog 被调用，treeId:', treeId);
    const node = treeData.value.find((n) => n.treeId === treeId);
    if (node) {
      console.log('找到节点:', node);
      editingNode.value = JSON.parse(JSON.stringify(node));
      showEditDialog.value = true;
      console.log('对话框状态已设置为 true');
    } else {
      console.warn('未找到节点，treeId:', treeId);
    }
  };

  /**
   * 保存节点
   */
  const saveNode = (nodeData) => {
    const index = treeData.value.findIndex((n) => n.treeId === nodeData.treeId);

    if (index !== -1) {
      // 更新现有节点
      treeData.value[index] = { ...nodeData };
    } else {
      // 新增节点
      const newTreeId = nodeData.treeId || getNextTreeId();
      const newNode = {
        ...nodeData,
        treeId: newTreeId,
        childIds: nodeData.childIds || [],
      };
      treeData.value.push(newNode);

      // 更新父节点的childIds
      if (newNode.parentId !== null) {
        const parent = treeData.value.find((n) => n.treeId === newNode.parentId);
        if (parent && !parent.childIds.includes(newTreeId)) {
          parent.childIds.push(newTreeId);
        }
      }
    }

    checkHasChanges();
    renderChart();
  };

  /**
   * 删除节点
   */
  const deleteNode = (treeId) => {
    const node = treeData.value.find((n) => n.treeId === treeId);
    if (!node) return;

    // 不能删除根节点
    if (node.nodeType === 'tree') {
      alert('不能删除根节点');
      return;
    }

    // 递归收集所有要删除的节点ID
    const nodesToDelete = [treeId];
    const collectChildrenIds = (id) => {
      const n = treeData.value.find((node) => node.treeId === id);
      if (n && n.childIds) {
        n.childIds.forEach((childId) => {
          nodesToDelete.push(childId);
          collectChildrenIds(childId);
        });
      }
    };
    collectChildrenIds(treeId);

    // 从父节点的childIds中移除
    if (node.parentId !== null) {
      const parent = treeData.value.find((n) => n.treeId === node.parentId);
      if (parent) {
        parent.childIds = parent.childIds.filter((id) => id !== treeId);
      }
    }

    // 删除节点
    treeData.value = treeData.value.filter((n) => !nodesToDelete.includes(n.treeId));

    checkHasChanges();
    renderChart();
  };

  /**
   * 添加子节点
   */
  const addChildNode = (parentId, nodeType = 'branch') => {
    const newTreeId = getNextTreeId();
    const newNode = {
      treeId: newTreeId,
      nodeType: nodeType,
      parentId: parentId,
      childIds: [],
      conditions: null,
      strategyId: null,
      treeVersion: currentTreeVersion.value,
      scenarioId: currentScenarioId.value,
    };

    treeData.value.push(newNode);

    // 更新父节点的childIds
    const parent = treeData.value.find((n) => n.treeId === parentId);
    if (parent) {
      parent.childIds.push(newTreeId);
    }

    checkHasChanges();
    renderChart();

    // 打开编辑对话框
    openEditDialog(newTreeId);
  };

  /**
   * 生成SQL语句（生成整棵树的INSERT语句）
   */
  const generateSQL = () => {
    sqlStatements.value = generateBatchInsertSQL(
      treeData.value,
      currentTreeVersion.value,
      currentScenarioId.value,
    );
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

  return {
    // 数据
    treeData,
    currentScenarioId,
    currentScenarioName,
    currentTreeVersion,
    loading,
    error,
    hasChanges,
    sqlStatements,

    // 编辑状态
    showEditDialog,
    editingNode,

    // 方法
    loadTreeData,
    initChart,
    renderChart,
    resizeChart,
    disposeChart,
    saveNode,
    deleteNode,
    addChildNode,
    openEditDialog,
    generateSQL,
  };
}
