<template>
  <div class="w-72 hidden lg:block">
    <div class="h-[calc(100vh-64px)] py-4 px-2 flex flex-col sticky top-[64px]">
      <!-- 搜索结果统计信息 -->
      <div
        v-if="searchResult && searchResult.count > 0"
        class="bg-info/10 border border-info/20 rounded-lg p-3 mb-4 text-left"
      >
        <div class="flex items-center gap-2 text-sm justify-start">
          <svg
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 16 16"
            fill="currentColor"
            class="w-4 h-4 text-info flex-shrink-0"
          >
            <path
              fill-rule="evenodd"
              d="M9.965 11.026a5 5 0 1 1 1.06-1.06l2.755 2.754a.75.75 0 1 1-1.06 1.06l-2.755-2.754ZM10.5 7a3.5 3.5 0 1 1-7 0 3.5 3.5 0 0 1 7 0Z"
              clip-rule="evenodd"
            />
          </svg>
          <span class="font-medium text-info text-left">搜索 "{{ searchResult.query }}"</span>
        </div>
        <div class="mt-2 text-xs text-gray-600 text-left">
          找到
          <span class="font-semibold text-info">{{ searchResult.count }}</span>
          个相关节点
          <span v-if="selectedNode" class="block mt-1 text-left">
            已选择: {{ selectedNodeDetails?.properties?.name || selectedNodeDetails?.id }}
          </span>
        </div>
      </div>

      <ul
        class="menu menu-xs bg-base-200 rounded-lg w-full max-w-xs flex-1 flex-nowrap overflow-y-auto"
      >
        <p class="font-bold mb-2 pl-2 text-left w-full">
          <!-- 搜索状态下的标题 -->
          <template v-if="searchResult">
            搜索结果
            <span class="text-xs text-gray-500"> ({{ searchResult.count }}个结果) </span>
          </template>
          <!-- 分类浏览状态下的标题 -->
          <template v-else-if="selectedCategory">
            知识列表
            <span class="text-xs text-gray-500"> ({{ selectedCategory.main }}) </span>
          </template>
          <!-- Tree类型节点显示策略列表 -->
          <template v-else-if="isOverviewMode || isStrategyTree(selectedNode)"> 节点列表 </template>
          <template v-else>知识列表</template>
        </p>

        <!-- 每页条数选择 -->
        <li v-if="knowledgeList.length > 0" class="px-2 mb-2">
          <div class="flex items-center gap-1 text-xs text-gray-500">
            <span>每页</span>
            <select
              v-model="pageSize"
              class="select select-xs select-bordered w-14"
              @change="currentPage = 1"
            >
              <option :value="10">10</option>
              <option :value="20">20</option>
            </select>
            <span>条，共 {{ knowledgeList.length }} 条</span>
          </div>
        </li>

        <li v-if="!knowledgeList.length" class="text-gray-500 text-left py-4 px-4">
          <template v-if="searchResult && searchResult.count === 0"> 未找到匹配的节点 </template>
          <template v-else-if="selectedCategory"> 该分类下暂无数据 </template>
          <template v-else> 请选择分类或搜索节点 </template>
        </li>
        <!-- 当前页知识列表 -->
        <li
          v-else
          v-for="node in pagedList"
          :key="node.id"
          @click="handleNodeClick(node)"
          class="cursor-pointer w-full"
          :class="{ 'bg-primary text-primary-content': selectedNode?.id === node.id }"
        >
          <a class="flex flex-col items-start text-left justify-start w-full overflow-hidden">
            <div class="flex items-center gap-2 w-full">
              <component
                :is="isStrategyTree(node) ? GitMerge : Book2"
                class="w-4 h-4 flex-shrink-0"
              />
              <span
                class="truncate flex-1 text-left"
                v-html="
                  searchResult
                    ? highlightSearchTerm(node.properties?.name || node.id, searchResult.query)
                    : node.properties?.name || node.id
                "
              ></span>
            </div>
            <!-- 搜索结果显示额外信息 -->
            <div
              v-if="searchResult && node.labels"
              class="text-xs opacity-70 mt-1 text-left w-full"
            >
              {{ node.labels.join(' · ') }}
            </div>
          </a>
        </li>
      </ul>

      <!-- 分页控制 -->
      <div v-if="totalPages > 1" class="flex items-center justify-between mt-2 px-1">
        <button
          class="btn btn-xs btn-ghost"
          :disabled="currentPage === 1"
          @click="currentPage--"
        >«</button>
        <span class="text-xs text-gray-500">{{ currentPage }} / {{ totalPages }}</span>
        <button
          class="btn btn-xs btn-ghost"
          :disabled="currentPage === totalPages"
          @click="currentPage++"
        >»</button>
      </div>

      <div v-if="isOverviewMode" class="mt-3 bg-white rounded-lg border border-gray-200 p-3 overflow-y-auto max-h-64">
        <div class="text-sm font-semibold mb-2">节点信息</div>
        <div v-if="!selectedNodeDetails" class="text-xs text-gray-500">点击图中的节点后在这里显示详情</div>
        <div v-else class="space-y-2 text-xs">
          <div>
            <span class="text-gray-500">名称：</span>
            <span>{{ selectedNodeDetails.properties?.name || selectedNodeDetails.id }}</span>
          </div>
          <div>
            <span class="text-gray-500">标签：</span>
            <span>{{ (selectedNodeDetails.labels || []).join(' · ') || '-' }}</span>
          </div>
          <div class="max-h-36 overflow-auto border rounded px-2 py-1 bg-gray-50">
            <div
              v-for="(value, key) in selectedNodeDetails.properties || {}"
              :key="key"
              class="mb-1 break-all"
            >
              <span class="text-gray-500">{{ key }}:</span> {{ value }}
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue';
import { Book2, GitMerge } from '@vicons/tabler';
import { highlightSearchTerm } from '../../utils/knowledgeUtils';

const props = defineProps({
  isOverviewMode: {
    type: Boolean,
    default: false,
  },
  knowledgeList: {
    type: Array,
    default: () => [],
  },
  searchResult: {
    type: Object,
    default: null,
  },
  selectedCategory: {
    type: Object,
    default: null,
  },
  selectedNode: {
    type: Object,
    default: null,
  },
  selectedNodeDetails: {
    type: Object,
    default: null,
  },
  canResolveConflict: {
    type: Boolean,
    default: false,
  },
});

const emit = defineEmits(['select-node', 'open-generation', 'open-conflict-resolution']);

const pageSize = ref(10);
const currentPage = ref(1);

// 列表变化时重置到第一页
watch(() => props.knowledgeList, () => { currentPage.value = 1; });

const totalPages = computed(() => Math.max(1, Math.ceil(props.knowledgeList.length / pageSize.value)));

const pagedList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  return props.knowledgeList.slice(start, start + pageSize.value);
});

const isStrategyTree = (node) => {
  return node?.labels?.some((label) => label.toLowerCase() === 'tree') || false;
};

const handleNodeClick = (node) => {
  emit('select-node', node);
};
</script>
