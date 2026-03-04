<template>
  <div v-if="show" class="modal modal-open">
    <div class="modal-box max-w-6xl max-h-[90vh]">
      <!-- 标题和统计信息 -->
      <div class="flex items-center justify-between mb-4 gap-4">
        <h3 class="font-bold text-lg">融合生成结果</h3>

        <!-- 统计信息 -->
        <div class="flex gap-2">
          <div class="rounded-lg p-2 bg-primary/20 flex items-center justify-between w-40">
            <div class="text-xs text-gray-600 font-semibold">总关系</div>
            <div class="text-2xl font-bold text-primary">{{ relationCount }}</div>
          </div>
          <div class="rounded-lg p-2 bg-warning/20 flex items-center justify-between w-40">
            <div class="text-xs text-gray-600 font-semibold">已存在</div>
            <div class="text-2xl font-bold text-warning">{{ existingCount }}</div>
          </div>
        </div>
      </div>

      <!-- 检查状态提示 -->
      <div v-if="checking" class="mb-3 text-sm text-gray-600 flex items-center gap-2">
        <span class="loading loading-spinner loading-sm"></span>
        正在检查关系是否存在...
      </div>

      <!-- 关系表格 -->
      <div class="overflow-auto max-h-[400px] border rounded-lg">
        <table class="table table-zebra table-sm w-full">
          <thead class="sticky top-0 bg-base-200 z-10">
            <tr>
              <th class="w-8">
                <input
                  type="checkbox"
                  class="checkbox checkbox-sm"
                  :checked="allSelected"
                  :disabled="checking"
                  @change="toggleSelectAll"
                />
              </th>
              <th class="w-12">#</th>
              <th class="w-20">状态</th>
              <th class="w-24">关系类型</th>
              <th class="w-48">起始节点</th>
              <th class="w-48">结束节点</th>
              <th>属性</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="(relation, index) in relations"
              :key="index"
              :class="{ 'opacity-50': relationStatus[index]?.exists }"
            >
              <td>
                <input
                  type="checkbox"
                  class="checkbox checkbox-sm"
                  :checked="selectedRelations.includes(index)"
                  :disabled="relationStatus[index]?.exists || checking"
                  @change="toggleRelation(index)"
                />
              </td>
              <td>{{ index + 1 }}</td>
              <td>
                <span v-if="relationStatus[index]?.checking" class="text-xs text-gray-500">
                  检查中...
                </span>
                <span
                  v-else-if="relationStatus[index]?.exists"
                  class="badge badge-warning badge-sm"
                >
                  已存在
                </span>
                <span v-else class="badge badge-success badge-sm">新关系</span>
              </td>
              <td>
                <span class="badge badge-primary badge-sm">{{ relation.type }}</span>
              </td>
              <td class="font-semibold">{{ relation.start }}</td>
              <td class="font-semibold">{{ relation.end }}</td>
              <td>
                <div class="text-sm space-y-1">
                  <div v-for="(value, key) in relation.properties" :key="key">
                    <span class="font-semibold text-gray-600">{{ key }}:</span>
                    <span class="ml-1">{{ value }}</span>
                  </div>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
        <div v-if="relations.length === 0" class="text-center py-8 text-gray-500">没有生成关系</div>
      </div>

      <!-- 已选择提示 -->
      <div v-if="selectedRelations.length > 0" class="mt-3 text-sm text-gray-600">
        已选择 <span class="font-bold text-primary">{{ selectedRelations.length }}</span> 个关系
      </div>

      <div class="modal-action">
        <button
          class="btn btn-primary"
          @click="handleImport"
          :disabled="selectedRelations.length === 0 || importing || checking"
        >
          {{ importing ? '导入中...' : '导入知识库' }}
        </button>
        <button class="btn" @click="$emit('close')" :disabled="importing || checking">关闭</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue';
import { useKnowledgeApi } from '../../../composables/knowledge/useKnowledgeApi.js';

const props = defineProps({
  show: {
    type: Boolean,
    default: false,
  },
  relations: {
    type: Array,
    default: () => [],
  },
});

const emit = defineEmits(['close', 'import']);

const { api } = useKnowledgeApi();

const selectedRelations = ref([]);
const importing = ref(false);
const checking = ref(false);
const relationStatus = ref({}); // 存储每个关系的检查状态 { index: { checking: false, exists: false } }

const relationCount = computed(() => props.relations.length);

const existingCount = computed(() => {
  return Object.values(relationStatus.value).filter((status) => status.exists).length;
});

const allSelected = computed(() => {
  // 只计算可选择的关系（不包括已存在的）
  const selectableIndices = props.relations
    .map((_, index) => index)
    .filter((index) => !relationStatus.value[index]?.exists);

  return (
    selectableIndices.length > 0 &&
    selectableIndices.every((index) => selectedRelations.value.includes(index))
  );
});

// 批量检查所有关系是否存在
const checkAllRelations = async () => {
  if (props.relations.length === 0) {
    return;
  }

  checking.value = true;
  relationStatus.value = {};

  // 初始化所有关系状态为检查中
  props.relations.forEach((_, index) => {
    relationStatus.value[index] = { checking: true, exists: false };
  });

  try {
    // 1. 按起始节点名称分组
    const relationsByStart = new Map(); // key: startNodeName, value: array of {relation, index}
    props.relations.forEach((relation, index) => {
      if (!relationsByStart.has(relation.start)) {
        relationsByStart.set(relation.start, []);
      }
      relationsByStart.get(relation.start).push({ relation, index });
    });

    // 2. 对每个唯一的起始节点，只查询一次
    const checkPromises = Array.from(relationsByStart.entries()).map(
      async ([startNodeName, relationsGroup]) => {
        try {
          // 查询起始节点
          const startNodeResult = await api.queryNodeByName(startNodeName, true);

          if (!startNodeResult.flag || !startNodeResult.data || startNodeResult.data.length === 0) {
            // 起始节点不存在，该组所有关系都标记为不存在
            relationsGroup.forEach(({ index }) => {
              relationStatus.value[index] = { checking: false, exists: false };
            });
            return;
          }

          const startNode = startNodeResult.data[0];

          // 获取该起始节点的所有关系（传递完整的节点结构）
          const relationResult = await api.getNodeRelation(startNode);

          if (!relationResult.flag || !relationResult.data) {
            // 查询失败，该组所有关系都标记为不存在
            relationsGroup.forEach(({ index }) => {
              relationStatus.value[index] = { checking: false, exists: false };
            });
            return;
          }

          // 3. 对该组的每个待检查关系，在返回的关系数组中查找是否存在
          let existingRelations = relationResult.data;

          // 如果返回的是数组的数组，需要扁平化
          if (Array.isArray(existingRelations[0])) {
            console.log('扁平化前');
            console.log(existingRelations);
            existingRelations = existingRelations.flat();
            console.log('扁平化后');
            console.log(existingRelations);
          }

          relationsGroup.forEach(({ relation, index }) => {
            const exists = existingRelations.some((existingRel) => {
              // 根据实际数据结构提取：
              // existingRel = {start: {...}, end: {...}, relation: {type, properties, ...}}
              const relationType = existingRel.relation?.type;
              const existingStartName = existingRel.start?.properties?.name;
              const existingEndName = existingRel.end?.properties?.name;

              // 检查关系类型、起始节点和结束节点是否都相同
              return (
                relationType === relation.type &&
                existingStartName === relation.start &&
                existingEndName === relation.end
              );
            });

            relationStatus.value[index] = { checking: false, exists };
          });
        } catch (error) {
          console.error(`检查起始节点 ${startNodeName} 的关系时出错:`, error);
          // 出错时，该组所有关系都标记为不存在
          relationsGroup.forEach(({ index }) => {
            relationStatus.value[index] = { checking: false, exists: false };
          });
        }
      },
    );

    // 4. 等待所有检查完成
    await Promise.all(checkPromises);
  } catch (error) {
    console.error('批量检查关系时出错:', error);
  } finally {
    checking.value = false;

    // 更新选择状态：移除已存在的关系
    selectedRelations.value = selectedRelations.value.filter(
      (index) => !relationStatus.value[index]?.exists,
    );
  }
};

// 切换全选
const toggleSelectAll = () => {
  if (allSelected.value) {
    selectedRelations.value = [];
  } else {
    // 只选择不存在的关系
    selectedRelations.value = props.relations
      .map((_, index) => index)
      .filter((index) => !relationStatus.value[index]?.exists);
  }
};

// 切换单个关系选择
const toggleRelation = (index) => {
  // 如果关系已存在，不允许选择
  if (relationStatus.value[index]?.exists) {
    return;
  }

  const idx = selectedRelations.value.indexOf(index);
  if (idx > -1) {
    selectedRelations.value.splice(idx, 1);
  } else {
    selectedRelations.value.push(index);
  }
};

// 处理导入
const handleImport = () => {
  const selectedRelationData = selectedRelations.value.map((index) => props.relations[index]);
  emit('import', selectedRelationData);
};

// 监听show变化，重置选择并检查关系
watch(
  () => props.show,
  async (newVal) => {
    if (newVal) {
      // 默认全选
      selectedRelations.value = props.relations.map((_, index) => index);
      // 检查所有关系是否存在
      await checkAllRelations();
    } else {
      selectedRelations.value = [];
      importing.value = false;
      checking.value = false;
      relationStatus.value = {};
    }
  },
);

// 暴露importing状态给父组件控制
defineExpose({
  setImporting: (value) => {
    importing.value = value;
  },
});
</script>
