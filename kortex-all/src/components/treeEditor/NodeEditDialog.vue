<template>
  <dialog class="modal modal-open z-[9999]">
    <div class="modal-box max-w-2xl">
      <h3 class="font-bold text-lg mb-4">
        {{ isNewNode ? '新建节点' : '编辑节点' }}
      </h3>

      <!-- 基本信息 -->
      <div class="grid grid-cols-2 gap-4 mb-4">
        <div class="form-control">
          <label class="label">
            <span class="label-text">节点ID</span>
          </label>
          <input
            type="number"
            v-model.number="localNodeData.treeId"
            class="input input-bordered input-sm"
            :disabled="!isNewNode"
          />
        </div>

        <div class="form-control">
          <label class="label">
            <span class="label-text">节点类型 *</span>
          </label>
          <select v-model="localNodeData.nodeType" class="select select-bordered select-sm">
            <option value="tree">tree (根节点)</option>
            <option value="branch">branch (分支节点)</option>
            <option value="node">node (叶子节点)</option>
          </select>
        </div>

        <div class="form-control">
          <label class="label">
            <span class="label-text">父节点ID</span>
          </label>
          <select
            v-model.number="localNodeData.parentId"
            class="select select-bordered select-sm"
            :disabled="localNodeData.nodeType === 'tree'"
          >
            <option :value="null">无 (根节点)</option>
            <option v-for="node in availableParentNodes" :key="node.treeId" :value="node.treeId">
              {{ node.treeId }} - {{ getNodeTypeName(node.nodeType) }}
            </option>
          </select>
        </div>

        <div class="form-control" v-if="localNodeData.nodeType === 'node'">
          <label class="label">
            <span class="label-text">策略ID</span>
          </label>
          <input
            type="number"
            v-model.number="localNodeData.strategyId"
            class="input input-bordered input-sm"
            placeholder="策略ID"
          />
        </div>
      </div>

      <!-- 条件编辑 -->
      <div class="mb-4" v-if="localNodeData.nodeType !== 'tree'">
        <label class="label">
          <span class="label-text">节点条件</span>
          <button class="btn btn-xs btn-ghost" @click="showConditionHelp = !showConditionHelp">
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
                d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
              />
            </svg>
          </button>
        </label>

        <div v-if="showConditionHelp" class="alert alert-info mb-2 text-xs">
          <span
            >可用条件键：sizeMax, speedAvg, speedMax, heightAvg, heightMax, targetNum, threatAvg,
            threatMax, visibility。值可以是数字、字符串或数组 [min, max]</span
          >
        </div>

        <div class="space-y-2">
          <div
            v-for="(condition, index) in conditionsList"
            :key="index"
            class="flex gap-2 items-center"
          >
            <input
              type="text"
              v-model="condition.key"
              placeholder="条件键"
              class="input input-bordered input-sm flex-1"
              list="conditionKeys"
            />
            <datalist id="conditionKeys">
              <option value="sizeMax">目标最大尺寸</option>
              <option value="speedAvg">目标平均速度</option>
              <option value="speedMax">目标最快速度</option>
              <option value="heightAvg">目标平均高度</option>
              <option value="heightMax">目标最大高度</option>
              <option value="targetNum">目标数量</option>
              <option value="threatAvg">目标平均威胁等级</option>
              <option value="threatMax">目标最大威胁等级</option>
              <option value="visibility">环境能见度</option>
            </datalist>
            <input
              type="text"
              v-model="condition.value"
              placeholder="值 (数字、字符串或 [min,max])"
              class="input input-bordered input-sm flex-1"
            />
            <button class="btn btn-sm btn-ghost btn-circle" @click="removeCondition(index)">
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
                  d="M6 18L18 6M6 6l12 12"
                />
              </svg>
            </button>
          </div>
          <button class="btn btn-sm btn-outline w-full" @click="addCondition">+ 添加条件</button>
        </div>
      </div>

      <!-- 子节点ID列表 -->
      <div class="mb-4">
        <label class="label">
          <span class="label-text">子节点ID列表</span>
        </label>
        <div class="flex flex-wrap gap-2">
          <div v-for="childId in localNodeData.childIds" :key="childId" class="badge badge-primary">
            {{ childId }}
          </div>
          <div v-if="localNodeData.childIds.length === 0" class="text-sm text-gray-400">
            无子节点
          </div>
        </div>
        <div class="text-xs text-gray-500 mt-1">子节点ID由系统自动管理，添加子节点时会自动更新</div>
      </div>

      <!-- JSON预览 -->
      <div class="mb-4">
        <label class="label">
          <span class="label-text">数据预览</span>
        </label>
        <pre class="bg-base-200 p-3 rounded text-xs overflow-auto max-h-40">{{
          JSON.stringify(getPreviewData(), null, 2)
        }}</pre>
      </div>

      <!-- 操作按钮 -->
      <div class="modal-action">
        <button class="btn btn-sm" @click="$emit('close')">取消</button>
        <button
          v-if="!isNewNode"
          class="btn btn-sm btn-error"
          @click="$emit('delete', localNodeData.treeId)"
        >
          删除
        </button>
        <button class="btn btn-sm btn-primary" @click="handleSave">保存</button>
      </div>
    </div>
    <form method="dialog" class="modal-backdrop">
      <button @click="$emit('close')">关闭</button>
    </form>
  </dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue';

const props = defineProps({
  nodeData: {
    type: Object,
    required: true,
  },
  treeData: {
    type: Array,
    required: true,
  },
});

const emit = defineEmits(['close', 'save', 'delete']);

// 本地节点数据
const localNodeData = ref({
  treeId: null,
  nodeType: 'branch',
  parentId: null,
  childIds: [],
  conditions: null,
  strategyId: null,
  ...JSON.parse(JSON.stringify(props.nodeData)),
});

// 条件列表
const conditionsList = ref([]);
const showConditionHelp = ref(false);

// 是否是新节点
const isNewNode = computed(() => {
  return !props.treeData.find((n) => n.treeId === localNodeData.value.treeId);
});

// 可用的父节点列表
const availableParentNodes = computed(() => {
  return props.treeData.filter(
    (node) =>
      node.treeId !== localNodeData.value.treeId &&
      (node.nodeType === 'tree' || node.nodeType === 'branch'),
  );
});

// 初始化条件列表函数
const initConditionsList = () => {
  conditionsList.value = [];
  const conditions = localNodeData.value.conditions;
  if (conditions && typeof conditions === 'object') {
    Object.entries(conditions).forEach(([key, value]) => {
      if (value !== null && value !== undefined) {
        conditionsList.value.push({
          key,
          value: JSON.stringify(value),
        });
      }
    });
  }
};

// 监听节点数据变化，重新初始化条件列表
watch(
  () => props.nodeData,
  () => {
    initConditionsList();
  },
  { immediate: true },
);

const addCondition = () => {
  conditionsList.value.push({ key: '', value: '' });
};

const removeCondition = (index) => {
  conditionsList.value.splice(index, 1);
};

const getNodeTypeName = (type) => {
  const map = {
    tree: '根节点',
    branch: '分支节点',
    node: '叶子节点',
  };
  return map[type] || type;
};

const parseConditionValue = (valueStr) => {
  if (!valueStr || valueStr.trim() === '') return null;

  try {
    // 尝试解析为JSON
    return JSON.parse(valueStr);
  } catch {
    // 如果不是有效的JSON，尝试解析为数字
    const num = Number(valueStr);
    if (!isNaN(num)) return num;

    // 否则返回字符串
    return valueStr;
  }
};

const getPreviewData = () => {
  // 构建条件对象
  const conditions = {};
  conditionsList.value.forEach((condition) => {
    if (condition.key && condition.key.trim()) {
      conditions[condition.key] = parseConditionValue(condition.value);
    }
  });

  return {
    treeId: localNodeData.value.treeId,
    nodeType: localNodeData.value.nodeType,
    parentId: localNodeData.value.parentId,
    childIds: localNodeData.value.childIds,
    conditions: Object.keys(conditions).length > 0 ? conditions : null,
    strategyId: localNodeData.value.strategyId,
  };
};

const handleSave = () => {
  // 验证必填字段
  if (!localNodeData.value.nodeType) {
    alert('请选择节点类型');
    return;
  }

  if (localNodeData.value.nodeType !== 'tree' && localNodeData.value.parentId === null) {
    alert('非根节点必须指定父节点');
    return;
  }

  // 构建条件对象
  const conditions = {};
  conditionsList.value.forEach((condition) => {
    if (condition.key && condition.key.trim()) {
      conditions[condition.key] = parseConditionValue(condition.value);
    }
  });

  // 构建最终数据
  const finalData = {
    ...localNodeData.value,
    conditions: Object.keys(conditions).length > 0 ? conditions : null,
  };

  emit('save', finalData);
};
</script>

<style scoped>
/* 自定义样式 */
</style>
