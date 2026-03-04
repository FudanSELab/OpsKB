<template>
  <div class="w-72 hidden md:block">
    <div class="max-h-[calc(100vh-64px)] py-4 px-2 overflow-auto sticky top-[64px]">
      <!-- 搜索 -->
      <label class="input input-bordered input-sm flex items-center gap-2 mb-4">
        <input
          type="text"
          class="grow outline-none ring-0"
          :placeholder="searchPlaceholder"
          :value="searchQuery"
          @input="$emit('update:searchQuery', $event.target.value)"
          @keyup.enter="$emit('search')"
        />
        <!-- 清空按钮 -->
        <button
          v-if="searchQuery"
          @click="$emit('clear-search')"
          class="cursor-pointer hover:text-error"
          :disabled="loading"
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
        <!-- 搜索按钮 -->
        <button
          @click="$emit('search')"
          class="cursor-pointer hover:text-primary"
          :disabled="loading"
        >
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
        </button>
      </label>
      <button
        class="btn w-full text-lg mb-4"
        :class="
          isOverviewMode
            ? 'bg-blue-100 text-blue-700 border-blue-200'
            : 'bg-white text-black border-[#e5e5e5]'
        "
        @click="$emit('overview-click')"
      >
        <Book2 class="w-4 h-4" />
        知识库总览
      </button>
      <!-- 知识类及其子类展示 -->
      <div class="join join-vertical w-full">
        <div v-if="showEntitySection" class="collapse collapse-arrow join-item border-base-300 border">
          <input type="checkbox" checked />
          <div class="collapse-title text-lg">
            知识分类({{
              Object.values(categoryCounts.entity).reduce((sum, count) => sum + count, 0)
            }})
          </div>
          <div class="collapse-content">
            <ul>
              <li
                v-for="(count, category) in categoryCounts.entity"
                :key="category"
                class="cursor-pointer px-2 py-1 rounded hover:bg-gray-100 mb-1"
                :class="{
                  'bg-blue-100 text-blue-700 font-bold':
                    selectedCategory?.type === 'entity' && selectedCategory?.main === category,
                }"
                @click="$emit('select-category', 'entity', category)"
              >
                {{ category }} ({{ count }})
              </li>
            </ul>
          </div>
        </div>
        <!-- 事件类知识 -->
        <div v-if="showEventSection" class="collapse collapse-arrow join-item border-base-300 border">
          <input type="checkbox" checked />
          <div class="collapse-title text-lg">
            事件类知识({{
              Object.values(categoryCounts.event).reduce((sum, count) => sum + count, 0)
            }})
          </div>
          <div class="collapse-content">
            <ul>
              <li
                v-for="(count, category) in categoryCounts.event"
                :key="category"
                class="cursor-pointer px-2 py-1 rounded hover:bg-gray-100 mb-1"
                :class="{
                  'bg-blue-100 text-blue-700 font-bold':
                    selectedCategory?.type === 'event' && selectedCategory?.main === category,
                }"
                @click="$emit('select-category', 'event', category)"
              >
                {{ category }} ({{ count }})
              </li>
            </ul>
          </div>
        </div>
        <!-- 模型类知识 -->
        <div v-if="showModelSection" class="collapse collapse-arrow join-item border-base-300 border">
          <input type="checkbox" checked />
          <div class="collapse-title text-lg">
            模型类知识({{
              Object.values(categoryCounts.model).reduce((sum, count) => sum + count, 0)
            }})
          </div>
          <div class="collapse-content">
            <ul>
              <li
                v-for="(count, category) in categoryCounts.model"
                :key="category"
                class="cursor-pointer px-2 py-1 rounded hover:bg-gray-100 mb-1"
                :class="{
                  'bg-blue-100 text-blue-700 font-bold':
                    selectedCategory?.type === 'model' && selectedCategory?.main === category,
                }"
                @click="$emit('select-category', 'model', category)"
              >
                {{ category }} ({{ count }})
              </li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { Book2 } from '@vicons/tabler';
const props = defineProps({
  searchQuery: {
    type: String,
    default: '',
  },
  searchPlaceholder: {
    type: String,
    default: '搜索节点...',
  },
  categoryCounts: {
    type: Object,
    required: true,
  },
  selectedCategory: {
    type: Object,
    default: null,
  },
  loading: {
    type: Boolean,
    default: false,
  },
  isOverviewMode: {
    type: Boolean,
    default: false,
  },
  visibleCategoryTypes: {
    type: Array,
    default: () => ['entity', 'event', 'model'],
  },
});

defineEmits(['update:searchQuery', 'search', 'clear-search', 'select-category', 'overview-click']);
const showEntitySection = computed(() => props.visibleCategoryTypes.includes('entity'));
const showEventSection = computed(() => props.visibleCategoryTypes.includes('event'));
const showModelSection = computed(() => props.visibleCategoryTypes.includes('model'));
</script>
