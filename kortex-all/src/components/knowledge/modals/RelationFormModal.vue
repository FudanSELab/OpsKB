<template>
  <dialog class="modal" :class="{ 'modal-open': show }">
    <div class="modal-box">
      <div class="flex justify-between items-center mb-4">
        <h3 class="font-bold text-lg">{{ isEdit ? '编辑关系' : '添加关系' }}</h3>
        <button
          v-if="isEdit"
          type="button"
          class="btn btn-sm btn-error"
          @click="$emit('delete')"
          :disabled="loading"
        >
          删除关系
        </button>
      </div>
      <form @submit.prevent="handleSubmit">
        <!-- 起始节点搜索 -->
        <div class="form-control mb-4">
          <label class="label">
            <span class="label-text">起始节点</span>
          </label>
          <div class="relative">
            <input
              type="text"
              class="input input-bordered w-full"
              :placeholder="isEdit ? '' : '搜索并选择起始节点...'"
              v-model="relationForm.startSearch"
              @input="$emit('start-search')"
              @focus="$emit('start-focus')"
              @blur="$emit('start-blur')"
              :disabled="isEdit"
            />
            <div
              v-if="relationForm.showStartDropdown && !isEdit"
              class="absolute z-50 w-full bg-white border border-gray-300 rounded-md shadow-lg max-h-48 overflow-y-auto top-full mt-1"
            >
              <div
                v-if="relationForm.startSearchLoading"
                class="px-3 py-2 text-sm text-gray-500 flex items-center gap-2"
              >
                <span class="loading loading-spinner loading-xs"></span>
                搜索中...
              </div>
              <div
                v-else-if="
                  relationForm.filteredStartNodes.length === 0 && relationForm.startSearch.trim()
                "
                class="px-3 py-2 text-sm text-gray-500"
              >
                没有找到匹配的节点
              </div>
              <div
                v-else-if="relationForm.filteredStartNodes.length === 0"
                class="px-3 py-2 text-sm text-gray-500"
              >
                请输入节点名称进行搜索
              </div>
              <div
                v-for="node in relationForm.filteredStartNodes"
                :key="node.id"
                class="px-3 py-2 hover:bg-gray-100 cursor-pointer text-sm"
                @mousedown.prevent="$emit('select-start', node)"
              >
                <div class="font-medium">{{ node.properties?.name || node.id }}</div>
                <div class="text-xs text-gray-500">
                  {{ node.labels?.join(' · ') }}
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 结束节点搜索 -->
        <div class="form-control mb-4">
          <label class="label">
            <span class="label-text">结束节点</span>
          </label>
          <div class="relative">
            <input
              type="text"
              class="input input-bordered w-full"
              :placeholder="isEdit ? '' : '搜索并选择结束节点...'"
              v-model="relationForm.endSearch"
              @input="$emit('end-search')"
              @focus="$emit('end-focus')"
              @blur="$emit('end-blur')"
              :disabled="isEdit"
            />
            <div
              v-if="relationForm.showEndDropdown && !isEdit"
              class="absolute z-50 w-full bg-white border border-gray-300 rounded-md shadow-lg max-h-48 overflow-y-auto top-full mt-1"
            >
              <div
                v-if="relationForm.endSearchLoading"
                class="px-3 py-2 text-sm text-gray-500 flex items-center gap-2"
              >
                <span class="loading loading-spinner loading-xs"></span>
                搜索中...
              </div>
              <div
                v-else-if="
                  relationForm.filteredEndNodes.length === 0 && relationForm.endSearch.trim()
                "
                class="px-3 py-2 text-sm text-gray-500"
              >
                没有找到匹配的节点
              </div>
              <div
                v-else-if="relationForm.filteredEndNodes.length === 0"
                class="px-3 py-2 text-sm text-gray-500"
              >
                请输入节点名称进行搜索
              </div>
              <div
                v-for="node in relationForm.filteredEndNodes"
                :key="node.id"
                class="px-3 py-2 hover:bg-gray-100 cursor-pointer text-sm"
                @mousedown.prevent="$emit('select-end', node)"
              >
                <div class="font-medium">{{ node.properties?.name || node.id }}</div>
                <div class="text-xs text-gray-500">
                  {{ node.labels?.join(' · ') }}
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="form-control mb-4">
          <label class="label">
            <span class="label-text">关系类型</span>
          </label>
          <input
            type="text"
            placeholder="如：AUTHORED, BELONGS_TO等"
            class="input input-bordered"
            v-model="relationForm.relation.type"
            required
          />
        </div>

        <div class="form-control mb-4">
          <label class="label">
            <span class="label-text">关系属性</span>
          </label>
          <div class="space-y-2">
            <div v-for="prop in relationForm.relation.properties" :key="prop.id">
              <div class="flex gap-2">
                <div class="flex-1">
                  <input
                    type="text"
                    placeholder="属性名（支持中文及标点，不能以数字开头）"
                    class="input input-bordered input-sm w-full"
                    :value="prop.key"
                    :class="{ 'input-error': propertyErrors[prop.id] }"
                    @input="updateRelationProperty(prop.id, $event.target.value, undefined)"
                  />
                  <label v-if="propertyErrors[prop.id]" class="label py-0.5">
                    <span class="label-text-alt text-error text-xs">{{
                      propertyErrors[prop.id]
                    }}</span>
                  </label>
                </div>
                <input
                  type="text"
                  placeholder="属性值"
                  class="input input-bordered input-sm flex-1"
                  :value="prop.value"
                  @input="updateRelationProperty(prop.id, undefined, $event.target.value)"
                />
                <button
                  type="button"
                  class="btn btn-sm btn-error"
                  @click="deleteRelationProperty(prop.id)"
                >
                  删除
                </button>
              </div>
            </div>
            <button type="button" class="btn btn-sm btn-outline" @click="addRelationProperty">
              添加属性
            </button>
          </div>
        </div>

        <div class="modal-action">
          <button
            type="submit"
            class="btn btn-primary"
            :disabled="
              loading ||
              (!isEdit && (!relationForm.start || !relationForm.end || !relationForm.relation.type))
            "
          >
            {{ loading ? (isEdit ? '更新中...' : '创建中...') : isEdit ? '更新' : '创建' }}
          </button>
          <button type="button" class="btn" @click="$emit('cancel')">取消</button>
        </div>
      </form>
    </div>
  </dialog>
</template>

<script setup>
import { watch } from 'vue';

const props = defineProps({
  show: {
    type: Boolean,
    default: false,
  },
  isEdit: {
    type: Boolean,
    default: false,
  },
  relationForm: {
    type: Object,
    required: true,
  },
  loading: {
    type: Boolean,
    default: false,
  },
  propertyErrors: {
    type: Object,
    default: () => ({}),
  },
  addRelationProperty: {
    type: Function,
    required: true,
  },
  deleteRelationProperty: {
    type: Function,
    required: true,
  },
  updateRelationProperty: {
    type: Function,
    required: true,
  },
  validateAllProperties: {
    type: Function,
    required: true,
  },
});

const emit = defineEmits([
  'submit',
  'cancel',
  'delete',
  'start-search',
  'end-search',
  'select-start',
  'select-end',
  'start-focus',
  'end-focus',
  'start-blur',
  'end-blur',
]);

const handleSubmit = () => {
  // 提交前验证所有属性
  if (!props.validateAllProperties()) {
    return; // 如果验证失败，不提交
  }
  emit('submit');
};

// 监听弹框显示状态，关闭时清理搜索下拉框状态
watch(
  () => props.show,
  (newVal) => {
    if (!newVal) {
      // modal关闭时，清理搜索相关的状态
      if (props.relationForm) {
        props.relationForm.showStartDropdown = false;
        props.relationForm.showEndDropdown = false;
        props.relationForm.filteredStartNodes = [];
        props.relationForm.filteredEndNodes = [];
      }
    }
  },
);
</script>
