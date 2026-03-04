<template>
  <div class="w-72 hidden lg:block">
    <div class="h-[calc(100vh-64px)] py-4 px-2 flex flex-col sticky top-[64px]">
      <!-- 知识生成和冲突消解按钮区域 -->
      <div class="mb-4 flex gap-2">
        <!-- 知识生成按钮 -->
        <button
          class="btn btn-primary btn-sm flex-1 flex items-center justify-center"
          @click="$emit('open-generation')"
        >
          <svg
            xmlns="http://www.w3.org/2000/svg"
            class="w-4 h-4 mr-1"
            viewBox="0 0 20 20"
            fill="currentColor"
          >
            <path
              d="M13.586 3.586a2 2 0 112.828 2.828l-.793.793-2.828-2.828.793-.793zM11.379 5.793L3 14.172V17h2.828l8.38-8.379-2.83-2.828z"
            />
          </svg>
          <span>知识生成</span>
        </button>

        <!-- 冲突消解按钮 -->
        <button
          class="btn btn-secondary btn-sm flex-1 flex items-center justify-center"
          :disabled="!canResolveConflict"
          @click="$emit('open-conflict-resolution')"
        >
          <svg
            xmlns="http://www.w3.org/2000/svg"
            class="w-4 h-4 mr-1"
            viewBox="0 0 20 20"
            fill="currentColor"
          >
            <path
              fill-rule="evenodd"
              d="M6.267 3.455a3.066 3.066 0 001.745-.723 3.066 3.066 0 013.976 0 3.066 3.066 0 001.745.723 3.066 3.066 0 012.812 2.812c.051.643.304 1.254.723 1.745a3.066 3.066 0 010 3.976 3.066 3.066 0 00-.723 1.745 3.066 3.066 0 01-2.812 2.812 3.066 3.066 0 00-1.745.723 3.066 3.066 0 01-3.976 0 3.066 3.066 0 00-1.745-.723 3.066 3.066 0 01-2.812-2.812 3.066 3.066 0 00-.723-1.745 3.066 3.066 0 010-3.976 3.066 3.066 0 00.723-1.745 3.066 3.066 0 012.812-2.812zm7.44 5.252a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z"
              clip-rule="evenodd"
            />
          </svg>
          <span>冲突消解</span>
        </button>
      </div>

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
        <p class="font-bold mb-6 pl-2 text-left w-full">
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
          <!-- 默认状态 -->
        </p>
        <li v-if="!knowledgeList.length" class="text-gray-500 text-left py-4 px-4">
          <template v-if="searchResult && searchResult.count === 0"> 未找到匹配的节点 </template>
          <template v-else-if="selectedCategory"> 该分类下暂无数据 </template>
          <template v-else> 请选择分类或搜索节点 </template>
        </li>
        <!-- 普通知识列表 -->
        <li
          v-else
          v-for="node in knowledgeList"
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

// 判断节点是否为策略树
const isStrategyTree = (node) => {
  return node?.labels?.some((label) => label.toLowerCase() === 'tree') || false;
};

// 处理节点点击事件，根据类型选择不同的处理方式
const handleNodeClick = (node) => {
  emit('select-node', node);
};
</script>
