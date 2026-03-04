<template>
  <dialog class="modal" :class="{ 'modal-open': show }">
    <div class="modal-box max-w-lg">
      <h3 class="font-bold text-lg mb-4 text-center">正在生成知识...</h3>

      <!-- 生成进度信息 -->
      <div class="bg-base-200 rounded-lg p-4">
        <div class="flex items-center gap-3">
          <span class="loading loading-spinner loading-sm text-primary"></span>
          <span class="text-sm font-medium text-gray-700">{{ statusText }}</span>
        </div>
      </div>
    </div>
  </dialog>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  show: {
    type: Boolean,
    default: false,
  },
  type: {
    type: String,
    default: 'text', // 'text', 'image', 'file'
  },
});

const statusText = computed(() => {
  switch (props.type) {
    case 'text':
      return '正在从文本中提取知识...';
    case 'image':
      return '正在从图片中提取知识...';
    case 'file':
      return '正在从文件中提取知识...';
    default:
      return '正在生成知识...';
  }
});
</script>

<style scoped></style>
