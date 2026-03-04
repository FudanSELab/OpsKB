import { ref } from 'vue';
import { strategyLocation, ucbSelect, planGenerate, trainOnce } from '../../api/decisionAPI.js';

/**
 * 决策流程管理组合式函数
 */
export function useDecisionFlow() {
  const decisionFlow = ref({
    step: 'none', // 'none' | 'eventDetail' | 'strategies' | 'ucbSelected' | 'plans' | 'completed'
    strategies: [],
    selectedStrategyIds: [],
    plans: [],
    finalResult: null,
  });

  const loading = ref({
    strategySearch: false,
    ucbSelect: false,
    planGenerate: false,
    trainOnce: false,
  });

  const error = ref({
    strategySearch: null,
    ucbSelect: null,
    planGenerate: null,
    trainOnce: null,
  });

  /**
   * 策略搜索
   */
  const handleStrategySearch = async (eventId, treeVersion) => {
    if (!eventId || !treeVersion) {
      alert('请先选择事件和策略树版本');
      return;
    }

    loading.value.strategySearch = true;
    error.value.strategySearch = null;
    try {
      const response = await strategyLocation(eventId, treeVersion);
      if (response.code === 200) {
        decisionFlow.value.strategies = response.data || [];
        decisionFlow.value.step = 'strategies';
      } else {
        throw new Error(response.message || '策略搜索失败');
      }
    } catch (err) {
      error.value.strategySearch = err.message;
      console.error('策略搜索失败:', err);
      alert('策略搜索失败: ' + err.message);
    } finally {
      loading.value.strategySearch = false;
    }
  };

  /**
   * UCB策略选择
   */
  const handleUcbSelect = async (eventId, treeVersion) => {
    if (!eventId || !treeVersion) {
      alert('请先选择事件和策略树版本');
      return;
    }

    loading.value.ucbSelect = true;
    error.value.ucbSelect = null;
    try {
      const response = await ucbSelect(eventId, treeVersion);
      if (response.code === 200) {
        decisionFlow.value.selectedStrategyIds = response.data || [];
        decisionFlow.value.step = 'ucbSelected';
      } else {
        throw new Error(response.message || 'UCB策略选择失败');
      }
    } catch (err) {
      error.value.ucbSelect = err.message;
      console.error('UCB策略选择失败:', err);
      alert('UCB策略选择失败: ' + err.message);
    } finally {
      loading.value.ucbSelect = false;
    }
  };

  /**
   * 生成调整方案
   */
  const handlePlanGenerate = async (eventId, treeVersion) => {
    if (!eventId || !treeVersion) {
      alert('请先选择事件和策略树版本');
      return;
    }

    loading.value.planGenerate = true;
    error.value.planGenerate = null;
    try {
      const response = await planGenerate(eventId, treeVersion);
      if (response.code === 200) {
        decisionFlow.value.plans = response.data || [];
        decisionFlow.value.step = 'plans';
      } else {
        throw new Error(response.message || '生成调整方案失败');
      }
    } catch (err) {
      error.value.planGenerate = err.message;
      console.error('生成调整方案失败:', err);
      alert('生成调整方案失败: ' + err.message);
    } finally {
      loading.value.planGenerate = false;
    }
  };

  /**
   * 策略优选执行
   */
  const handleTrainOnce = async (eventId, treeVersion, onSuccess) => {
    if (!eventId || !treeVersion) {
      alert('请先选择事件和策略树版本');
      return;
    }

    loading.value.trainOnce = true;
    error.value.trainOnce = null;
    try {
      const response = await trainOnce(eventId, treeVersion);
      if (response.code === 200) {
        decisionFlow.value.finalResult = response.data;
        decisionFlow.value.step = 'completed';
        if (onSuccess) {
          onSuccess();
        }
      } else {
        throw new Error(response.message || '策略优选执行失败');
      }
    } catch (err) {
      error.value.trainOnce = err.message;
      console.error('策略优选执行失败:', err);
      alert('策略优选执行失败: ' + err.message);
    } finally {
      loading.value.trainOnce = false;
    }
  };

  /**
   * 重置决策流程
   */
  const resetDecisionFlow = (step = 'none') => {
    decisionFlow.value = {
      step,
      strategies: [],
      selectedStrategyIds: [],
      plans: [],
      finalResult: null,
    };
  };

  return {
    decisionFlow,
    loading,
    error,
    handleStrategySearch,
    handleUcbSelect,
    handlePlanGenerate,
    handleTrainOnce,
    resetDecisionFlow,
  };
}
