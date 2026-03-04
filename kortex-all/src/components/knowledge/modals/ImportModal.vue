<template>
  <dialog class="modal" :class="{ 'modal-open': show }">
    <div class="modal-box">
      <h3 class="font-bold text-lg mb-4">导入知识</h3>
      <form @submit.prevent="$emit('submit')">
        <div class="form-control mb-4">
          <label class="label">
            <span class="label-text">数据类型</span>
          </label>
          <div class="flex gap-4">
            <label class="label cursor-pointer">
              <input
                type="radio"
                name="import-type"
                class="radio"
                value="node"
                v-model="importForm.type"
              />
              <span class="label-text ml-2">节点信息</span>
            </label>
            <label class="label cursor-pointer">
              <input
                type="radio"
                name="import-type"
                class="radio"
                value="relation"
                v-model="importForm.type"
              />
              <span class="label-text ml-2">关系信息</span>
            </label>
          </div>
        </div>

        <div class="form-control mb-4">
          <label class="label">
            <span class="label-text">选择CSV文件</span>
          </label>
          <input
            ref="fileInput"
            type="file"
            class="file-input file-input-bordered"
            accept=".csv"
            @change="importForm.file = $event.target.files[0]"
            required
          />
          <div class="label">
            <span class="label-text-alt text-gray-500">
              请上传CSV格式文件，确保格式符合API要求
            </span>
          </div>
        </div>

        <div class="modal-action">
          <button type="submit" class="btn btn-primary" :disabled="loading || !importForm.file">
            {{ loading ? '导入中...' : '导入' }}
          </button>
          <button type="button" class="btn" @click="$emit('cancel')">取消</button>
        </div>
      </form>
    </div>
  </dialog>
</template>

<script setup>
import { ref, watch } from 'vue';

const props = defineProps({
  show: {
    type: Boolean,
    default: false,
  },
  importForm: {
    type: Object,
    required: true,
  },
  loading: {
    type: Boolean,
    default: false,
  },
});

defineEmits(['submit', 'cancel']);

const fileInput = ref(null);

// 监听弹框显示状态，打开时清除已选文件和状态
watch(
  () => props.show,
  (newVal) => {
    if (newVal) {
      // 清除文件
      props.importForm.file = null;
      // 重置类型为默认值
      props.importForm.type = 'node';
      // 清除文件输入框
      if (fileInput.value) {
        fileInput.value.value = '';
      }
    }
  },
);
</script>
