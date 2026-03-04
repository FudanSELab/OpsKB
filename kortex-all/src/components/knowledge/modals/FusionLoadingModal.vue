<template>
  <dialog class="modal" :class="{ 'modal-open': show }">
    <div class="modal-box max-w-lg">
      <h3 class="font-bold text-lg mb-4 text-center">正在融合知识...</h3>

      <!-- 融合进度信息 -->
      <div class="bg-base-200 rounded-lg p-4">
        <div class="flex items-center gap-3">
          <span class="loading loading-spinner loading-sm text-accent"></span>
          <span class="text-sm font-medium text-gray-700">正在进行融合演进...</span>
        </div>
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
});
</script>

<style scoped></style>
