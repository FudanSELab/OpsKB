<template>
  <!-- Toast Container -->
  <Toast :toasts="toasts" @remove="removeToast" />

  <!-- Confirm Modal -->
  <ConfirmModal
    :show="confirmModal.show"
    :title="confirmModal.title"
    :message="confirmModal.message"
    @confirm="handleConfirm"
    @cancel="handleCancel"
  />

  <div class="h-[calc(100vh-64px)]">
    <div class="max-w-screen-xl mx-auto">
      <div class="flex">
        <!-- 左侧边栏 -->
        <CategorySidebar
          v-model:searchQuery="state.searchQuery"
          :searchPlaceholder="sidebarSearchPlaceholder"
          :categoryCounts="state.categoryCounts"
          :visibleCategoryTypes="visibleCategoryTypes"
          :selectedCategory="state.selectedCategory"
          :isOverviewMode="state.isOverviewMode"
          :loading="loading"
          @search="handleSearch"
          @clear-search="clearSearchInput"
          @select-category="selectCategory"
          @overview-click="handleOverviewClick"
        />

        <!-- 中间内容区 -->
        <div class="flex-1 min-h-0">
          <div class="p-3.5">
            <!-- 面包屑及操作按钮 -->
            <Breadcrumbs
              :searchResult="state.searchResult"
              :selectedCategory="state.selectedCategory"
              :selectedNodeDetails="state.selectedNodeDetails"
              :isOverviewMode="state.isOverviewMode"
              :knowledgeBaseName="activeKnowledgeBase?.name || '知识库'"
              :loading="loading"
              @clear-search="clearSearch"
              @clear-category="clearCategory"
              @clear-overview="clearOverview"
              @go-kb-home="goKnowledgeBaseHome"
              @add-node="openAddNodeModal"
              @add-relation="openAddRelationModal"
              @import="openCSVImportModal"
              @create-fusion-scenario="openCreateFusionScenarioModal"
            />
            <div class="mt-3 mb-3 flex flex-wrap items-center gap-2 rounded-lg border border-gray-200 bg-white px-3 py-2">
              <span class="text-sm font-medium text-gray-700">知识库</span>
              <button
                v-for="kb in knowledgeBases"
                :key="kb.id"
                class="btn btn-xs"
                :class="activeKnowledgeBaseId === kb.id ? 'btn-primary' : 'btn-outline'"
                :disabled="loading"
                @click="switchKnowledgeBase(kb.id)"
              >
                {{ kb.name }}
              </button>
              <span class="mx-1 h-4 w-px bg-gray-200"></span>
              <span class="text-sm font-medium text-gray-700">存储</span>
              <button
                v-for="source in activeKnowledgeBaseSources"
                :key="source"
                class="btn btn-xs"
                :class="activeStorageSource === source ? 'btn-secondary' : 'btn-outline'"
                :disabled="loading"
                @click="switchStorageSource(source)"
              >
                {{ source }}
              </button>
              <span
                v-if="activeKnowledgeBase"
                class="text-xs"
                :class="isReadOnlyKnowledgeBase ? 'text-amber-600' : 'text-emerald-700'"
              >
                {{ activeStorageSource }} · {{ isReadOnlyKnowledgeBase ? '只读' : '可编辑' }}
              </span>
            </div>
            <template v-if="showGraphPresentation">
            <!-- 知识图谱绘图区 -->
            <div
              class="relative"
              :class="state.isOverviewMode ? 'h-[calc(100vh-145px)]' : 'h-[300px]'"
            >
              <!-- 无状态提示 -->
              <div
                v-if="!state.selectedNode && !state.isOverviewMode"
                class="absolute inset-0 flex items-center justify-center bg-gray-50 rounded-lg border border-gray-200"
              >
                <div class="text-center text-gray-400">
                  <svg
                    class="w-16 h-16 mx-auto mb-2 opacity-50"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      stroke-linecap="round"
                      stroke-linejoin="round"
                      stroke-width="1.5"
                      d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
                    ></path>
                  </svg>
                  <p class="text-sm font-medium">请选择知识查看图谱</p>
                </div>
              </div>
              <!-- 知识图谱 -->
              <KnowledgeGraph
                v-else
                :selected-node="state.selectedNode"
                :node-relations="state.selectedNodeRelations"
                :all-nodes="
                  state.isTreeGraphMode
                    ? state.treeGraphNodes
                    : state.isOverviewMode
                      ? state.allNodes
                      : []
                "
                :all-relations="
                  state.isTreeGraphMode
                    ? state.treeGraphRelations
                    : state.isOverviewMode
                      ? state.allRelations
                      : []
                "
                :is-overview-mode="state.isOverviewMode || state.isTreeGraphMode"
                @node-click="handleGraphNodeClick"
              />
            </div>
            <!-- 知识详情（总览模式下隐藏） -->
            <div v-if="!state.isOverviewMode" class="flex gap-4 mt-4 h-[420px]">
              <div class="w-1/2 bg-gray-100 rounded-lg p-4 flex flex-col min-h-0">
                <div class="flex items-center justify-between mb-4 flex-shrink-0">
                  <span class="font-bold text-lg">节点详情</span>
                  <div v-if="state.selectedNodeDetails" class="flex gap-2">
                    <button
                      class="btn btn-xs btn-primary"
                      @click="openEditNodeModal()"
                      :disabled="loading"
                    >
                      编辑
                    </button>
                    <button
                      class="btn btn-xs btn-error"
                      @click="confirmDeleteNode()"
                      :disabled="loading"
                    >
                      删除
                    </button>
                  </div>
                </div>
                <div class="overflow-y-auto flex-1 min-h-0">
                  <div v-if="!state.selectedNodeDetails" class="text-center py-8 text-gray-500">
                    请选择一个节点查看详情
                  </div>
                  <table
                    v-else
                    class="table table-fixed w-full text-sm bg-white rounded shadow border border-gray-300"
                  >
                    <tbody>
                      <tr class="border-b border-gray-300">
                        <td class="font-semibold py-1 px-2 border-r border-gray-300 w-28 align-top">节点ID</td>
                        <td class="py-1 px-2 break-all">
                          {{ state.selectedNodeDetails.id || '未知' }}
                        </td>
                      </tr>
                      <tr
                        v-for="(value, key) in state.selectedNodeDetails.properties"
                        :key="key"
                        class="border-b border-gray-300"
                      >
                        <td class="font-semibold py-1 px-2 border-r border-gray-300 w-28 align-top">
                          {{ key }}
                        </td>
                        <td class="py-1 px-2">
                          <div
                            class="break-all whitespace-pre-wrap"
                            v-html="
                              state.searchResult
                                ? highlightSearchTerm(String(value), state.searchResult.query)
                                : value
                            "
                          ></div>
                        </td>
                      </tr>
                      <tr class="border-b border-gray-300">
                        <td class="font-semibold py-1 px-2 border-r border-gray-300 align-top">标签</td>
                        <td class="py-1 px-2">
                          <div class="flex flex-wrap gap-1">
                            <span
                              v-for="label in state.selectedNodeDetails.labels"
                              :key="label"
                              class="badge badge-sm"
                            >
                              {{ label }}
                            </span>
                          </div>
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </div>
              <div class="w-1/2 bg-gray-100 rounded-lg p-4 flex flex-col min-h-0">
                <div class="flex items-center justify-between mb-4">
                  <span class="font-bold text-lg">关系详情</span>
                </div>
                <div class="overflow-y-auto flex-1 min-h-0">
                <div
                  v-if="!state.selectedNodeRelations.length"
                  class="text-center py-8 text-gray-500"
                >
                  {{ state.selectedNodeDetails ? '该节点暂无关系' : '请选择一个节点查看关系' }}
                </div>
                <ul v-else class="space-y-3">
                  <li
                    v-for="relation in state.selectedNodeRelations"
                    :key="relation.relation.id"
                    class="relative group bg-white rounded border border-gray-300 px-4 py-3 flex items-center justify-between hover:bg-gray-50 transition"
                  >
                    <div class="flex flex-col">
                      <span class="font-semibold text-base text-primary">{{
                        relation.relation.type
                      }}</span>
                      <span class="text-xs text-gray-500">
                        {{ relation.start.properties.name || relation.start.id }} →
                        {{ relation.end.properties.name || relation.end.id }}
                      </span>
                    </div>
                    <button
                      class="btn btn-xs btn-primary ml-4"
                      @click="openEditRelationModal(relation)"
                      :disabled="loading"
                    >
                      编辑
                    </button>
                    <div
                      class="absolute left-1/2 top-full mt-2 -translate-x-1/2 w-64 bg-white text-xs text-gray-700 rounded shadow p-3 opacity-0 group-hover:opacity-100 transition-opacity pointer-events-none z-10 border border-gray-200"
                    >
                      <div>
                        <span class="font-semibold">关系ID：</span>{{ relation.relation.id }}
                      </div>
                      <div v-for="(value, key) in relation.relation.properties" :key="key">
                        <span class="font-semibold">{{ key }}：</span>{{ value }}
                      </div>
                      <div>
                        <span class="font-semibold">起点：</span
                        >{{ relation.start.properties.name || relation.start.id }} (ID:
                        {{ relation.start.id }})
                      </div>
                      <div>
                        <span class="font-semibold">终点：</span
                        >{{ relation.end.properties.name || relation.end.id }} (ID:
                        {{ relation.end.id }})
                      </div>
                    </div>
                  </li>
                </ul>
                <div v-if="state.selectedNodeRelations.length" class="text-xs text-gray-500 mt-2">
                  鼠标悬停每条关系可查看详细字段
                </div>
                </div>
              </div>
            </div>
            </template>

            <template v-else>
              <div class="grid grid-cols-1 xl:grid-cols-3 gap-4">
                <div class="xl:col-span-2 bg-white rounded-lg border border-gray-200 p-4">
                  <div class="flex items-center justify-between mb-3">
                    <div>
                      <div class="font-bold text-lg">
                        {{ activeKnowledgeBase?.name || '知识库' }}目录视图
                      </div>
                      <div class="text-xs text-gray-500 mt-1">
                        该知识库以实体检索为主，不展示关系图谱
                      </div>
                    </div>
                    <div class="flex items-center gap-3">
                      <div class="flex items-center gap-1 text-xs text-gray-500">
                        <span>每页</span>
                        <select
                          v-model="catalogPageSize"
                          class="select select-xs select-bordered w-14"
                          @change="onCatalogPageSizeChange"
                        >
                          <option :value="10">10</option>
                          <option :value="20">20</option>
                        </select>
                        <span>条</span>
                      </div>
                      <div class="text-sm text-gray-600">
                        共 {{ isEsPaginated ? catalogTotal : catalogDisplayNodes.length }} 条
                      </div>
                    </div>
                  </div>
                  <div class="max-h-[calc(100vh-300px)] overflow-auto border rounded-lg">
                    <table class="table table-pin-rows table-sm w-full">
                      <thead>
                        <tr>
                          <th>名称</th>
                          <th>类型</th>
                          <th>标签</th>
                        </tr>
                      </thead>
                      <tbody>
                        <tr
                          v-for="node in catalogPagedNodes"
                          :key="node.id"
                          class="cursor-pointer hover:bg-base-200"
                          :class="{ 'bg-primary/10': state.selectedNode?.id === node.id }"
                          @click="selectNode(node)"
                        >
                          <td class="max-w-[320px] truncate">{{ node.properties?.name || node.id }}</td>
                          <td>{{ node.properties?.entity_type || node.properties?.type || '-' }}</td>
                          <td class="max-w-[260px] truncate">{{ (node.labels || []).join(' · ') }}</td>
                        </tr>
                        <tr v-if="(isEsPaginated ? catalogTotal : catalogDisplayNodes.length) === 0">
                          <td colspan="3" class="text-center text-gray-400 py-8">
                            {{ state.searchResult ? '未找到匹配数据' : '请选择分类或搜索内容' }}
                          </td>
                        </tr>
                      </tbody>
                    </table>
                  </div>
                  <!-- 分页控制 -->
                  <div v-if="catalogTotalPages > 1" class="flex items-center justify-center gap-2 mt-3">
                    <button
                      class="btn btn-xs btn-ghost"
                      :disabled="catalogPage === 1"
                      @click="catalogPage = 1"
                    >«</button>
                    <button
                      class="btn btn-xs btn-ghost"
                      :disabled="catalogPage === 1"
                      @click="catalogPage--"
                    >‹</button>
                    <span class="text-xs text-gray-600">{{ catalogPage }} / {{ catalogTotalPages }}</span>
                    <button
                      class="btn btn-xs btn-ghost"
                      :disabled="catalogPage === catalogTotalPages"
                      @click="catalogPage++"
                    >›</button>
                    <button
                      class="btn btn-xs btn-ghost"
                      :disabled="catalogPage === catalogTotalPages"
                      @click="catalogPage = catalogTotalPages"
                    >»</button>
                  </div>
                </div>

                <div class="bg-gray-100 rounded-lg p-4 max-h-[calc(100vh-260px)] overflow-y-auto">
                  <div class="font-bold text-lg mb-3">实体详情</div>
                  <div v-if="!state.selectedNodeDetails" class="text-sm text-gray-500 py-6 text-center">
                    请选择一条数据查看详情
                  </div>
                  <div v-else class="space-y-2">
                    <div class="bg-white rounded border px-3 py-2">
                      <div class="text-xs text-gray-500">节点ID</div>
                      <div class="text-sm break-all">{{ state.selectedNodeDetails.id }}</div>
                    </div>
                    <div class="bg-white rounded border px-3 py-2">
                      <div class="text-xs text-gray-500">标签</div>
                      <div class="flex flex-wrap gap-1 mt-1">
                        <span
                          v-for="label in state.selectedNodeDetails.labels || []"
                          :key="label"
                          class="badge badge-sm"
                          >{{ label }}</span
                        >
                      </div>
                    </div>
                    <div class="bg-white rounded border px-3 py-2 max-h-[420px] overflow-auto">
                      <div class="text-xs text-gray-500 mb-2">属性</div>
                      <div
                        v-for="(value, key) in state.selectedNodeDetails.properties || {}"
                        :key="key"
                        class="mb-2 border-b border-gray-100 pb-2"
                      >
                        <div class="text-xs font-medium text-gray-600">{{ key }}</div>
                        <div class="text-sm break-all">{{ value }}</div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </template>
          </div>
        </div>

        <!-- 右侧知识列表 -->
        <KnowledgeList
          v-if="showGraphPresentation"
          :knowledgeList="displayKnowledgeList"
          :searchResult="state.searchResult"
          :selectedCategory="state.selectedCategory"
          :selectedNode="state.selectedNode"
          :selectedNodeDetails="state.selectedNodeDetails"
          :canResolveConflict="generationState.canResolveConflict"
          :isOverviewMode="state.isOverviewMode"
          @select-node="selectNode"
          @open-generation="openKnowledgeGeneration"
          @open-conflict-resolution="openConflictResolution"
        />
      </div>
    </div>
  </div>

  <!-- 节点表单模态框 -->
  <NodeFormModal
    :show="modals.addNode || modals.editNode"
    :isEdit="modals.editNode"
    :nodeForm="nodeForm"
    :loading="loading"
    :propertyErrors="propertyErrors"
    :addNodeProperty="addNodeProperty"
    :deleteNodeProperty="deleteNodeProperty"
    :updateNodeProperty="updateNodeProperty"
    :validateAllProperties="validateAllNodeProperties"
    @submit="modals.editNode ? handleUpdateNode() : handleCreateNode()"
    @cancel="
      modals.addNode = false;
      modals.editNode = false;
    "
  />

  <!-- 关系表单模态框 -->
  <RelationFormModal
    :show="modals.addRelation || modals.editRelation"
    :isEdit="modals.editRelation"
    :relationForm="relationForm"
    :loading="loading"
    :propertyErrors="relationPropertyErrors"
    :addRelationProperty="addRelationProperty"
    :deleteRelationProperty="deleteRelationProperty"
    :updateRelationProperty="updateRelationProperty"
    :validateAllProperties="validateAllRelationProperties"
    @submit="modals.editRelation ? handleUpdateRelation() : handleCreateRelation()"
    @cancel="
      modals.addRelation = false;
      modals.editRelation = false;
    "
    @delete="confirmDeleteRelation"
    @start-search="onStartNodeSearch"
    @end-search="onEndNodeSearch"
    @select-start="selectStartNode"
    @select-end="selectEndNode"
    @start-focus="onStartNodeFocus"
    @end-focus="onEndNodeFocus"
    @start-blur="onStartNodeBlur"
    @end-blur="onEndNodeBlur"
  />

  <!-- 导入知识模态框 -->
  <ImportModal
    :show="modals.importKnowledge"
    :importForm="importForm"
    :loading="loading"
    @submit="handleImport"
    @cancel="modals.importKnowledge = false"
  />

  <!-- 创建融合场景模态框 -->
  <FusionScenarioModal
    :show="modals.createFusionScenario"
    :categoryList="state.eventCategoryList"
    :loading="loading"
    @submit="handleCreateFusionScenario"
    @cancel="modals.createFusionScenario = false"
  />

  <!-- 融合加载提示 -->
  <FusionLoadingModal :show="modals.fusionLoading" />

  <!-- 融合生成结果模态框 -->
  <FusionGenerationResult
    ref="fusionGenerationResultRef"
    :show="modals.fusionResult"
    :relations="fusionGenerationData.relations"
    @close="handleFusionResultCancel"
    @import="handleFusionImport"
  />

  <!-- 知识生成模态框 -->
  <KnowledgeImportModal
    :show="generationState.showGenerationModal"
    :loading="generationLoading"
    @submit="handleKnowledgeGeneration"
    @cancel="closeGenerationModal"
  />

  <!-- 知识生成加载提示 -->
  <GenerationLoadingModal
    :show="generationState.showGenerationLoadingModal"
    :type="generationState.originalInputType"
  />

  <!-- 知识生成结果模态框 -->
  <KnowledgeGenerationResult
    :show="generationState.showGenerationResultModal"
    :nodes="generationState.generatedNodes"
    @close="closeGenerationResult"
  />

  <!-- 冲突消解加载提示 -->
  <ConflictResolutionLoadingModal
    :show="generationState.showConflictLoadingModal"
    :totalNodes="generationState.totalNodes"
    :processedNodes="generationState.processedNodes"
    :phase="generationState.conflictResolutionPhase"
  />

  <!-- 冲突消解结果模态框 -->
  <ConflictResolutionResult
    :show="generationState.showConflictResultModal"
    :resolutionResults="generationState.resolutionResults"
    :loading="generationLoading"
    @insert="handleInsertToKnowledgeBase"
    @cancel="cancelConflictResolution"
  />
</template>

<script setup>
import { ref, onMounted, reactive, nextTick, computed, watch } from 'vue';
import KnowledgeGraph from '../components/knowledge/KnowledgeGraph.vue';
import Toast from '../components/common/Toast.vue';
import ConfirmModal from '../components/common/ConfirmModal.vue';
import CategorySidebar from '../components/knowledge/CategorySidebar.vue';
import KnowledgeList from '../components/knowledge/KnowledgeList.vue';
import Breadcrumbs from '../components/knowledge/Breadcrumbs.vue';
import NodeFormModal from '../components/knowledge/modals/NodeFormModal.vue';
import RelationFormModal from '../components/knowledge/modals/RelationFormModal.vue';
import ImportModal from '../components/knowledge/modals/ImportModal.vue';
import FusionScenarioModal from '../components/knowledge/modals/FusionModal.vue';
import FusionLoadingModal from '../components/knowledge/modals/FusionLoadingModal.vue';
import FusionGenerationResult from '../components/knowledge/modals/FusionResult.vue';
import KnowledgeImportModal from '../components/knowledge/modals/GenModal.vue';
import GenerationLoadingModal from '../components/knowledge/modals/GenLoadingModal.vue';
import ConflictResolutionLoadingModal from '../components/knowledge/modals/ConflictResolutionLoadingModal.vue';
import KnowledgeGenerationResult from '../components/knowledge/modals/GenResult.vue';
import ConflictResolutionResult from '../components/knowledge/modals/ConflictResolutionResult.vue';

import { useToast } from '../composables/useToast';
import { useConfirm } from '../composables/useConfirm';
import { useKnowledgeApi } from '../composables/knowledge/useKnowledgeApi';
import { useNodeForm } from '../composables/knowledge/useNodeForm';
import { useRelationForm } from '../composables/knowledge/useRelationForm';
import { useKnowledgeGeneration } from '../composables/knowledge/useKnowledgeGeneration';
import { highlightSearchTerm } from '../utils/knowledgeUtils';

// Toast 系统
const { toasts, showToast, removeToast } = useToast();

// 确认对话框
const { confirmModal, showConfirm, handleConfirm, handleCancel } = useConfirm();

// API
const OVERVIEW_NODE_LIMIT = 200; // 总览模式最多加载的节点数
const { loading, api } = useKnowledgeApi();
const knowledgeBases = ref([]);
const activeKnowledgeBaseId = ref(api.getActiveKnowledgeBase());
const activeStorageSource = ref(api.getActiveStorageSource());

// 节点表单
const {
  nodeForm,
  propertyErrors,
  initNodeForm,
  addNodeProperty,
  deleteNodeProperty,
  updateNodeProperty,
  validateAllProperties: validateAllNodeProperties,
  getPropertiesObject: getNodePropertiesObject,
} = useNodeForm();

// 搜索节点函数
const searchNodesByName = async (searchTerm) => {
  if (!searchTerm || !searchTerm.trim()) {
    return [];
  }
  const result = await api.queryNodeByName(searchTerm);
  if (result.flag && result.data && Array.isArray(result.data)) {
    return result.data.slice(0, 10);
  }
  return [];
};

// 关系表单
const {
  relationForm,
  propertyErrors: relationPropertyErrors,
  initRelationForm,
  onStartNodeSearch,
  onEndNodeSearch,
  selectStartNode,
  selectEndNode,
  onStartNodeFocus,
  onEndNodeFocus,
  onStartNodeBlur,
  onEndNodeBlur,
  addRelationProperty,
  deleteRelationProperty,
  updateRelationProperty,
  validateAllProperties: validateAllRelationProperties,
  getPropertiesObject: getRelationPropertiesObject,
} = useRelationForm(searchNodesByName);

// 知识生成和冲突消解
const {
  loading: generationLoading,
  generationState,
  openGenerationModal,
  closeGenerationModal,
  startGeneration,
  startConflictResolution,
  insertToKnowledgeBase,
  closeGenerationResult,
  cancelConflictResolution,
  confirmAndReset,
  resetGenerationState,
} = useKnowledgeGeneration();

// 响应式数据状态
const state = reactive({
  searchQuery: '',
  selectedCategory: null,
  selectedNode: null,
  selectedNodeDetails: null,
  selectedNodeRelations: [],
  searchResult: null,
  categories: {
    entity: { main: [], detail: [] },
    event: { main: [], detail: [] },
    model: { main: [], detail: [] },
  },
  categoryCounts: {
    entity: {},
    event: {},
    model: {},
    tree: {},
  },
  knowledgeList: [],
  treeKnowledgeList: [], // 策略树知识列表
  allNodes: [],
  allRelations: [],
  eventList: [],
  eventCategoryList: [], // 事件分类列表
  isOverviewMode: false, // 是否处于总览模式
  treeGraphNodes: [], // tree节点查询返回的所有节点（用于图谱展示）
  treeGraphRelations: [], // tree节点查询返回的所有关系（用于图谱展示）
  isTreeGraphMode: false, // 是否处于tree节点图谱模式
});

// 计算当前显示的知识列表（避免闪烁）
const displayKnowledgeList = computed(() => {
  return state.isOverviewMode || state.isTreeGraphMode
    ? state.treeKnowledgeList
    : state.knowledgeList;
});

const activeKnowledgeBase = computed(() => {
  return knowledgeBases.value.find((item) => item.id === activeKnowledgeBaseId.value) || null;
});

const activeKnowledgeBaseSources = computed(() => {
  if (!activeKnowledgeBase.value) return ['NEO4J', 'ES'];
  if (Array.isArray(activeKnowledgeBase.value.availableSources)) {
    return activeKnowledgeBase.value.availableSources;
  }
  return ['NEO4J', 'ES'];
});

const showGraphPresentation = computed(() => {
  if (state.isOverviewMode || state.isTreeGraphMode) return true;
  // ES 模式下，选择分类或搜索后直接展示列表视图
  if (activeStorageSource.value === 'ES' && (state.selectedCategory || state.searchResult)) {
    return false;
  }
  return true;
});

const visibleCategoryTypes = computed(() => ['entity']);

const sidebarSearchPlaceholder = computed(() => {
  if (activeKnowledgeBaseId.value === 'fault-kb') return '搜索故障节点...';
  if (activeKnowledgeBaseId.value === 'promcopilot') return '搜索系统上下文实体...';
  if (activeKnowledgeBaseId.value === 'logcopilot') return '搜索日志知识...';
  return '搜索知识...';
});

const isReadOnlyKnowledgeBase = computed(() => {
  if (!activeKnowledgeBase.value) return false;
  const sourceSettings = activeKnowledgeBase.value.sourceSettings || {};
  const currentSource = sourceSettings[activeStorageSource.value];
  if (currentSource && typeof currentSource.readOnly === 'boolean') {
    return currentSource.readOnly;
  }
  return !!activeKnowledgeBase.value.readOnly;
});

const catalogDisplayNodes = computed(() => {
  if (state.searchResult) return state.knowledgeList || [];
  if (state.selectedCategory) return state.knowledgeList || [];
  return state.allNodes || [];
});

// ES 目录分页
const catalogPageSize = ref(10);
const catalogPage = ref(1);
const catalogTotal = ref(0);      // ES 服务端返回的总条数
const isEsPaginated = ref(false); // 当前是否为 ES 服务端分页模式

const catalogTotalPages = computed(() => {
  if (isEsPaginated.value) {
    return Math.max(1, Math.ceil(catalogTotal.value / catalogPageSize.value));
  }
  return Math.max(1, Math.ceil(catalogDisplayNodes.value.length / catalogPageSize.value));
});
const catalogPagedNodes = computed(() => {
  if (isEsPaginated.value) {
    return state.knowledgeList || [];
  }
  const start = (catalogPage.value - 1) * catalogPageSize.value;
  return catalogDisplayNodes.value.slice(start, start + catalogPageSize.value);
});
// 非 ES 模式下列表切换时重置页码
watch(catalogDisplayNodes, () => {
  if (!isEsPaginated.value) catalogPage.value = 1;
});
// ES 分页模式下翻页时重新拉取后端数据
watch(catalogPage, async (newPage) => {
  if (isEsPaginated.value && state.selectedCategory) {
    await fetchCatalogPage(newPage);
  }
});

const fetchCatalogPage = async (page) => {
  if (!state.selectedCategory) return;
  const { main, detail } = state.selectedCategory;
  const result = await api.queryNodeByCategory(main, detail, page, catalogPageSize.value);
  if (result.flag && result.data && result.data.nodes !== undefined) {
    state.knowledgeList = result.data.nodes;
    catalogTotal.value = Number(result.data.total) || 0;
  }
};

const onCatalogPageSizeChange = async () => {
  if (isEsPaginated.value && state.selectedCategory) {
    if (catalogPage.value === 1) {
      await fetchCatalogPage(1);
    } else {
      catalogPage.value = 1; // 触发 watch 自动拉取
    }
  } else {
    catalogPage.value = 1;
  }
};

const goKnowledgeBaseHome = async () => {
  clearSearch();
  clearCategory();
  clearOverview();
  state.selectedNode = null;
  state.selectedNodeDetails = null;
  state.selectedNodeRelations = [];
  await reloadKnowledgeBaseData();
};

// 模态框状态
const modals = reactive({
  addNode: false,
  editNode: false,
  addRelation: false,
  editRelation: false,
  importKnowledge: false,
  createFusionScenario: false,
  fusionLoading: false,
  fusionResult: false,
});

// 融合生成结果组件引用
const fusionGenerationResultRef = ref(null);

// 导入表单
const importForm = reactive({
  file: null,
  type: 'node',
});

// 融合生成数据
const fusionGenerationData = reactive({
  relations: [],
});

// 重置表单
const resetImportForm = () => {
  importForm.file = null;
  importForm.type = 'node';
};

const ensureWritableKnowledgeBase = () => {
  if (!isReadOnlyKnowledgeBase.value) return true;
  showToast('当前知识库为只读（ES 数据源），不支持新增/编辑/删除/导入', 'warning');
  return false;
};

const resetKnowledgeBaseViewState = () => {
  state.searchQuery = '';
  state.selectedCategory = null;
  state.selectedNode = null;
  state.selectedNodeDetails = null;
  state.selectedNodeRelations = [];
  state.searchResult = null;
  state.knowledgeList = [];
  state.treeKnowledgeList = [];
  state.allNodes = [];
  state.allRelations = [];
  state.eventList = [];
  state.eventCategoryList = [];
  state.isOverviewMode = false;
  state.treeGraphNodes = [];
  state.treeGraphRelations = [];
  state.isTreeGraphMode = false;
};

const loadKnowledgeBaseList = async () => {
  const result = await api.listKnowledgeBases();
  if (!result.flag || !Array.isArray(result.data)) {
    showToast(result.data || '知识库列表加载失败', 'error');
    return;
  }

  const kbNameMap = {
    'fault-kb': '故障知识库',
    promcopilot: '系统上下文知识库',
    logcopilot: '日志知识库',
  };
  knowledgeBases.value = result.data.map((item) => ({
    ...item,
    name: kbNameMap[item.id] || item.name || item.id,
  }));
  if (knowledgeBases.value.some((item) => item.id === activeKnowledgeBaseId.value)) {
    const currentKb = knowledgeBases.value.find((item) => item.id === activeKnowledgeBaseId.value);
    const currentSources = Array.isArray(currentKb?.availableSources) ? currentKb.availableSources : [];
    if (!currentSources.includes(activeStorageSource.value)) {
      const fallbackSource = currentKb?.defaultStorageType || currentSources[0] || 'NEO4J';
      activeStorageSource.value = fallbackSource;
      api.setActiveStorageSource(fallbackSource);
    }
    return;
  }

  const defaultKb =
    knowledgeBases.value.find((item) => item.defaultSelected) || knowledgeBases.value[0] || null;
  if (defaultKb) {
    activeKnowledgeBaseId.value = defaultKb.id;
    api.setActiveKnowledgeBase(defaultKb.id);
    const defaultSource = defaultKb.defaultStorageType || defaultKb.sourceType || 'NEO4J';
    activeStorageSource.value = defaultSource;
    api.setActiveStorageSource(defaultSource);
  }
};

const reloadKnowledgeBaseData = async () => {
  await loadCategories();
  // 切换知识库/存储时不立即拉取全量图数据，避免大数据量导致切换卡顿。
  state.allNodes = [];
  state.allRelations = [];
};

const switchKnowledgeBase = async (kbId) => {
  if (!kbId || kbId === activeKnowledgeBaseId.value) return;
  activeKnowledgeBaseId.value = kbId;
  api.setActiveKnowledgeBase(kbId);
  const kb = knowledgeBases.value.find((item) => item.id === kbId);
  const targetSource =
    kb?.defaultStorageType || (Array.isArray(kb?.availableSources) ? kb.availableSources[0] : null) || 'NEO4J';
  activeStorageSource.value = targetSource;
  api.setActiveStorageSource(targetSource);
  resetKnowledgeBaseViewState();
  await reloadKnowledgeBaseData();
};

const switchStorageSource = async (source) => {
  if (!source || source === activeStorageSource.value) return;
  activeStorageSource.value = source;
  api.setActiveStorageSource(source);
  resetKnowledgeBaseViewState();
  await reloadKnowledgeBaseData();
};

// 搜索处理
const handleSearch = async () => {
  if (!state.searchQuery.trim()) {
    clearSearch();
    return;
  }

  // 退出总览模式
  state.isOverviewMode = false;
  state.allNodes = [];
  state.allRelations = [];
  state.treeKnowledgeList = [];

  const result = await api.queryNodeByName(state.searchQuery);
  if (result.flag && result.data && Array.isArray(result.data)) {
    if (result.data.length > 0) {
      state.searchResult = {
        query: state.searchQuery,
        nodes: result.data,
        count: result.data.length,
      };
      state.selectedCategory = null;
      state.knowledgeList = result.data;
      if (result.data.length === 1) {
        selectNode(result.data[0]);
      } else {
        state.selectedNode = null;
        state.selectedNodeDetails = null;
        state.selectedNodeRelations = [];
      }
    } else {
      state.searchResult = {
        query: state.searchQuery,
        nodes: [],
        count: 0,
      };
      state.knowledgeList = [];
      state.selectedNode = null;
      state.selectedNodeDetails = null;
      state.selectedNodeRelations = [];
      showToast('未找到匹配的节点', 'error');
    }
  } else {
    showToast(result.data || '搜索请求失败', 'error');
    state.knowledgeList = [];
    state.searchResult = null;
  }
};

const clearSearch = () => {
  state.searchResult = null;
  state.searchQuery = '';
  state.knowledgeList = [];
  state.selectedNodeDetails = null;
  state.selectedNodeRelations = [];
  state.selectedNode = null;
  state.isTreeGraphMode = false;
  state.treeGraphNodes = [];
  state.treeGraphRelations = [];
};

const clearCategory = () => {
  state.selectedCategory = null;
  state.knowledgeList = [];
  isEsPaginated.value = false;
  catalogTotal.value = 0;
  catalogPage.value = 1;
  state.selectedNodeDetails = null;
  state.selectedNodeRelations = [];
  state.selectedNode = null;
  state.isTreeGraphMode = false;
  state.treeGraphNodes = [];
  state.treeGraphRelations = [];
};

const clearSearchInput = () => {
  state.searchQuery = '';
  clearSearch();
};

// 总览模式处理
const handleOverviewClick = async () => {
  // 清除其他状态
  state.searchResult = null;
  state.searchQuery = '';
  state.selectedCategory = null;
  state.selectedNodeDetails = null;
  state.selectedNodeRelations = [];
  state.isTreeGraphMode = false; // 清除tree图谱模式
  state.treeGraphNodes = [];
  state.treeGraphRelations = [];

  // 进入总览模式
  state.isOverviewMode = true;

  // 后端只拉取200条，减少传输量
  const allData = await api.getAll(OVERVIEW_NODE_LIMIT);
  if (allData.flag && allData.data) {
    const rawNodes = allData.data[0] || [];
    const rawRelations = allData.data[1] || [];
    state.allNodes = rawNodes;
    const visibleNodeIds = new Set(state.allNodes.map((node) => node.id));
    state.allRelations = rawRelations.filter((rel) => {
      const sid = rel?.start?.id;
      const tid = rel?.end?.id;
      return visibleNodeIds.has(sid) && visibleNodeIds.has(tid);
    });
    state.treeKnowledgeList = state.allNodes;

    // 总览模式下不默认选择节点，让用户从右侧列表中选择
    state.selectedNode = null;

    console.log('总览模式数据:', {
      总节点数: rawNodes.length,
      总关系数: rawRelations.length,
      总览列表数量: state.treeKnowledgeList.length,
    });

    showToast(
      `已加载 ${state.allNodes.length} 个节点, ${state.allRelations.length} 个关系`,
      'success',
    );
  } else {
    showToast('获取数据失败', 'error');
  }
};

const clearOverview = () => {
  state.isOverviewMode = false;
  state.allNodes = [];
  state.allRelations = [];
  state.treeKnowledgeList = [];
  state.selectedNode = null;
  state.selectedNodeDetails = null;
  state.selectedNodeRelations = [];
  state.isTreeGraphMode = false;
  state.treeGraphNodes = [];
  state.treeGraphRelations = [];
};

// 分类选择
const selectCategory = async (categoryType, categoryMain, categoryDetail = null) => {
  state.searchResult = null;
  state.searchQuery = '';
  state.selectedNode = null;
  state.selectedNodeDetails = null;
  state.selectedNodeRelations = [];
  state.isOverviewMode = false; // 退出总览模式
  state.allNodes = [];
  state.allRelations = [];
  state.treeKnowledgeList = [];
  state.isTreeGraphMode = false; // 退出tree图谱模式
  state.treeGraphNodes = [];
  state.treeGraphRelations = [];

  state.selectedCategory = { type: categoryType, main: categoryMain, detail: categoryDetail };
  catalogPage.value = 1;
  isEsPaginated.value = false;

  const result = await api.queryNodeByCategory(categoryMain, categoryDetail, 1, catalogPageSize.value);
  if (result.flag) {
    if (result.data && result.data.nodes !== undefined) {
      // ES 服务端分页：只返回第一页数据
      state.knowledgeList = result.data.nodes;
      catalogTotal.value = Number(result.data.total) || 0;
      isEsPaginated.value = true;
    } else {
      // Neo4j 客户端分页
      state.knowledgeList = Array.isArray(result.data) ? result.data : [];
      catalogTotal.value = state.knowledgeList.length;
      isEsPaginated.value = false;
    }
  } else {
    state.knowledgeList = [];
    catalogTotal.value = 0;
    isEsPaginated.value = false;
  }
};

// 选择节点
const selectNode = async (node) => {
  console.log('=== 选择节点 ===', node);

  // 先判断是否为tree节点
  const isTreeNode = node?.labels?.some((label) => label.toLowerCase() === 'tree');
  console.log('节点labels:', node?.labels, '是否为tree节点:', isTreeNode);

  // 更新基本状态
  state.selectedNode = node;
  state.selectedNodeDetails = node;
  state.selectedNodeRelations = [];

  // 根据节点类型决定是否重置tree图谱模式，避免闪烁
  if (!isTreeNode) {
    // 只有在非tree节点时才重置tree图谱模式
    state.treeGraphNodes = [];
    state.treeGraphRelations = [];
    state.isTreeGraphMode = false;
  }

  if (isTreeNode) {
    // tree节点使用queryGraph接口获取图谱数据，以总览模式展示
    const nodeName = node.properties?.name || node.id;
    console.log(`查询Tree节点图谱: ${nodeName}`);

    // 如果treeKnowledgeList为空，需要先加载所有tree节点
    if (state.treeKnowledgeList.length === 0 && state.allNodes.length > 0) {
      state.treeKnowledgeList = state.allNodes.filter(
        (n) => n.labels && n.labels.some((label) => label.toLowerCase() === 'tree'),
      );
    } else if (state.treeKnowledgeList.length === 0) {
      // 如果allNodes也为空，尝试加载所有数据（限制200条）
      const allData = await api.getAll(OVERVIEW_NODE_LIMIT);
      if (allData.flag && allData.data) {
        state.allNodes = allData.data[0] || [];
        state.allRelations = allData.data[1] || [];
        state.treeKnowledgeList = state.allNodes.filter(
          (n) => n.labels && n.labels.some((label) => label.toLowerCase() === 'tree'),
        );
      }
    }

    // 原子性地切换到tree图谱模式
    state.isOverviewMode = false;
    state.isTreeGraphMode = true;

    const graphResult = await api.queryGraph(nodeName, 1);

    if (graphResult.flag && graphResult.data) {
      // queryGraph返回的数据格式包含nodes和relations
      let relations = graphResult.data.relations || [];
      let nodes = graphResult.data.nodes || [];

      // 限制邻居数量，防止高度数节点导致渲染爆炸
      const NEIGHBOR_LIMIT = 10;
      if (relations.length > NEIGHBOR_LIMIT) {
        relations = relations.slice(0, NEIGHBOR_LIMIT);
        // 重新计算需要保留的节点
        const neededNodeIds = new Set();
        relations.forEach(rel => {
          neededNodeIds.add(rel.startNodeId);
          neededNodeIds.add(rel.endNodeId);
        });
        nodes = nodes.filter(n => neededNodeIds.has(n.id));
      }

      console.log(`Tree图谱数据: ${nodes.length} 个节点, ${relations.length} 个关系`);

      // 更新图谱数据
      state.treeGraphNodes = nodes;
      console.log('✅ 设置tree图谱模式:', {
        isTreeGraphMode: state.isTreeGraphMode,
        isOverviewMode: state.isOverviewMode,
        节点数: nodes.length,
        关系数: relations.length,
      });

      // 将relations转换为与allRelations相同的格式（用于总览模式）
      state.treeGraphRelations = relations.map((rel) => {
        const startNode = nodes.find((n) => n.id === rel.startNodeId);
        const endNode = nodes.find((n) => n.id === rel.endNodeId);
        return {
          start: startNode || { id: rel.startNodeId, properties: {}, labels: [] },
          end: endNode || { id: rel.endNodeId, properties: {}, labels: [] },
          relation: {
            id: rel.id,
            type: rel.type,
            properties: rel.properties || {},
          },
        };
      });

      // 同时保存到selectedNodeRelations用于关系详情显示
      state.selectedNodeRelations = state.treeGraphRelations;

      // 性能提示
      if (nodes.length > 100) {
        showToast(`Tree图谱数据量较大(${nodes.length}个节点)，加载可能需要一些时间`, 'info');
      }
    } else {
      console.warn('查询Tree图谱失败或无数据');
      // 查询失败时清空图谱数据，但保持tree模式
      state.selectedNodeRelations = [];
      state.treeGraphNodes = [];
      state.treeGraphRelations = [];
      showToast('查询Tree图谱失败', 'error');
    }
  } else {
    // 普通节点使用原有的getNodeRelation接口
    const relationResult = await api.getNodeRelation(node);
    if (relationResult.flag && relationResult.data && relationResult.data.length > 0) {
      const relations = relationResult.data[0] || [];
      // 限制邻居数量，防止高度数节点导致渲染爆炸
      const NEIGHBOR_LIMIT = 10;
      state.selectedNodeRelations = relations.length > NEIGHBOR_LIMIT ? relations.slice(0, NEIGHBOR_LIMIT) : relations;
    } else {
      state.selectedNodeRelations = [];
    }
  }
};

// 图谱节点点击
const handleGraphNodeClick = async (nodeData) => {
  if (state.selectedNode?.id !== nodeData.id) {
    await selectNode(nodeData);
  }
};

// 模态框操作
const openAddNodeModal = () => {
  if (!ensureWritableKnowledgeBase()) return;
  initNodeForm();
  modals.addNode = true;
};

const openEditNodeModal = () => {
  if (!ensureWritableKnowledgeBase()) return;
  initNodeForm(state.selectedNodeDetails);
  modals.editNode = true;
};

const openAddRelationModal = () => {
  if (!ensureWritableKnowledgeBase()) return;
  initRelationForm();
  modals.addRelation = true;
};

const openEditRelationModal = (relation) => {
  if (!ensureWritableKnowledgeBase()) return;
  initRelationForm(relation);
  modals.editRelation = true;
};

const openCSVImportModal = () => {
  if (!ensureWritableKnowledgeBase()) return;
  resetImportForm();
  modals.importKnowledge = true;
};

const openCreateFusionScenarioModal = async () => {
  if (!ensureWritableKnowledgeBase()) return;
  try {
    // 先打开模态框（FusionScenarioModal组件会自动重置状态）
    modals.createFusionScenario = true;
    // 加载事件类知识分类列表
    const eventCategories = await api.getEventCategories();

    if (eventCategories.flag && eventCategories.data) {
      // 将分类数据转换为数组格式
      const categoryList = Object.keys(eventCategories.data).map((categoryName) => ({
        name: categoryName,
        count: eventCategories.data[categoryName],
      }));

      state.eventCategoryList = categoryList;

      if (categoryList.length === 0) {
        showToast('没有找到事件分类', 'warning');
      }
    } else {
      state.eventCategoryList = [];
      showToast('未能加载事件分类列表', 'warning');
    }
  } catch (error) {
    showToast('加载事件分类列表失败: ' + error.message, 'error');
    state.eventCategoryList = [];
  }
};

// 节点操作
const handleCreateNode = async () => {
  if (!ensureWritableKnowledgeBase()) return;
  if (!nodeForm.labels.length) {
    showToast('请添加至少一个标签', 'error');
    return;
  }
  const properties = getNodePropertiesObject();
  if (!properties.name || !properties.name.trim()) {
    showToast('请填写节点名称', 'error');
    return;
  }
  const nodeData = {
    labels: nodeForm.labels,
    properties: properties,
  };

  const result = await api.createNode(nodeData);

  if (result.flag) {
    showToast('节点创建成功');
    modals.addNode = false;
    await loadCategories();
  } else {
    showToast(result.data, 'error');
  }
};

const handleUpdateNode = async () => {
  if (!ensureWritableKnowledgeBase()) return;
  if (!state.selectedNodeDetails?.id) {
    showToast('无法获取节点ID', 'error');
    return;
  }

  if (!nodeForm.labels.length) {
    showToast('请添加至少一个标签', 'error');
    return;
  }

  const properties = getNodePropertiesObject();

  const result = await api.updateNodePropertiesById(
    state.selectedNodeDetails.id,
    properties,
    nodeForm.labels,
  );

  if (result.flag) {
    showToast('节点更新成功');
    modals.editNode = false;

    // 重新获取更新后的节点信息
    const updatedNode = await api.queryNodeByName(state.selectedNodeDetails.properties.name);
    if (updatedNode.flag && updatedNode.data && updatedNode.data.length > 0) {
      // queryNodeByName 返回数组，取第一个元素
      selectNode(updatedNode.data[0]);
    }

    // 刷新知识列表（如果在分类视图或搜索视图中）
    if (state.selectedCategory) {
      const listResult = await api.queryNodeByCategory(
        state.selectedCategory.main,
        state.selectedCategory.detail,
      );
      if (listResult.flag) {
        state.knowledgeList = listResult.data;
      }
    } else if (state.searchResult) {
      await handleSearch();
    }
  } else {
    showToast(result.data, 'error');
  }
};

const confirmDeleteNode = () => {
  if (!ensureWritableKnowledgeBase()) return;
  if (!state.selectedNodeDetails) {
    showToast('没有选中的节点', 'error');
    return;
  }

  const nodeName = state.selectedNodeDetails.properties?.name || state.selectedNodeDetails.id;
  showConfirm(
    '删除节点确认',
    `确定要删除节点 "${nodeName}" 吗？\n\n注意：删除后无法恢复，并且会同时删除所有相关的关系。`,
    handleDeleteNode,
  );
};

const handleDeleteNode = async () => {
  if (!ensureWritableKnowledgeBase()) return;
  if (!state.selectedNodeDetails) {
    showToast('没有选中的节点', 'error');
    return;
  }

  const result = await api.deleteNode({
    id: state.selectedNodeDetails.id,
    labels: state.selectedNodeDetails.labels,
    properties: state.selectedNodeDetails.properties,
  });

  if (result.flag) {
    showToast('节点删除成功');

    state.selectedNode = null;
    state.selectedNodeDetails = null;
    state.selectedNodeRelations = [];

    if (state.selectedCategory) {
      const result = await api.queryNodeByCategory(
        state.selectedCategory.main,
        state.selectedCategory.detail,
      );
      if (result.flag) {
        state.knowledgeList = result.data;
      }
    } else if (state.searchResult) {
      await handleSearch();
    }

    await loadCategories();
  } else {
    showToast(result.data || '删除节点失败', 'error');
  }
};

// 关系操作
const handleCreateRelation = async () => {
  if (!ensureWritableKnowledgeBase()) return;
  // 将数组格式的属性转换为对象格式
  const relationWithObjProps = {
    ...relationForm.relation,
    properties: getRelationPropertiesObject(),
  };

  // 使用新的API格式：优先使用ID，否则使用name
  const relationData = api.buildRelationData(
    relationForm.start,
    relationForm.end,
    relationWithObjProps,
  );

  const result = await api.createRelation(relationData);

  if (result.flag) {
    showToast('关系创建成功');
    modals.addRelation = false;
    if (state.selectedNodeDetails) {
      const relationResult = await api.getNodeRelation(state.selectedNodeDetails);
      if (relationResult.flag && relationResult.data && relationResult.data.length > 0) {
        state.selectedNodeRelations = relationResult.data[0] || [];
      } else {
        state.selectedNodeRelations = [];
      }
    }
  } else {
    showToast(result.data, 'error');
  }
};

const handleUpdateRelation = async () => {
  if (!ensureWritableKnowledgeBase()) return;
  // 将数组格式的属性转换为对象格式
  const relationWithObjProps = {
    ...relationForm.relation,
    properties: getRelationPropertiesObject(),
  };

  // 使用新的API格式：优先使用ID，否则使用name
  const relationData = api.buildRelationData(
    relationForm.start,
    relationForm.end,
    relationWithObjProps,
  );

  const result = await api.updateRelationProperties(relationData);

  if (result.flag) {
    showToast('关系更新成功');
    modals.editRelation = false;
    if (state.selectedNodeDetails) {
      const relationResult = await api.getNodeRelation(state.selectedNodeDetails);
      if (relationResult.flag && relationResult.data && relationResult.data.length > 0) {
        state.selectedNodeRelations = relationResult.data[0] || [];
      } else {
        state.selectedNodeRelations = [];
      }
    }
  } else {
    showToast(result.data, 'error');
  }
};

const confirmDeleteRelation = () => {
  if (!relationForm.relation || !relationForm.start || !relationForm.end) {
    showToast('关系信息不完整', 'error');
    return;
  }

  const startName = relationForm.start.properties?.name || relationForm.start.id;
  const endName = relationForm.end.properties?.name || relationForm.end.id;
  const relationType = relationForm.relation.type;

  showConfirm(
    '删除关系确认',
    `确定要删除关系吗？\n\n关系：${startName} → [${relationType}] → ${endName}\n\n注意：删除后无法恢复。`,
    handleDeleteRelation,
  );
};

const handleDeleteRelation = async () => {
  if (!ensureWritableKnowledgeBase()) return;
  if (!relationForm.relation || !relationForm.start || !relationForm.end) {
    showToast('关系信息不完整', 'error');
    return;
  }

  // 将数组格式的属性转换为对象格式
  const relationWithObjProps = {
    ...relationForm.relation,
    properties: getRelationPropertiesObject(),
  };

  // 使用新的API格式：优先使用ID，否则使用name
  const relationData = api.buildRelationData(
    relationForm.start,
    relationForm.end,
    relationWithObjProps,
  );

  const result = await api.deleteRelation(relationData);

  if (result.flag) {
    showToast('关系删除成功');
    modals.editRelation = false;

    if (state.selectedNodeDetails) {
      const relationResult = await api.getNodeRelation(state.selectedNodeDetails);
      if (relationResult.flag && relationResult.data && relationResult.data.length > 0) {
        state.selectedNodeRelations = relationResult.data[0] || [];
      } else {
        state.selectedNodeRelations = [];
      }
    }
  } else {
    showToast(result.data || '删除关系失败', 'error');
  }
};

// 导入操作
const handleImport = async () => {
  if (!ensureWritableKnowledgeBase()) return;
  if (!importForm.file) {
    showToast('请选择文件', 'error');
    return;
  }

  const result = await api.loadFromCSV(importForm.file, importForm.type);

  if (result.flag) {
    showToast('导入成功');
    modals.importKnowledge = false;
    await loadCategories();
  } else {
    showToast(result.data, 'error');
  }
};

// 融合场景操作
const handleCreateFusionScenario = async (selectedCategory) => {
  if (!selectedCategory) {
    showToast('请选择一个事件分类', 'error');
    return;
  }

  try {
    loading.value = true;
    modals.createFusionScenario = false;

    // 显示融合加载提示
    modals.fusionLoading = true;

    // 根据选中的分类，查询该分类下的所有事件知识
    const result = await api.queryNodeByCategory(selectedCategory);

    if (!result.flag || !result.data || result.data.length === 0) {
      modals.fusionLoading = false;
      showToast(`分类"${selectedCategory}"下没有找到事件知识`, 'warning');
      loading.value = false;
      return;
    }

    const categoryEvents = result.data;

    // 提取所有事件的labels（去重）
    const labelsSet = new Set();
    categoryEvents.forEach((event) => {
      if (event.labels && Array.isArray(event.labels)) {
        event.labels.forEach((label) => labelsSet.add(label));
      }
    });

    // 提取所有事件的处置策略（去重）
    const strategiesSet = new Set();
    categoryEvents.forEach((event) => {
      if (event.properties && event.properties['处理策略']) {
        strategiesSet.add(event.properties['处理策略']);
      }
    });

    // 构建新的json格式
    const eventsParam = JSON.stringify({
      labels: Array.from(labelsSet),
      处置策略: Array.from(strategiesSet),
    });

    // 将treeKnowledgeList（labels为tree的数据）的name组成字符串数组
    const categoriesArray = state.treeKnowledgeList.map((tree) => tree.properties.name);
    const categoriesParam = JSON.stringify(categoriesArray);

    // 调用新的融合API
    const fusionResult = await api.fusion(eventsParam, categoriesParam);

    // 关闭融合加载提示
    modals.fusionLoading = false;

    if (fusionResult.code === 200) {
      // 保存融合结果数据
      fusionGenerationData.relations = fusionResult.relations || [];

      // 显示成功提示
      showToast(`融合成功，生成了 ${fusionResult.relationCount || 0} 个关系`, 'success');

      // 显示结果模态框
      modals.fusionResult = true;
    } else {
      showToast(fusionResult.message || '融合失败', 'error');
    }
  } catch (error) {
    console.error('融合失败:', error);
    // 关闭融合加载提示
    modals.fusionLoading = false;
    showToast('融合失败: ' + error.message, 'error');
  } finally {
    loading.value = false;
  }
};

// 导入融合生成的关系到知识库
const handleFusionImport = async (selectedRelations) => {
  if (!selectedRelations || selectedRelations.length === 0) {
    showToast('请至少选择一个关系', 'error');
    return;
  }

  try {
    // 设置导入状态
    if (fusionGenerationResultRef.value) {
      fusionGenerationResultRef.value.setImporting(true);
    }

    const insertResults = {
      successRelations: 0,
      failedRelations: 0,
      errors: [],
    };

    // 创建节点的辅助函数（如果节点不存在）
    const ensureNodeExists = async (nodeName) => {
      try {
        const queryResult = await api.queryNodeByName(nodeName, true);
        if (queryResult.flag && queryResult.data && queryResult.data.length > 0) {
          return queryResult.data[0];
        }
        // 节点不存在，创建节点
        const createResult = await api.createNode({
          labels: ['事件'], // 默认标签
          properties: { name: nodeName },
        });
        if (createResult.flag) {
          const newQueryResult = await api.queryNodeByName(nodeName, true);
          if (newQueryResult.flag && newQueryResult.data && newQueryResult.data.length > 0) {
            return newQueryResult.data[0];
          }
        }
        return null;
      } catch (error) {
        console.error('确保节点存在失败:', error);
        return null;
      }
    };

    // 处理每个选中的关系
    for (const relation of selectedRelations) {
      try {
        // 确保起始节点和结束节点存在
        const startNode = await ensureNodeExists(relation.start);
        const endNode = await ensureNodeExists(relation.end);

        if (!startNode || !endNode) {
          insertResults.failedRelations++;
          insertResults.errors.push(
            `关系创建失败: 节点不存在 (${relation.start} -> ${relation.end})`,
          );
          continue;
        }

        // 创建关系
        const relationData = api.buildRelationData(
          { id: startNode.id },
          { id: endNode.id },
          {
            type: relation.type,
            properties: relation.properties || {},
          },
        );

        const result = await api.createRelation(relationData);

        if (result.flag) {
          insertResults.successRelations++;
        } else {
          insertResults.failedRelations++;
          insertResults.errors.push(
            `关系创建失败: ${relation.start} -> ${relation.end} - ${result.data}`,
          );
        }
      } catch (error) {
        insertResults.failedRelations++;
        insertResults.errors.push(
          `关系处理异常: ${relation.start} -> ${relation.end} - ${error.message}`,
        );
      }
    }

    // 关闭结果模态框
    modals.fusionResult = false;

    // 重置融合结果数据
    fusionGenerationData.relations = [];

    // 显示导入结果
    if (insertResults.failedRelations > 0) {
      showToast(
        `导入完成！成功 ${insertResults.successRelations} 个，失败 ${insertResults.failedRelations} 个`,
        'warning',
      );
      if (insertResults.errors.length > 0) {
        console.error('导入错误详情:', insertResults.errors);
      }
    } else {
      showToast(`成功导入 ${insertResults.successRelations} 个关系到知识库！`, 'success');
    }

    // 重新加载分类数据
    await loadCategories();
  } catch (error) {
    console.error('导入知识库失败:', error);
    showToast('导入知识库失败: ' + error.message, 'error');
  } finally {
    // 重置导入状态
    if (fusionGenerationResultRef.value) {
      fusionGenerationResultRef.value.setImporting(false);
    }
  }
};

// 取消融合结果
const handleFusionResultCancel = () => {
  modals.fusionResult = false;
  fusionGenerationData.relations = [];
};

// 加载基础数据
const loadCategories = async () => {
  try {
    const [entityCat, eventCat, modelCat, treeCat, entityCount, eventCount, modelCount, treeCount] =
      await Promise.all([
        api.getEntityCategories(),
        api.getEventCategories(),
        api.getModelCategories(),
        api.getTreeCategories(),
        api.getEntityCountByCategory(),
        api.getEventCountByCategory(),
        api.getModelCountByCategory(),
        api.getTreeCountByCategory(),
      ]);

    if (entityCat.flag) state.categories.entity = entityCat.data;
    if (eventCat.flag) state.categories.event = eventCat.data;
    if (modelCat.flag) state.categories.model = modelCat.data;
    if (treeCat.flag) state.categories.tree = treeCat.data;

    if (entityCount.flag) state.categoryCounts.entity = entityCount.data;
    if (eventCount.flag) state.categoryCounts.event = eventCount.data;
    if (modelCount.flag) state.categoryCounts.model = modelCount.data;
    if (treeCount.flag) state.categoryCounts.tree = treeCount.data;
  } catch (error) {
    console.error('加载分类数据失败:', error);
  }
};

// 知识生成操作
const openKnowledgeGeneration = () => {
  // 如果有未消解的数据，提示用户
  if (generationState.canResolveConflict) {
    if (!confirmAndReset()) {
      return;
    }
    resetGenerationState();
  }
  openGenerationModal();
};

const handleKnowledgeGeneration = async (data) => {
  try {
    const result = await startGeneration(data);
    showToast(`知识生成成功！节点: ${result.nodeCount} 个`, 'success');
  } catch (error) {
    showToast(error.message || '知识生成失败', 'error');
  }
};

// 冲突消解操作
const openConflictResolution = async () => {
  try {
    const result = await startConflictResolution();
    showToast(
      `冲突消解完成！总计: ${result.totalNodes} 个节点，新建: ${result.newNodes} 个，合并: ${result.mergeNodes} 个`,
      'success',
    );
  } catch (error) {
    showToast(error.message || '冲突消解失败', 'error');
  }
};

// 插入知识库操作
const handleInsertToKnowledgeBase = async (selectedData) => {
  try {
    // 显示确认对话框
    const nodeCount = selectedData.nodes.length;
    const newCount = selectedData.nodes.filter((n) => n.judge === '否').length;
    const mergeCount = selectedData.nodes.filter((n) => n.judge === '是').length;

    const confirmMessage = `即将导入以下节点：

    节点总数: ${nodeCount} 个
    - 新建: ${newCount} 个
    - 合并: ${mergeCount} 个

    确认导入吗？`;

    if (!confirm(confirmMessage)) {
      return;
    }

    const result = await insertToKnowledgeBase(selectedData);

    const message = `节点插入完成！成功 ${result.successNodes} 个（新建 ${result.newNodes}，合并 ${result.mergedNodes}）${result.failedNodes > 0 ? `，失败 ${result.failedNodes} 个` : ''}`;

    if (result.errors.length > 0) {
      console.error('插入错误:', result.errors);
      showToast(message + '\n存在部分错误，请查看控制台', 'warning');
    } else {
      showToast(message, 'success');
    }

    // 刷新分类数据
    await loadCategories();
  } catch (error) {
    showToast(error.message || '插入知识库失败', 'error');
  }
};

// 组件挂载
onMounted(async () => {
  await loadKnowledgeBaseList();
  await reloadKnowledgeBaseData();
});
</script>

<style scoped></style>
