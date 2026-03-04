<template>
  <dialog class="modal" :class="{ 'modal-open': show }">
    <div class="modal-box max-w-4xl">
      <h3 class="font-bold text-lg mb-4">融合演进新知识</h3>
      <form @submit.prevent="handleSubmit">
        <div class="form-control mb-4">
          <label class="label">
            <span class="label-text">选择用于融合的事件分类</span>
            <span class="label-text-alt" v-if="selectedCategory"
              >已选择：{{ selectedCategory }}</span
            >
          </label>

          <!-- 搜索框 -->
          <div class="mb-3">
            <label class="input input-bordered input-sm flex items-center gap-2">
              <svg
                xmlns="http://www.w3.org/2000/svg"
                viewBox="0 0 16 16"
                fill="currentColor"
                class="h-4 w-4 opacity-70"
              >
                <path
                  fill-rule="evenodd"
                  d="M9.965 11.026a5 5 0 1 1 1.06-1.06l2.755 2.754a.75.75 0 1 1-1.06 1.06l-2.755-2.754ZM10.5 7a3.5 3.5 0 1 1-7 0 3.5 3.5 0 0 1 7 0Z"
                  clip-rule="evenodd"
                />
              </svg>
              <input
                type="text"
                class="grow outline-none"
                placeholder="搜索分类名称..."
                v-model="searchQuery"
              />
              <button
                v-if="searchQuery"
                @click="clearSearch"
                type="button"
                class="cursor-pointer hover:text-error"
              >
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  viewBox="0 0 16 16"
                  fill="currentColor"
                  class="h-4 w-4 opacity-70"
                >
                  <path
                    d="M5.28 4.22a.75.75 0 0 0-1.06 1.06L6.94 8l-2.72 2.72a.75.75 0 1 0 1.06 1.06L8 9.06l2.72 2.72a.75.75 0 1 0 1.06-1.06L9.06 8l2.72-2.72a.75.75 0 0 0-1.06-1.06L8 6.94 5.28 4.22Z"
                  />
                </svg>
              </button>
            </label>
          </div>

          <!-- 事件分类列表 -->
          <div ref="scrollContainer" class="bg-base-200 rounded-lg p-3 max-h-80 overflow-y-auto">
            <div v-if="!categoryList.length" class="text-center text-gray-500 py-8">
              暂无可选分类
            </div>
            <div v-else-if="!filteredCategoryList.length" class="text-center text-gray-500 py-8">
              未找到匹配的分类
            </div>
            <div v-else>
              <div class="grid grid-cols-2 gap-2">
                <label
                  v-for="category in filteredCategoryList"
                  :key="category.name"
                  class="relative flex items-center gap-2 p-3 bg-white rounded hover:bg-gray-50 cursor-pointer transition"
                  :class="{ 'ring-2 ring-primary': selectedCategory === category.name }"
                >
                  <input
                    type="radio"
                    :value="category.name"
                    v-model="selectedCategory"
                    class="radio radio-primary radio-sm flex-shrink-0"
                  />
                  <div class="flex-1 min-w-0">
                    <div class="font-medium text-sm">{{ category.name }}</div>
                  </div>
                </label>
              </div>
            </div>
          </div>
        </div>

        <div class="modal-action">
          <button
            type="submit"
            class="btn btn-primary btn-sm"
            :disabled="loading || !selectedCategory"
          >
            {{ loading ? '融合中...' : '开始融合' }}
          </button>
          <button type="button" class="btn btn-sm" @click="handleCancel">取消</button>
        </div>
      </form>
    </div>
  </dialog>
</template>

<script setup>
import { ref, watch, computed } from 'vue';

const props = defineProps({
  show: {
    type: Boolean,
    default: false,
  },
  categoryList: {
    type: Array,
    default: () => [],
  },
  loading: {
    type: Boolean,
    default: false,
  },
});

const emit = defineEmits(['submit', 'cancel']);

const selectedCategory = ref('');
const searchQuery = ref('');
const scrollContainer = ref(null);

// 本地搜索过滤分类列表
const filteredCategoryList = computed(() => {
  const query = searchQuery.value.trim().toLowerCase();

  if (!query) {
    return props.categoryList;
  }

  return props.categoryList.filter((category) => {
    const name = (category.name || '').toLowerCase();
    return name.includes(query);
  });
});

// 清空搜索
const clearSearch = () => {
  searchQuery.value = '';
};

// 当模态框打开时重置选中的分类和搜索
watch(
  () => props.show,
  (newVal) => {
    if (newVal) {
      selectedCategory.value = '';
      searchQuery.value = '';
    }
  },
);

const handleSubmit = () => {
  emit('submit', selectedCategory.value);
};

const handleCancel = () => {
  selectedCategory.value = '';
  searchQuery.value = '';
  emit('cancel');
};
</script>
