import { ref } from 'vue';
import { startTraining, stopTraining, getTrainingStatus } from '../../api/decisionAPI.js';

/**
 * 训练管理组合式函数
 */
export function useTraining() {
  const isTraining = ref(false);
  const trainingStatus = ref({
    running: false,
    currentStatus: '未启动',
    totalEvents: 0,
    processedEvents: 0,
    successCount: 0,
    failureCount: 0,
    currentBatch: 0,
    totalBatches: 0,
    batchSize: 0,
    progressPercentage: 0,
  });
  const error = ref(null);
  let trainingTimer = null;

  /**
   * 加载训练状态
   */
  const loadTrainingStatus = async (scenarioId) => {
    if (!scenarioId) return;

    try {
      const response = await getTrainingStatus(scenarioId);
      if (response.code === 200) {
        const data = response.data;
        trainingStatus.value = {
          running: data.running,
          currentStatus: data.currentStatus || '未启动',
          totalEvents: data.totalEvents || 0,
          processedEvents: data.processedEvents || 0,
          successCount: data.successCount || 0,
          failureCount: data.failureCount || 0,
          currentBatch: data.currentBatch || 0,
          totalBatches: data.totalBatches || 0,
          batchSize: data.batchSize || 0,
          progressPercentage: data.progressPercentage || 0,
        };
        isTraining.value = data.running;
      } else {
        throw new Error(response.message || '获取训练状态失败');
      }
    } catch (err) {
      error.value = err.message;
      console.error('获取训练状态失败:', err);
    }
  };

  /**
   * 开始训练
   */
  const handleStartTraining = async (scenarioId, treeVersion, onSuccess) => {
    try {
      error.value = null;
      const response = await startTraining(scenarioId, treeVersion);

      if (response.code === 200) {
        await loadTrainingStatus(scenarioId);
        startTrainingStatusPolling(scenarioId, onSuccess);
      } else {
        throw new Error(response.message || '开始训练失败');
      }
    } catch (err) {
      error.value = err.message;
      console.error('开始训练失败:', err);
      alert('开始训练失败: ' + err.message);
    }
  };

  /**
   * 停止训练
   */
  const handleStopTraining = async (scenarioId) => {
    try {
      error.value = null;
      const response = await stopTraining(scenarioId);

      if (response.code === 200) {
        stopTrainingStatusPolling();
        await loadTrainingStatus(scenarioId);
      } else {
        throw new Error(response.message || '停止训练失败');
      }
    } catch (err) {
      error.value = err.message;
      console.error('停止训练失败:', err);
      alert('停止训练失败: ' + err.message);
    }
  };

  /**
   * 开始定时查询训练状态
   */
  const startTrainingStatusPolling = (scenarioId, onComplete) => {
    stopTrainingStatusPolling();

    trainingTimer = setInterval(async () => {
      await loadTrainingStatus(scenarioId);

      if (!trainingStatus.value.running && trainingStatus.value.currentStatus === '训练完成') {
        stopTrainingStatusPolling();
        if (onComplete) {
          onComplete();
        }
      }
    }, 2000);
  };

  /**
   * 停止定时查询训练状态
   */
  const stopTrainingStatusPolling = () => {
    if (trainingTimer) {
      clearInterval(trainingTimer);
      trainingTimer = null;
    }
  };

  return {
    isTraining,
    trainingStatus,
    error,
    loadTrainingStatus,
    handleStartTraining,
    handleStopTraining,
    startTrainingStatusPolling,
    stopTrainingStatusPolling,
  };
}
