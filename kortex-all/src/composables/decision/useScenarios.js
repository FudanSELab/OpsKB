import { ref } from 'vue';
import { getScenarios } from '../../api/decisionAPI.js';

/**
 * 场景管理组合式函数
 */
export function useScenarios() {
  const scenarios = ref([]);
  const currentScenarioId = ref(1);
  const currentScenarioName = ref('');
  const currentTreeVersion = ref('');
  const loading = ref(false);
  const error = ref(null);

  /**
   * 加载场景列表
   */
  const loadScenarios = async () => {
    loading.value = true;
    error.value = null;
    try {
      const response = await getScenarios();
      if (response.code === 200) {
        scenarios.value = response.data || [];
      } else {
        throw new Error(response.message || '获取场景列表失败');
      }
    } catch (err) {
      error.value = err.message;
      console.error('加载场景列表失败:', err);
    } finally {
      loading.value = false;
    }
  };

  /**
   * 设置当前场景和版本
   */
  const setCurrentScenarioAndVersion = (scenarioId, version) => {
    currentScenarioId.value = scenarioId;
    currentTreeVersion.value = version;

    const scenario = scenarios.value.find((s) => s.scenarioId === scenarioId);
    if (scenario) {
      currentScenarioName.value = scenario.scenarioName;
    }
  };

  return {
    scenarios,
    currentScenarioId,
    currentScenarioName,
    currentTreeVersion,
    loading,
    error,
    loadScenarios,
    setCurrentScenarioAndVersion,
  };
}
