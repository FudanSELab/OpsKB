<template>
  <dialog class="modal" :class="{ 'modal-open': show }">
    <div class="modal-box max-w-lg">
      <h3 class="font-bold text-lg mb-4 text-center">正在消解知识冲突...</h3>
      <!-- 消解进度信息 -->
      <div class="bg-base-200 rounded-lg p-4">
        <!-- 知识库检索阶段 -->
        <div class="flex items-center gap-3 mb-3">
          <span
            v-if="phase === 'retrieval'"
            class="loading loading-spinner loading-sm text-primary"
          ></span>
          <span
            v-else
            class="flex items-center justify-center w-5 h-5 rounded-full bg-success text-white text-xs"
          >
            ✓
          </span>
          <span
            class="text-sm font-medium"
            :class="phase === 'retrieval' ? 'text-gray-700' : 'text-gray-500'"
          >
            正在进行知识库检索...
          </span>
        </div>

        <!-- 冲突消解分析阶段 -->
        <div class="flex items-center gap-3 mb-3">
          <span
            v-if="phase === 'analysis'"
            class="loading loading-spinner loading-sm text-warning"
          ></span>
          <span v-else class="w-5 h-5"></span>
          <span
            class="text-sm font-medium"
            :class="phase === 'analysis' ? 'text-gray-700' : 'text-gray-400'"
          >
            正在进行冲突消解分析...
          </span>
        </div>

        <div v-if="totalNodes > 0 && phase === 'analysis'">
          <div class="flex justify-between text-xs text-gray-600 mb-1">
            <span>处理进度</span>
            <span>{{ processedNodes }} / {{ totalNodes }}</span>
          </div>
          <progress
            class="progress progress-warning w-full"
            :value="processedNodes"
            :max="totalNodes"
          ></progress>
        </div>

        <!-- <div class="space-y-2 text-xs">
          <div class="flex items-center gap-2 text-gray-600">
            <span class="loading loading-spinner loading-xs"></span>
            <span>正在比对知识库中的相似节点...</span>
          </div>
          <div class="flex items-center gap-2 text-gray-600">
            <span class="loading loading-spinner loading-xs" style="animation-delay: 0.2s"></span>
            <span>正在分析节点属性差异...</span>
          </div>
          <div class="flex items-center gap-2 text-gray-600">
            <span class="loading loading-spinner loading-xs" style="animation-delay: 0.4s"></span>
            <span>正在生成合并策略...</span>
          </div>
        </div> -->
      </div>
    </div>
  </dialog>
</template>

<script setup>
const props = defineProps({
  show: {
    type: Boolean,
    default: false,
  },
  totalNodes: {
    type: Number,
    default: 0,
  },
  processedNodes: {
    type: Number,
    default: 0,
  },
  phase: {
    type: String,
    default: 'retrieval', // 'retrieval' | 'analysis'
  },
});
</script>

<style scoped></style>
