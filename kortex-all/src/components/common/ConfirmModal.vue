<template>
  <dialog class="modal z-[9999]" :class="{ 'modal-open': show }">
    <div class="modal-box">
      <h3 class="font-bold text-lg">{{ title }}</h3>
      <div class="py-4">
        <div class="whitespace-pre-line">{{ message }}</div>
      </div>
      <div class="modal-action">
        <button @click="handleConfirm" class="btn btn-error">确认</button>
        <button @click="handleCancel" class="btn">取消</button>
      </div>
    </div>
  </dialog>
</template>

<script setup>
defineProps({
  show: {
    type: Boolean,
    default: false,
  },
  title: {
    type: String,
    default: '确认操作',
  },
  message: {
    type: String,
    default: '',
  },
});

const emit = defineEmits(['confirm', 'cancel']);

const handleConfirm = () => {
  emit('confirm');
};

const handleCancel = () => {
  emit('cancel');
};
</script>
