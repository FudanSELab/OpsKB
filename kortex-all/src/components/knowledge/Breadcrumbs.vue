<template>
  <div class="flex items-center justify-between mb-4">
    <div class="breadcrumbs text-sm overflow-hidden flex-1 relative">
      <ul>
        <!-- 搜索状态下的面包屑 -->
        <template v-if="searchResult">
          <li>
            <a @click="$emit('go-kb-home')" class="cursor-pointer hover:text-primary">{{
              knowledgeBaseName
            }}</a>
          </li>
          <li><a>搜索结果</a></li>
          <li>{{ searchResult.query }} ({{ searchResult.count }}个结果)</li>
          <li v-if="selectedNodeDetails">
            {{ selectedNodeDetails.properties?.name || selectedNodeDetails.id }}
          </li>
        </template>

        <!-- 分类浏览状态下的面包屑 -->
        <template v-else-if="selectedCategory">
          <li>
            <a @click="$emit('go-kb-home')" class="cursor-pointer hover:text-primary">{{
              knowledgeBaseName
            }}</a>
          </li>
          <li>
            <a>{{
              selectedCategory.type === 'tree'
                ? '策略树知识'
                : selectedCategory.type === 'entity'
                  ? '实体类'
                  : selectedCategory.type === 'event'
                    ? '事件类'
                    : '模型类'
            }}</a>
          </li>
          <li v-if="selectedCategory.type === 'tree' && selectedCategory.scenarioName">
            <a>{{ selectedCategory.scenarioName }}</a>
          </li>
          <li v-else-if="selectedCategory.main">
            <a>{{ selectedCategory.main }}</a>
          </li>
          <li v-if="selectedStrategyTree">
            {{ selectedStrategyTree.properties?.name || `策略树${selectedStrategyTree.id}` }}
          </li>
          <li v-else-if="selectedNodeDetails && selectedCategory.type !== 'fusion'">
            {{ selectedNodeDetails.properties?.name || selectedNodeDetails.id }}
          </li>
        </template>

        <!-- 总览模式下的面包屑 -->
        <template v-else-if="isOverviewMode">
          <li>
            <a @click="$emit('go-kb-home')" class="cursor-pointer hover:text-primary">{{
              knowledgeBaseName
            }}</a>
          </li>
          <li><a>知识库总览</a></li>
        </template>

        <!-- 默认状态下的面包屑 -->
        <template v-else>
          <li>
            <a @click="$emit('go-kb-home')" class="cursor-pointer hover:text-primary">{{
              knowledgeBaseName
            }}</a>
          </li>
          <li v-if="selectedNodeDetails">
            {{ selectedNodeDetails.properties?.name || selectedNodeDetails.id }}
          </li>
        </template>
      </ul>
      <!-- 渐变遮罩 -->
      <div
        class="absolute top-0 right-0 h-full w-16 bg-gradient-to-r from-transparent to-white"
      ></div>
    </div>
    <div class="flex items-center gap-2">
      <button class="btn btn-sm" @click="$emit('add-node')" :disabled="loading">添加知识</button>
      <button class="btn btn-sm" @click="$emit('add-relation')" :disabled="loading">
        添加关联
      </button>
      <button class="btn btn-sm" @click="$emit('import')" :disabled="loading">批量导入</button>
      <template v-if="isOverviewMode">
        <button
          class="btn btn-sm btn-primary"
          @click="$emit('create-fusion-scenario')"
          :disabled="loading"
        >
          <svg
            xmlns="http://www.w3.org/2000/svg"
            class="w-4 h-4"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
          >
            <path
              d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"
            ></path>
            <polyline points="7.5 4.21 12 6.81 16.5 4.21"></polyline>
            <polyline points="7.5 19.79 7.5 14.6 3 12"></polyline>
            <polyline points="21 12 16.5 14.6 16.5 19.79"></polyline>
            <polyline points="3.27 6.96 12 12.01 20.73 6.96"></polyline>
            <line x1="12" y1="22.08" x2="12" y2="12"></line>
          </svg>
          融合演进
        </button>
      </template>
    </div>
  </div>
</template>

<script setup>
defineProps({
  searchResult: {
    type: Object,
    default: null,
  },
  selectedCategory: {
    type: Object,
    default: null,
  },
  selectedNodeDetails: {
    type: Object,
    default: null,
  },
  selectedStrategyTree: {
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
  knowledgeBaseName: {
    type: String,
    default: '知识库',
  },
});

defineEmits([
  'clear-search',
  'clear-category',
  'clear-overview',
  'go-kb-home',
  'add-node',
  'add-relation',
  'import',
  'create-fusion-scenario',
]);
</script>
