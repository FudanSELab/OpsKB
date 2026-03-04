<template>
  <div class="h-[calc(100vh-64px)] flex">
    <!-- 左侧：树图可视化区域 -->
    <div class="h-[calc(100vh-64px)] w-full flex flex-col p-4">
      <!-- 顶部工具栏 -->
      <div class="flex items-center justify-between mb-4">
        <div class="breadcrumbs text-sm">
          <ul>
            <li>策略生成</li>
            <li>策略树编辑器</li>
            <li v-if="currentScenarioName">
              {{ currentScenarioName }}
            </li>
            <li v-if="currentTreeVersion">
              {{ currentTreeVersion }}
            </li>
          </ul>
        </div>
        <div class="flex gap-2">
          <button class="btn btn-sm btn-outline" @click="handleLoadTree">
            <svg
              xmlns="http://www.w3.org/2000/svg"
              class="h-4 w-4"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-8l-4-4m0 0L8 8m4-4v12"
              />
            </svg>
            加载树
          </button>
          <button class="btn btn-sm btn-primary" @click="handleSaveTree" :disabled="!hasChanges">
            <svg
              xmlns="http://www.w3.org/2000/svg"
              class="h-4 w-4"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M8 7H5a2 2 0 00-2 2v9a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-3m-1 4l-3 3m0 0l-3-3m3 3V4"
              />
            </svg>
            保存更改
          </button>
          <button class="btn btn-sm btn-success" @click="handleGenerateSQL">
            <svg
              xmlns="http://www.w3.org/2000/svg"
              class="h-4 w-4"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M4 7v10c0 2.21 3.582 4 8 4s8-1.79 8-4V7M4 7c0 2.21 3.582 4 8 4s8-1.79 8-4M4 7c0-2.21 3.582-4 8-4s8 1.79 8 4"
              />
            </svg>
            生成SQL
          </button>
        </div>
      </div>

      <!-- 树图区域 -->
      <div class="flex-1 card bg-base-100 border-2">
        <div class="card-body p-4">
          <div v-if="loading" class="flex items-center justify-center h-full">
            <span class="loading loading-spinner loading-lg"></span>
          </div>
          <div v-else-if="error" class="flex flex-col items-center justify-center h-full">
            <svg
              xmlns="http://www.w3.org/2000/svg"
              class="h-16 w-16 text-error mb-4"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
              />
            </svg>
            <p class="text-error">{{ error }}</p>
            <button class="btn btn-sm btn-outline mt-4" @click="handleLoadTree">重试</button>
          </div>
          <div v-else ref="chartContainer" class="w-full h-full"></div>
        </div>
      </div>

      <!-- 操作提示 -->
      <div class="mt-4 alert">
        <svg
          xmlns="http://www.w3.org/2000/svg"
          fill="none"
          viewBox="0 0 24 24"
          class="stroke-current shrink-0 w-6 h-6"
        >
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
          ></path>
        </svg>
        <span>
          <strong>操作提示：</strong>
          左键单击编辑节点 | 双击删除节点 | 右键添加子节点 | 拖拽移动 | 滚轮缩放
        </span>
      </div>
    </div>

    <!-- 右侧：场景选择和SQL预览 -->
    <div class="w-96 flex flex-col py-4 h-[calc(100vh-64px)]">
      <!-- 场景选择 -->
      <div class="card bg-base-200 mb-4">
        <div class="card-body p-4">
          <h3 class="card-title text-base">场景选择</h3>
          <div class="form-control">
            <label class="label">
              <span class="label-text">场景ID</span>
            </label>
            <input
              type="number"
              v-model.number="currentScenarioId"
              class="input input-bordered input-sm"
              placeholder="输入场景ID"
            />
          </div>
          <div class="form-control">
            <label class="label">
              <span class="label-text">树版本</span>
            </label>
            <input
              type="text"
              v-model="currentTreeVersion"
              class="input input-bordered input-sm"
              placeholder="例如: v1.0"
            />
          </div>
        </div>
      </div>

      <!-- SQL预览 -->
      <div class="flex-1 card bg-base-200">
        <div class="card-body p-4 flex flex-col">
          <div class="flex items-center justify-between mb-2">
            <h3 class="card-title text-base">SQL语句预览</h3>
            <button
              class="btn btn-xs btn-ghost"
              @click="handleCopySQL"
              :disabled="sqlStatements.length === 0"
            >
              复制
            </button>
          </div>
          <div class="h-[530px] w-[280px]">
            <div
              class="text-xs font-mono bg-base-200 rounded whitespace-pre-wrap break-words overflow-auto h-full w-full"
              v-if="sqlStatements.length > 0"
            >
              {{ sqlStatements.join('\n\n') }}
            </div>
            <div v-else class="text-sm text-gray-400 text-center py-8">暂无SQL语句</div>
          </div>
          <div class="text-xs text-gray-500 mt-2">共 {{ sqlStatements.length }} 条语句</div>
        </div>
      </div>
    </div>

    <!-- 节点编辑对话框 -->
    <NodeEditDialog
      v-if="showEditDialog"
      :node-data="editingNode"
      :tree-data="treeData"
      @close="showEditDialog = false"
      @save="handleSaveNode"
      @delete="handleDeleteNode"
    />

    <!-- 加载树对话框 -->
    <dialog ref="loadTreeDialog" class="modal">
      <div class="modal-box">
        <h3 class="font-bold text-lg mb-4">加载策略树</h3>
        <div class="form-control mb-4">
          <label class="label">
            <span class="label-text">场景ID</span>
          </label>
          <input
            type="number"
            v-model.number="loadScenarioId"
            class="input input-bordered"
            placeholder="输入场景ID"
          />
        </div>
        <div class="form-control mb-4">
          <label class="label">
            <span class="label-text">树版本</span>
          </label>
          <input
            type="text"
            v-model="loadTreeVersion"
            class="input input-bordered"
            placeholder="例如: v1.0"
          />
        </div>
        <div class="modal-action">
          <button class="btn" @click="closeLoadDialog">取消</button>
          <button class="btn btn-primary" @click="confirmLoadTree">加载</button>
        </div>
      </div>
      <form method="dialog" class="modal-backdrop">
        <button @click="closeLoadDialog">关闭</button>
      </form>
    </dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import NodeEditDialog from '../components/treeEditor/NodeEditDialog.vue';
import { useTreeEditor } from '../composables/treeEditor/useTreeEditor.js';

// 使用树编辑器组合式函数
const editorData = useTreeEditor();

// 解构响应式属性供模板使用
const {
  treeData,
  currentScenarioId,
  currentScenarioName,
  currentTreeVersion,
  loading,
  error,
  hasChanges,
  sqlStatements,
  showEditDialog,
  editingNode,
} = editorData;

// 组件引用
const chartContainer = ref(null);
const loadTreeDialog = ref(null);

// 加载树对话框数据
const loadScenarioId = ref(null);
const loadTreeVersion = ref('');

// ========== 事件处理函数 ==========

const handleLoadTree = () => {
  loadScenarioId.value = currentScenarioId.value;
  loadTreeVersion.value = currentTreeVersion.value;
  loadTreeDialog.value?.showModal();
};

const closeLoadDialog = () => {
  loadTreeDialog.value?.close();
};

const confirmLoadTree = async () => {
  if (!loadScenarioId.value || !loadTreeVersion.value) {
    alert('请输入场景ID和树版本');
    return;
  }

  await editorData.loadTreeData(loadScenarioId.value, loadTreeVersion.value);
  closeLoadDialog();

  // 初始化图表
  if (chartContainer.value && treeData.value.length > 0) {
    editorData.initChart(chartContainer.value);
  }
};

const handleSaveNode = (nodeData) => {
  editorData.saveNode(nodeData);
  showEditDialog.value = false;
};

const handleDeleteNode = (treeId) => {
  if (confirm('确定要删除此节点及其所有子节点吗？')) {
    editorData.deleteNode(treeId);
    showEditDialog.value = false;
  }
};

const handleSaveTree = () => {
  // 这里可以调用API保存到数据库
  alert('保存功能待实现，请使用生成SQL功能');
};

const handleGenerateSQL = () => {
  editorData.generateSQL();
};

const handleCopySQL = () => {
  const sql = sqlStatements.value.join('\n\n');
  navigator.clipboard.writeText(sql).then(() => {
    alert('SQL语句已复制到剪贴板');
  });
};

// ========== 生命周期 ==========

onMounted(() => {
  // 可以在这里自动加载默认树
});

onUnmounted(() => {
  editorData.disposeChart();
});
</script>

<style scoped>
/* 自定义样式 */
</style>
