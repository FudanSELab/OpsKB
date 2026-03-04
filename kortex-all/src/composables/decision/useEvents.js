import { ref } from 'vue';
import { getEventList, getEventDetail } from '../../api/eventAPI.js';

/**
 * 事件管理组合式函数
 */
export function useEvents() {
  const eventList = ref([]);
  const currentEventId = ref(0);
  const currentEventDetail = ref(null);
  const pagination = ref({
    current: 1,
    pageSize: 20,
    total: 0,
  });
  const loading = ref(false);
  const detailLoading = ref(false);
  const error = ref(null);

  /**
   * 加载事件列表
   */
  const loadEventList = async () => {
    loading.value = true;
    error.value = null;
    try {
      const response = await getEventList(pagination.value.current, pagination.value.pageSize);
      if (response.code === 200 || response.code === 0) {
        const dataList = Array.isArray(response.data)
          ? response.data
          : response.data.records || response.data.data || response.data.content || [];
        eventList.value = dataList;

        pagination.value.current = parseInt(
          response.data.current || response.data.number || pagination.value.current,
        );
        pagination.value.pageSize = parseInt(
          response.data.size || response.data.pageSize || pagination.value.pageSize,
        );
        pagination.value.total = parseInt(response.data.total || response.data.totalElements || 0);

        // 如果当前没有选中的事件或选中的事件不在当前页面，默认选择第一个
        if (
          !currentEventId.value ||
          !eventList.value.find((e) => e.eventId === currentEventId.value)
        ) {
          if (eventList.value.length > 0) {
            currentEventId.value = eventList.value[0].eventId;
          }
        }
      } else {
        throw new Error(response.message || '获取事件列表失败');
      }
    } catch (err) {
      error.value = err.message;
      console.error('加载事件列表失败:', err);
    } finally {
      loading.value = false;
    }
  };

  /**
   * 加载事件详情
   */
  const loadEventDetail = async (eventId) => {
    detailLoading.value = true;
    error.value = null;
    try {
      const response = await getEventDetail(eventId);
      if (response.code === 200 || response.code === 0) {
        currentEventDetail.value = response.data;
        currentEventId.value = eventId;
      } else {
        throw new Error(response.message || '获取事件详情失败');
      }
    } catch (err) {
      error.value = err.message;
      console.error('加载事件详情失败:', err);
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
   * 重置事件状态
   */
  const resetEvents = () => {
    currentEventId.value = null;
    currentEventDetail.value = null;
    pagination.value.current = 1;
  };

  return {
    eventList,
    currentEventId,
    currentEventDetail,
    pagination,
    loading,
    detailLoading,
    error,
    loadEventList,
    loadEventDetail,
    changePage,
    resetEvents,
  };
}
