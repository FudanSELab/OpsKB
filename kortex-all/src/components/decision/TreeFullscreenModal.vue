<template>
  <div v-if="visible" class="fixed inset-0 z-[999] flex flex-col" @click.self="$emit('close')">
    <!-- 标题栏 -->
    <div class="bg-gray-100 px-6 py-4 flex items-center justify-between">
      <div class="flex items-center gap-3">
        <h2 class="text-xl font-bold">策略树全屏视图</h2>
        <div class="badge badge-info">{{ treeVersion }}</div>
        <span class="text-sm">鼠标滚轮缩放 · 拖拽移动</span>
      </div>
      <button @click="$emit('close')" class="btn btn-sm btn-ghost hover:bg-gray-200">
        <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M6 18L18 6M6 6l12 12"
          ></path>
        </svg>
      </button>
    </div>

    <!-- 图表区域 -->
    <div class="flex-1">
      <div class="w-full h-full bg-white relative">
        <div
          v-if="loading"
          class="absolute inset-0 flex items-center justify-center bg-white bg-opacity-75 z-10"
        >
          <div class="flex flex-col items-center gap-2">
            <span class="loading loading-spinner loading-lg"></span>
            <p class="text-sm text-gray-600">加载决策树中...</p>
          </div>
        </div>
        <div v-else-if="error" class="absolute inset-0 flex items-center justify-center z-10">
          <div class="text-red-500 text-center">
            <p class="font-bold">决策树加载失败</p>
            <p class="text-sm">{{ error }}</p>
            <button @click="$emit('retry')" class="btn btn-sm btn-primary mt-2">重试</button>
          </div>
        </div>
        <div ref="fullscreenChartContainer" class="w-full h-full">
          <!-- ECharts树图将在这里渲染 -->
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';

defineProps({
  visible: {
    type: Boolean,
    default: false,
  },
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

defineEmits(['close', 'retry']);

const fullscreenChartContainer = ref(null);

defineExpose({
  fullscreenChartContainer,
});
</script>
