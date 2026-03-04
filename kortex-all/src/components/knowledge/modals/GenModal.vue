<template>
  <div v-if="show" class="modal modal-open">
    <div class="modal-box max-w-4xl">
      <h3 class="font-bold text-lg mb-4">知识生成</h3>

      <!-- 知识类型和来源选择 -->
      <div class="flex items-start gap-6 mb-4">
        <!-- 知识类型选择 -->
        <div class="form-control">
          <label class="label pb-2 pt-0 h-8">
            <span class="label-text font-semibold">知识类型</span>
          </label>
          <div class="flex gap-4 items-center h-8">
            <label class="label cursor-pointer gap-2 p-0 h-auto">
              <input
                type="radio"
                name="knowledge-type"
                class="radio radio-primary radio-sm"
                value="entity"
                v-model="knowledgeType"
              />
              <span class="label-text">实体</span>
            </label>
            <label
              v-show="activeTab !== 'image' && activeTab !== 'file'"
              class="label cursor-pointer gap-2 p-0 h-auto"
            >
              <input
                type="radio"
                name="knowledge-type"
                class="radio radio-primary radio-sm"
                value="event"
                v-model="knowledgeType"
              />
              <span class="label-text">事件</span>
            </label>
            <label
              v-show="activeTab !== 'image' && activeTab !== 'file'"
              class="label cursor-pointer gap-2 p-0 h-auto"
            >
              <input
                type="radio"
                name="knowledge-type"
                class="radio radio-primary radio-sm"
                value="model"
                v-model="knowledgeType"
              />
              <span class="label-text">模型</span>
            </label>
          </div>
        </div>

        <!-- 分隔线 -->
        <div class="h-16 w-px bg-gray-300"></div>

        <!-- 知识来源 Tab 切换 -->
        <div class="form-control flex-1">
          <label class="label pb-2 pt-0 h-8">
            <span class="label-text font-semibold">知识来源</span>
          </label>
          <div role="tablist" class="tabs tabs-boxed h-8">
            <a
              role="tab"
              class="tab tab-sm h-full"
              :class="{ 'tab-active': activeTab === 'text' }"
              @click="activeTab = 'text'"
            >
              <svg
                xmlns="http://www.w3.org/2000/svg"
                class="w-4 h-4 mr-1"
                viewBox="0 0 20 20"
                fill="currentColor"
              >
                <path
                  fill-rule="evenodd"
                  d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z"
                  clip-rule="evenodd"
                />
              </svg>
              文本
            </a>
            <a
              role="tab"
              class="tab tab-sm h-full"
              :class="{ 'tab-active': activeTab === 'image' }"
              @click="activeTab = 'image'"
            >
              <svg
                xmlns="http://www.w3.org/2000/svg"
                class="w-4 h-4 mr-1"
                viewBox="0 0 20 20"
                fill="currentColor"
              >
                <path
                  fill-rule="evenodd"
                  d="M4 3a2 2 0 00-2 2v10a2 2 0 002 2h12a2 2 0 002-2V5a2 2 0 00-2-2H4zm12 12H4l4-8 3 6 2-4 3 6z"
                  clip-rule="evenodd"
                />
              </svg>
              图片
            </a>
            <a
              role="tab"
              class="tab tab-sm h-full"
              :class="{ 'tab-active': activeTab === 'file' }"
              @click="activeTab = 'file'"
            >
              <svg
                xmlns="http://www.w3.org/2000/svg"
                class="w-4 h-4 mr-1"
                viewBox="0 0 20 20"
                fill="currentColor"
              >
                <path
                  fill-rule="evenodd"
                  d="M4 4a2 2 0 012-2h4.586A2 2 0 0112 2.586L15.414 6A2 2 0 0116 7.414V16a2 2 0 01-2 2H6a2 2 0 01-2-2V4z"
                  clip-rule="evenodd"
                />
              </svg>
              文件
            </a>
          </div>
        </div>
      </div>

      <!-- Tab 内容 -->
      <div class="min-h-[200px]">
        <!-- 文本输入 -->
        <div v-show="activeTab === 'text'" class="form-control">
          <label class="label">
            <span class="label-text">请输入文本内容</span>
          </label>
          <textarea
            v-model="textContent"
            class="textarea textarea-bordered h-32"
            placeholder="输入要生成知识的文本内容..."
          ></textarea>
        </div>

        <!-- 图片选择 -->
        <div v-show="activeTab === 'image'" class="form-control">
          <label class="label">
            <span class="label-text">选择图片文件</span>
          </label>
          <input
            type="file"
            accept="image/*"
            @change="handleImageSelect"
            class="file-input file-input-bordered w-full"
          />
          <div v-if="imagePreview" class="mt-4">
            <img :src="imagePreview" alt="预览" class="max-h-64 rounded border" />
          </div>
        </div>

        <!-- 文件上传 -->
        <div v-show="activeTab === 'file'" class="form-control">
          <label class="label">
            <span class="label-text">选择文件（仅支持 PDF 格式）</span>
          </label>
          <input
            type="file"
            accept=".pdf"
            @change="handleFileSelect"
            class="file-input file-input-bordered w-full"
          />
          <div v-if="fileName" class="mt-2 text-sm text-gray-600">已选择: {{ fileName }}</div>
        </div>
      </div>

      <div class="modal-action">
        <button class="btn" @click="handleCancel" :disabled="loading">取消</button>
        <button class="btn btn-primary" @click="handleSubmit" :disabled="loading || !isValid">
          <span v-if="loading" class="loading loading-spinner"></span>
          开始生成
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue';

const props = defineProps({
  show: {
    type: Boolean,
    default: false,
  },
  loading: {
    type: Boolean,
    default: false,
  },
});

const emit = defineEmits(['submit', 'cancel']);

const activeTab = ref('text');
const knowledgeType = ref('entity'); // 默认选择实体
const textContent = ref('');
const imageFile = ref(null);
const imagePreview = ref('');
const file = ref(null);
const fileName = ref('');

// 监听弹框显示状态，打开时清除所有状态和已选文件
watch(
  () => props.show,
  (newVal) => {
    if (newVal) {
      // 重置所有状态
      activeTab.value = 'text';
      knowledgeType.value = 'entity';
      textContent.value = '';
      imageFile.value = null;
      imagePreview.value = '';
      file.value = null;
      fileName.value = '';
    }
  },
);

// 监听 activeTab 变化，当切换到图片或文件时，强制知识类型为实体
watch(activeTab, (newTab) => {
  if ((newTab === 'image' || newTab === 'file') && knowledgeType.value !== 'entity') {
    knowledgeType.value = 'entity';
  }
});

const isValid = computed(() => {
  if (activeTab.value === 'text') {
    return textContent.value.trim().length > 0;
  } else if (activeTab.value === 'image') {
    return imageFile.value !== null;
  } else if (activeTab.value === 'file') {
    return file.value !== null;
  }
  return false;
});

const handleImageSelect = (event) => {
  const selectedFile = event.target.files[0];
  if (selectedFile) {
    imageFile.value = selectedFile;
    // 创建预览
    const reader = new FileReader();
    reader.onload = (e) => {
      imagePreview.value = e.target.result;
    };
    reader.readAsDataURL(selectedFile);
  }
};

const handleFileSelect = (event) => {
  const selectedFile = event.target.files[0];
  if (selectedFile) {
    file.value = selectedFile;
    fileName.value = selectedFile.name;
  }
};

const handleSubmit = () => {
  let data = null;
  if (activeTab.value === 'text') {
    data = { type: 'text', content: textContent.value, knowledgeType: knowledgeType.value };
  } else if (activeTab.value === 'image') {
    data = { type: 'image', file: imageFile.value, knowledgeType: knowledgeType.value };
  } else if (activeTab.value === 'file') {
    data = { type: 'file', file: file.value, knowledgeType: knowledgeType.value };
  }
  emit('submit', data);
};

const handleCancel = () => {
  // 重置表单
  textContent.value = '';
  imageFile.value = null;
  imagePreview.value = '';
  file.value = null;
  fileName.value = '';
  activeTab.value = 'text';
  knowledgeType.value = 'entity'; // 重置为默认值
  emit('cancel');
};
</script>
