<template>
  <div class="bg-white rounded-md mt-4 border border-gray-300 shrink-0 relative">
    <!-- 折叠标题栏 -->
    <div
      class="flex items-center justify-between p-3 border-b border-gray-200 hover:bg-gray-50 transition-colors"
    >
      <div class="flex items-center gap-2 flex-1 cursor-pointer" @click="toggleCollapse">
        <h3 class="font-medium text-gray-800">策略树</h3>
        <div class="badge badge-xs badge-info">{{ treeVersion }}</div>
        <span class="text-xs text-gray-500"> (鼠标滚轮缩放 · 拖拽移动) </span>
      </div>
      <div class="flex items-center gap-2">
        <!-- 全屏按钮 -->
        <button
          v-if="treeVersion"
          @click.stop="$emit('fullscreen')"
          class="btn btn-xs btn-ghost"
          title="全屏查看"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M4 8V4m0 0h4M4 4l5 5m11-1V4m0 0h-4m4 0l-5 5M4 16v4m0 0h4m-4 0l5-5m11 5l-5-5m5 5v-4m0 4h-4"
            ></path>
          </svg>
        </button>
        <!-- 折叠按钮 -->
        <div class="cursor-pointer" @click="toggleCollapse">
          <svg
            class="w-4 h-4 transform transition-transform duration-200"
            :class="{ 'rotate-180': collapsed }"
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
    </div>

    <!-- 图表内容区域 -->
    <div
      class="transition-all duration-300 ease-in-out overflow-hidden"
      :class="collapsed ? 'h-0' : 'h-[300px]'"
    >
      <div class="p-4 h-full relative">
        <!-- 空状态提示 -->
        <div v-if="!treeVersion" class="absolute inset-0 flex items-center justify-center">
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
            <p class="text-base font-medium">请先选择决策场景和版本</p>
            <p class="text-sm mt-1">从左侧选择一个决策场景和版本</p>
          </div>
        </div>
        <div
          v-else-if="loading"
          class="absolute inset-0 flex items-center justify-center bg-white bg-opacity-75"
        >
          <div class="flex flex-col items-center gap-2">
            <span class="loading loading-spinner loading-lg"></span>
            <p class="text-sm text-gray-600">加载决策树中...</p>
          </div>
        </div>
        <div v-else-if="error" class="absolute inset-0 flex items-center justify-center">
          <div class="text-red-500 text-center">
            <p class="font-bold">决策树加载失败</p>
            <p class="text-sm">{{ error }}</p>
            <button @click="$emit('retry')" class="btn btn-sm btn-primary mt-2">重试</button>
          </div>
        </div>
        <div ref="chartContainer" class="w-full h-full">
          <!-- ECharts树图将在这里渲染 -->
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';

defineProps({
  treeVersion: {
    type: String,
    default: '',
  },
  loading: {
    type: Boolean,
    default: false,
  },
  error: {
    type: String,
    default: null,
  },
});

const emit = defineEmits(['fullscreen', 'retry', 'update:collapsed']);

const collapsed = ref(false);
const chartContainer = ref(null);

const toggleCollapse = () => {
  collapsed.value = !collapsed.value;
  emit('update:collapsed', collapsed.value);
};

defineExpose({
  chartContainer,
  collapsed,
});
</script>
