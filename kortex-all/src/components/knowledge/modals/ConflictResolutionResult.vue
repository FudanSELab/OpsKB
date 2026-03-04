<template>
  <div v-if="show" class="modal modal-open">
    <div class="modal-box max-w-7xl max-h-[90vh]">
      <!-- 标题和统计信息 -->
      <div class="flex items-center justify-between mb-4 gap-4">
        <h3 class="font-bold text-lg">冲突消解结果 - 选择要导入的节点</h3>

        <!-- 统计信息 -->
        <div class="grid grid-cols-3 gap-2" style="width: 380px">
          <div class="rounded-lg p-2 bg-primary/10 flex flex-col items-center justify-center">
            <div class="text-xs text-gray-600 font-semibold">已选择</div>
            <div class="text-2xl font-bold text-primary">{{ selectedNodeCount }}</div>
          </div>
          <div class="rounded-lg p-2 bg-success/10 flex flex-col items-center justify-center">
            <div class="text-xs text-gray-600 font-semibold">新建</div>
            <div class="text-2xl font-bold text-success">{{ selectedNewNodeCount }}</div>
          </div>
          <div class="rounded-lg p-2 bg-warning/10 flex flex-col items-center justify-center">
            <div class="text-xs text-gray-600 font-semibold">合并</div>
            <div class="text-2xl font-bold text-warning">{{ selectedMergeNodeCount }}</div>
          </div>
        </div>
      </div>

      <!-- 快速操作按钮 -->
      <div class="flex gap-2 mb-4">
        <button class="btn btn-sm btn-outline" @click="selectAll">全选</button>
        <button class="btn btn-sm btn-outline btn-error" @click="selectNone">全不选</button>
        <button class="btn btn-sm btn-outline btn-success" @click="selectOnlyNew">仅选新建</button>
        <button class="btn btn-sm btn-outline btn-warning" @click="selectOnlyMerge">
          仅选合并
        </button>
      </div>

      <!-- 节点表格 -->
      <div class="overflow-auto max-h-[500px] border rounded-lg">
        <table class="table table-zebra table-sm w-full">
          <thead class="sticky top-0 bg-base-200 z-10">
            <tr>
              <th class="w-12">
                <input
                  type="checkbox"
                  class="checkbox checkbox-sm"
                  :checked="allNodesSelected"
                  @change="toggleAllNodes"
                />
              </th>
              <th class="w-12">#</th>
              <th class="w-40">操作类型</th>
              <th class="w-80">原始节点</th>
              <th class="w-80">相似节点</th>
              <th class="w-80">合并节点</th>
              <th class="w-80">消解理由</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="(result, index) in resolutionResults"
              :key="index"
              :class="{
                'bg-green-50': result.judge === '否',
                'bg-yellow-50': result.judge === '是',
              }"
            >
              <td>
                <input
                  type="checkbox"
                  class="checkbox checkbox-sm checkbox-primary"
                  v-model="nodeSelections[index]"
                />
              </td>
              <td>{{ index + 1 }}</td>
              <td>
                <!-- 合并类型 - 显示badge和选择按钮 -->
                <div v-if="result.judge === '是'" class="flex flex-col gap-2">
                  <span class="badge badge-warning badge-sm gap-1" title="更新已有节点">
                    <svg
                      xmlns="http://www.w3.org/2000/svg"
                      class="h-3 w-3"
                      fill="none"
                      viewBox="0 0 24 24"
                      stroke="currentColor"
                    >
                      <path
                        stroke-linecap="round"
                        stroke-linejoin="round"
                        stroke-width="2"
                        d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"
                      />
                    </svg>
                    合并
                  </span>
                  <label class="flex items-center gap-1 cursor-pointer">
                    <input
                      type="radio"
                      class="radio radio-xs radio-primary"
                      :name="'merge-choice-' + index"
                      value="finalKg"
                      v-model="mergeChoices[index]"
                    />
                    <span class="text-xs">使用合并</span>
                  </label>
                  <label class="flex items-center gap-1 cursor-pointer">
                    <input
                      type="radio"
                      class="radio radio-xs radio-secondary"
                      :name="'merge-choice-' + index"
                      value="original"
                      v-model="mergeChoices[index]"
                    />
                    <span class="text-xs">使用原始</span>
                  </label>
                </div>
                <!-- 新建类型 -->
                <span
                  v-else-if="result.judge === '否'"
                  class="badge badge-success badge-sm gap-1"
                  title="插入新节点"
                >
                  <svg
                    xmlns="http://www.w3.org/2000/svg"
                    class="h-3 w-3"
                    fill="none"
                    viewBox="0 0 24 24"
                    stroke="currentColor"
                  >
                    <path
                      stroke-linecap="round"
                      stroke-linejoin="round"
                      stroke-width="2"
                      d="M12 4v16m8-8H4"
                    />
                  </svg>
                  新建
                </span>
                <!-- 其他类型 -->
                <span v-else class="badge badge-ghost badge-sm">
                  {{ result.judge }}
                </span>
              </td>
              <!-- 原始节点列 -->
              <td>
                <div v-if="result.target" class="text-sm">
                  <div class="font-semibold mb-1">
                    {{ getNodeName(result.target) }}
                  </div>
                  <div class="flex flex-wrap gap-1 mb-1">
                    <span
                      v-for="label in parseNodeLabels(result.target)"
                      :key="label"
                      class="badge badge-ghost badge-xs"
                    >
                      {{ label }}
                    </span>
                  </div>
                  <div class="space-y-0.5 text-gray-600">
                    <div v-for="(value, key) in parseNodeProperties(result.target)" :key="key">
                      <span class="font-semibold">{{ key }}:</span> {{ value }}
                    </div>
                  </div>
                </div>
                <div v-else class="text-center text-gray-400 py-4">无原始数据</div>
              </td>
              <td>
                <div v-if="result.similar_target" class="text-sm">
                  <div class="font-semibold mb-1">
                    {{ getNodeName(result.similar_target) }}
                  </div>
                  <div
                    v-if="getSimilarTargetId(result.similar_target)"
                    class="text-xs text-gray-500 mb-1"
                  >
                    ID: {{ getSimilarTargetId(result.similar_target) }}
                  </div>
                  <div class="flex flex-wrap gap-1 mb-1">
                    <span
                      v-for="label in parseNodeLabels(result.similar_target)"
                      :key="label"
                      class="badge badge-ghost badge-xs"
                    >
                      {{ label }}
                    </span>
                  </div>
                  <div class="space-y-0.5 text-gray-600">
                    <div
                      v-for="(value, key) in parseNodeProperties(result.similar_target)"
                      :key="key"
                    >
                      <span class="font-semibold">{{ key }}:</span> {{ value }}
                    </div>
                  </div>
                </div>
                <div v-else class="text-center text-gray-400 py-4">无已有节点</div>
              </td>
              <td>
                <div v-if="result.finalKg" class="text-sm">
                  <div
                    class="font-semibold mb-1"
                    :class="result.judge === '否' ? 'text-success' : 'text-warning'"
                  >
                    {{ result.finalKg.properties?.name || '-' }}
                  </div>
                  <div v-if="result.finalKg.id" class="text-xs text-gray-500 mb-1">
                    ID: {{ result.finalKg.id }}
                  </div>
                  <div class="flex flex-wrap gap-1 mb-1">
                    <span
                      v-for="label in result.finalKg.labels"
                      :key="label"
                      class="badge badge-xs"
                      :class="result.judge === '否' ? 'badge-success' : 'badge-warning'"
                    >
                      {{ label }}
                    </span>
                  </div>
                  <div class="space-y-0.5 text-gray-600">
                    <div v-for="(value, key) in result.finalKg.properties" :key="key">
                      <span class="font-semibold">{{ key }}:</span> {{ value }}
                    </div>
                  </div>
                </div>
                <div v-else class="text-center text-gray-400 py-4">无消解结果</div>
              </td>
              <td>
                <div class="text-sm text-gray-700">
                  {{ result.judge_reason || '-' }}
                </div>
              </td>
            </tr>
          </tbody>
        </table>
        <div v-if="resolutionResults.length === 0" class="text-center py-8 text-gray-500">
          没有消解结果
        </div>
      </div>

      <div class="modal-action">
        <button class="btn" @click="$emit('cancel')" :disabled="loading">取消</button>
        <button
          class="btn btn-primary"
          @click="handleInsert"
          :disabled="loading || selectedNodeCount === 0"
        >
          <span v-if="loading" class="loading loading-spinner"></span>
          插入知识库 ({{ selectedNodeCount }} 个节点)
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
  resolutionResults: {
    type: Array,
    default: () => [],
  },
  loading: {
    type: Boolean,
    default: false,
  },
});

const emit = defineEmits(['insert', 'cancel']);

// 节点选择状态
const nodeSelections = ref([]);

// 合并类型节点的选择：'finalKg' 或 'original'
const mergeChoices = ref([]);

// 初始化选择状态
watch(
  () => props.resolutionResults,
  (newResults) => {
    // 默认全选所有节点
    nodeSelections.value = newResults.map(() => true);
    // 默认对于合并类型选择使用最终节点(finalKg)
    mergeChoices.value = newResults.map(() => 'finalKg');
  },
  { immediate: true },
);

// 监听弹框显示状态，打开时重置选择状态
watch(
  () => props.show,
  (newVal) => {
    if (newVal && props.resolutionResults.length > 0) {
      // modal打开时，重置为全选状态
      nodeSelections.value = props.resolutionResults.map(() => true);
      mergeChoices.value = props.resolutionResults.map(() => 'finalKg');
    }
  },
);

// 统计信息
const nodeCount = computed(() => props.resolutionResults.length);

const selectedNodeCount = computed(() => {
  return nodeSelections.value.filter((s) => s).length;
});

const selectedNewNodeCount = computed(() => {
  return props.resolutionResults.filter(
    (result, index) => nodeSelections.value[index] && result.judge === '否',
  ).length;
});

const selectedMergeNodeCount = computed(() => {
  return props.resolutionResults.filter(
    (result, index) => nodeSelections.value[index] && result.judge === '是',
  ).length;
});

const allNodesSelected = computed(() => {
  return nodeSelections.value.length > 0 && nodeSelections.value.every((selected) => selected);
});

// 快速操作方法
const selectAll = () => {
  nodeSelections.value = nodeSelections.value.map(() => true);
};

const selectNone = () => {
  nodeSelections.value = nodeSelections.value.map(() => false);
};

const selectOnlyNew = () => {
  nodeSelections.value = props.resolutionResults.map((result) => result.judge === '否');
};

const selectOnlyMerge = () => {
  nodeSelections.value = props.resolutionResults.map((result) => result.judge === '是');
};

const toggleAllNodes = () => {
  const newValue = !allNodesSelected.value;
  nodeSelections.value = nodeSelections.value.map(() => newValue);
};

// 处理插入
const handleInsert = () => {
  const selectedData = {
    nodes: [],
  };

  // 收集选中的节点
  nodeSelections.value.forEach((selected, index) => {
    if (selected) {
      const result = props.resolutionResults[index];
      let nodeToInsert = null;

      if (result.judge === '否') {
        // judge='否' → 新建节点：使用 target 数据（原始提取的数据）
        try {
          nodeToInsert =
            typeof result.target === 'string' ? JSON.parse(result.target) : result.target;
        } catch (e) {
          console.error('解析原始节点失败:', e);
        }
      } else if (result.judge === '是') {
        // judge='是' → 合并节点：根据用户选择使用 finalKg 或 original
        if (mergeChoices.value[index] === 'original') {
          // 用户选择使用原始节点
          try {
            nodeToInsert =
              typeof result.target === 'string' ? JSON.parse(result.target) : result.target;
          } catch (e) {
            console.error('解析原始节点失败:', e);
          }
        } else {
          // 默认使用合并后的节点 (finalKg)
          nodeToInsert = result.finalKg;
        }
      }

      // 检查 nodeToInsert 是否有效
      if (!nodeToInsert) {
        console.error('节点数据无效，跳过:', result);
        return; // 跳过这个无效的节点
      }

      selectedData.nodes.push({
        judge: result.judge, // '是' 或 '否'
        finalKg: nodeToInsert,
      });
    }
  });

  emit('insert', selectedData);
};

// 从JSON字符串或对象中获取节点名称
const getNodeName = (nodeData) => {
  try {
    if (typeof nodeData === 'string') {
      const parsed = JSON.parse(nodeData);
      return parsed.properties?.name || parsed.name || '未知';
    } else if (typeof nodeData === 'object' && nodeData !== null) {
      return nodeData.properties?.name || nodeData.name || '未知';
    }
    return '未知';
  } catch (e) {
    return '未知';
  }
};

// 获取 similar_target 的 ID
const getSimilarTargetId = (nodeData) => {
  try {
    if (typeof nodeData === 'string') {
      const parsed = JSON.parse(nodeData);
      return parsed.id || null;
    } else if (typeof nodeData === 'object' && nodeData !== null) {
      return nodeData.id || null;
    }
    return null;
  } catch (e) {
    return null;
  }
};

// 解析节点属性
const parseNodeProperties = (nodeData) => {
  try {
    if (typeof nodeData === 'string') {
      const parsed = JSON.parse(nodeData);
      return parsed.properties || {};
    } else if (typeof nodeData === 'object' && nodeData !== null) {
      return nodeData.properties || {};
    }
    return {};
  } catch (e) {
    return {};
  }
};

// 解析节点标签
const parseNodeLabels = (nodeData) => {
  try {
    if (typeof nodeData === 'string') {
      const parsed = JSON.parse(nodeData);
      return parsed.labels || [];
    } else if (typeof nodeData === 'object' && nodeData !== null) {
      return nodeData.labels || [];
    }
    return [];
  } catch (e) {
    return [];
  }
};
</script>
