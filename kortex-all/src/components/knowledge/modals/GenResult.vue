<template>
  <div v-if="show" class="modal modal-open">
    <div class="modal-box max-w-6xl max-h-[90vh]">
      <!-- 标题和统计信息 -->
      <div class="flex items-center justify-between mb-4 gap-4">
        <h3 class="font-bold text-lg">知识生成结果</h3>

        <!-- 统计信息 -->
        <div class="rounded-lg p-2 bg-primary/20 flex items-center justify-between w-40">
          <div class="text-xs text-gray-600 font-semibold">节点</div>
          <div class="text-2xl font-bold text-primary">{{ nodeCount }}</div>
        </div>
      </div>

      <!-- 节点表格 -->
      <div class="overflow-auto max-h-[400px] border rounded-lg">
        <table class="table table-zebra table-sm w-full">
          <thead class="sticky top-0 bg-base-200 z-10">
            <tr>
              <th class="w-12">#</th>
              <th class="w-48">标签</th>
              <th class="w-32">名称</th>
              <th>属性</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(node, index) in nodes" :key="index">
              <td>{{ index + 1 }}</td>
              <td>
                <div class="flex flex-wrap gap-1">
                  <span
                    v-for="label in node.labels"
                    :key="label"
                    class="badge badge-primary badge-sm"
                  >
                    {{ label }}
                  </span>
                </div>
              </td>
              <td class="font-semibold">{{ node.properties?.name || '-' }}</td>
              <td>
                <div class="text-sm space-y-1">
                  <div v-for="(value, key) in node.properties" :key="key">
                    <span class="font-semibold text-gray-600">{{ key }}:</span>
                    <span class="ml-1">{{ value }}</span>
                  </div>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
        <div v-if="nodes.length === 0" class="text-center py-8 text-gray-500">没有生成节点</div>
      </div>

      <div class="modal-action">
        <button class="btn" @click="$emit('close')">关闭</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  show: {
    type: Boolean,
    default: false,
  },
  nodes: {
    type: Array,
    default: () => [],
  },
});

defineEmits(['close']);

const nodeCount = computed(() => props.nodes.length);
</script>
