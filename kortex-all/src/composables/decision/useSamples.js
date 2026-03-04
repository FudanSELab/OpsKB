import { ref } from 'vue';
import {
  getSampleList,
  getSampleById,
  getFactSampleList,
  getFactSampleById,
} from '../../api/decisionAPI.js';

/**
 * 样本管理组合式函数
 */
export function useSamples() {
  const sampleList = ref([]);
  const currentSampleId = ref(0);
  const currentDecisionData = ref(null);
  const currentSampleType = ref('training'); // 'training' | 'fact'
  const pagination = ref({
    current: 1,
    pageSize: 20,
    total: 0,
  });
  const loading = ref(false);
  const detailLoading = ref(false);
  const error = ref(null);

  /**
   * 切换样本类型
   */
  const setSampleType = (type) => {
    if (currentSampleType.value !== type) {
      currentSampleType.value = type;
      resetSamples();
    }
  };

  /**
   * 加载样本列表
   */
  const loadSampleList = async (scenarioId, treeVersion) => {
    loading.value = true;
    error.value = null;
    try {
      const apiFunc =
        currentSampleType.value === 'fact' ? getFactSampleList : getSampleList;
      const response = await apiFunc(
        scenarioId,
        treeVersion,
        pagination.value.current,
        pagination.value.pageSize,
      );
      if (response.code === 200) {
        sampleList.value = response.data.data || [];
        pagination.value.current = parseInt(response.data.current) || 1;
        pagination.value.pageSize =
          parseInt(response.data.pageSize) || pagination.value.pageSize;
        pagination.value.total = parseInt(response.data.total) || 0;

        // 如果当前选中的样本不在当前页面，清空选中状态
        if (
          currentSampleId.value &&
          !sampleList.value.find((s) => s.sampleId === currentSampleId.value)
        ) {
          currentSampleId.value = null;
          currentDecisionData.value = null;
        }
      } else {
        throw new Error(response.message || '获取样本列表失败');
      }
    } catch (err) {
      error.value = err.message;
      console.error('加载样本列表失败:', err);
    } finally {
      loading.value = false;
    }
  };

  /**
   * 加载样本详情
   */
  const loadSampleDetail = async (sampleId) => {
    detailLoading.value = true;
    error.value = null;
    try {
      const apiFunc =
        currentSampleType.value === 'fact' ? getFactSampleById : getSampleById;
      const response = await apiFunc(sampleId);
      if (response.code === 200) {
        currentDecisionData.value = response.data;
        currentSampleId.value = sampleId;
      } else {
        throw new Error(response.message || '获取样本详情失败');
      }
    } catch (err) {
      error.value = err.message;
      console.error('加载样本详情失败:', err);
    } finally {
      detailLoading.value = false;
    }
  };

  /**
   * 切换页面
   */
  const changePage = (page) => {
    pagination.value.current = page;
  };

  /**
   * 重置样本状态
   */
  const resetSamples = () => {
    currentSampleId.value = null;
    currentDecisionData.value = null;
    pagination.value.current = 1;
  };

  return {
    sampleList,
    currentSampleId,
    currentDecisionData,
    pagination,
    loading,
    detailLoading,
    error,
    loadSampleList,
    loadSampleDetail,
    changePage,
    resetSamples,
    setSampleType,
    currentSampleType,
  };
}
