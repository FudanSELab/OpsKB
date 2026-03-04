<template>
  <div class="w-72 hidden md:block">
    <div class="h-[calc(100vh-96px)] bg-blue-50 my-4 rounded-md flex flex-col">
      <div class="px-2 pt-3">
        <div role="tablist" class="tabs tabs-boxed w-full">
          <a
            role="tab"
            class="tab tab-sm"
            :class="{ 'tab-active': activeTab === 'samples' }"
            @click="$emit('tabChange', 'samples')"
          >
            样本列表
          </a>
          <a
            role="tab"
            class="tab tab-sm"
            :class="{ 'tab-active': activeTab === 'events' }"
            @click="$emit('tabChange', 'events')"
          >
            事件列表
          </a>
        </div>
        <div class="flex items-center justify-center py-2">
          <p class="text-sm font-bold text-blue-500" v-if="scenarioName">
            {{ '(' + scenarioName + ')' }}
          </p>
        </div>
      </div>

      <div class="flex-1 overflow-y-auto">
        <div class="px-2">
          <!-- 样本列表 -->
          <div v-if="activeTab === 'samples'">
            <div
              v-if="!treeVersion && sampleList.length === 0 && !sampleLoading"
              class="flex items-center justify-center h-[300px]"
            >
              <div class="text-center text-gray-400">
                <svg
                  class="w-12 h-12 mx-auto mb-2 text-gray-300"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="1.5"
                    d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
                  ></path>
                </svg>
                <p class="text-sm font-medium">暂无样本数据</p>
                <p class="text-xs mt-1">请先选择策略树版本</p>
              </div>
            </div>
            <div v-else-if="sampleLoading" class="flex justify-center py-4">
              <span class="loading loading-spinner loading-md"></span>
            </div>
            <div v-else-if="sampleError" class="text-red-500 text-center py-4">
              {{ sampleError }}
            </div>
            <div
              v-else-if="sampleList.length === 0 && treeVersion"
              class="flex items-center justify-center h-full"
            >
              <div class="text-gray-500 text-center">
                <div class="flex flex-col items-center gap-3">
                  <svg
                    class="w-12 h-12 text-gray-400"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      stroke-linecap="round"
                      stroke-linejoin="round"
                      stroke-width="2"
                      d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4"
                    ></path>
                  </svg>
                  <p class="font-medium text-gray-600">暂无样本数据</p>
                  <p class="text-xs text-gray-500">当前策略树版本下没有样本记录</p>
                </div>
              </div>
            </div>
            <div v-else class="flex flex-col gap-1">
              <div
                v-for="sample in sampleList"
                :key="sample.sampleId"
                class="flex flex-col hover:bg-blue-100 px-4 py-1 rounded-md cursor-pointer"
                :class="{ 'bg-blue-200': currentSampleId === sample.sampleId }"
                @click="$emit('selectSample', sample.sampleId)"
              >
                <div class="flex items-center justify-between">
                  <div class="flex flex-col">
                    <p class="text-sm">样本{{ sample.sampleId }}</p>
                    <p class="text-xs text-gray-500">事件{{ sample.eventId }}</p>
                    <p class="text-xs text-gray-500">
                      {{ new Date(sample.updatedAt).toLocaleString() }}
                    </p>
                  </div>
                  <span class="badge badge-sm badge-success">已处理</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 事件列表 -->
          <div v-if="activeTab === 'events'">
            <div
              v-if="!scenarioId && eventList.length === 0 && !eventLoading"
              class="flex items-center justify-center h-[300px]"
            >
              <div class="text-center text-gray-400">
                <svg
                  class="w-12 h-12 mx-auto mb-2 text-gray-300"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="1.5"
                    d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"
                  ></path>
                </svg>
                <p class="text-sm font-medium">暂无事件数据</p>
                <p class="text-xs mt-1">请先选择场景</p>
              </div>
            </div>
            <div v-else-if="eventLoading" class="flex justify-center py-4">
              <span class="loading loading-spinner loading-md"></span>
            </div>
            <div v-else-if="eventError" class="text-red-500 text-center py-4">
              {{ eventError }}
            </div>
            <div
              v-else-if="eventList.length === 0 && scenarioId"
              class="flex items-center justify-center h-full"
            >
              <div class="text-center text-gray-400">
                <svg
                  class="w-12 h-12 mx-auto mb-2 text-gray-300"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="1.5"
                    d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4"
                  ></path>
                </svg>
                <p class="text-sm font-medium">暂无事件数据</p>
                <p class="text-xs mt-1">当前场景下没有事件记录</p>
              </div>
            </div>
            <div v-else class="flex flex-col gap-1">
              <div
                v-for="event in eventList"
                :key="event.eventId"
                class="flex flex-col hover:bg-blue-100 px-4 py-1 rounded-md cursor-pointer"
                :class="{ 'bg-blue-200': currentEventId === event.eventId }"
                @click="$emit('selectEvent', event.eventId)"
              >
                <div class="flex items-center justify-between">
                  <div class="flex items-center">
                    <p class="text-sm font-medium">事件{{ event.eventId }}</p>
                  </div>
                  <div class="flex flex-col items-end">
                    <p class="text-xs text-gray-500">
                      目标数: {{ event.targetCount || event.targetInfo?.target_number || 0 }}
                    </p>
                    <p class="text-xs text-gray-500">
                      资源数:
                      {{ event.resourceCount || event.resourceInfo?.resource_number || 0 }}
                    </p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 分页控件 -->
      <div class="flex justify-between items-center my-3 px-2" v-if="showPagination">
        <div class="text-xs text-gray-500">共 {{ currentPagination.total }} 条</div>
        <div class="flex gap-1">
          <button
            class="btn btn-xs"
            :disabled="currentPagination.current <= 1"
            @click="$emit('pageChange', currentPagination.current - 1)"
          >
            上一页
          </button>
          <span class="text-xs self-center px-2">
            {{ currentPagination.current }}/{{ maxPage }}
          </span>
          <button
            class="btn btn-xs"
            :disabled="currentPagination.current >= maxPage"
            @click="$emit('pageChange', currentPagination.current + 1)"
          >
            下一页
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  activeTab: {
    type: String,
    default: 'samples',
  },
  scenarioName: {
    type: String,
    default: '',
  },
  scenarioId: {
    type: Number,
    default: null,
  },
  treeVersion: {
    type: String,
    default: '',
  },
  sampleList: {
    type: Array,
    default: () => [],
  },
  currentSampleId: {
    type: Number,
    default: null,
  },
  sampleLoading: {
    type: Boolean,
    default: false,
  },
  sampleError: {
    type: String,
    default: null,
  },
  samplePagination: {
    type: Object,
    default: () => ({ current: 1, pageSize: 20, total: 0 }),
  },
  eventList: {
    type: Array,
    default: () => [],
  },
  currentEventId: {
    type: Number,
    default: null,
  },
  eventLoading: {
    type: Boolean,
    default: false,
  },
  eventError: {
    type: String,
    default: null,
  },
  eventPagination: {
    type: Object,
    default: () => ({ current: 1, pageSize: 20, total: 0 }),
  },
  currentSampleType: {
    type: String,
    default: 'training',
  },
});

defineEmits([
  'tabChange',
  'selectSample',
  'selectEvent',
  'pageChange',
  'sampleTypeChange',
]);

const currentPagination = computed(() => {
  if (props.activeTab === 'samples') {
    return props.samplePagination;
  } else if (props.activeTab === 'events') {
    return props.eventPagination;
  }
  return { current: 1, pageSize: 20, total: 0 };
});

const maxPage = computed(() => {
  const total = parseInt(currentPagination.value.total) || 0;
  const pageSize = parseInt(currentPagination.value.pageSize) || 10;
  return Math.ceil(total / pageSize);
});

const showPagination = computed(() => {
  return (
    (props.activeTab === 'samples' && props.samplePagination.total > 0) ||
    (props.activeTab === 'events' && props.eventPagination.total > 0)
  );
});
</script>
