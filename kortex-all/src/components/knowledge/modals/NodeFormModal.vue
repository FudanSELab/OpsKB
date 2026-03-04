<template>
  <dialog class="modal" :class="{ 'modal-open': show }">
    <div class="modal-box">
      <h3 class="font-bold text-lg mb-4">{{ isEdit ? '编辑节点' : '添加节点' }}</h3>
      <form @submit.prevent="handleSubmit">
        <div class="form-control">
          <label class="label">
            <span class="label-text">知识分类</span>
          </label>
          <div class="flex gap-4 mb-3">
            <label class="cursor-pointer flex items-center gap-2">
              <input
                type="radio"
                v-model="knowledgeType"
                value="entity"
                class="radio radio-sm radio-primary"
              />
              <span class="label-text">实体</span>
            </label>
            <label class="cursor-pointer flex items-center gap-2">
              <input
                type="radio"
                v-model="knowledgeType"
                value="event"
                class="radio radio-sm radio-primary"
              />
              <span class="label-text">事件</span>
            </label>
            <label class="cursor-pointer flex items-center gap-2">
              <input
                type="radio"
                v-model="knowledgeType"
                value="model"
                class="radio radio-sm radio-primary"
              />
              <span class="label-text">模型</span>
            </label>
            <label class="cursor-pointer flex items-center gap-2">
              <input
                type="radio"
                v-model="knowledgeType"
                value="tree"
                class="radio radio-sm radio-primary"
              />
              <span class="label-text">策略</span>
            </label>
          </div>
        </div>

        <div class="form-control mb-4">
          <label class="label">
            <span class="label-text">细分类别</span>
          </label>
          <input
            type="text"
            :disabled="knowledgeType === 'tree'"
            v-model="categoryInput"
            placeholder="输入类别名称"
            class="input input-sm input-bordered mb-2"
          />
          <div v-if="availableCategories.length > 0" class="mt-2">
            <div class="text-xs text-gray-500 mb-1">已有类别（点击快速添加）：</div>
            <div class="flex flex-wrap gap-1">
              <button
                v-for="category in availableCategories"
                :key="category"
                type="button"
                class="badge badge-outline badge-sm cursor-pointer hover:badge-primary"
                @click="selectCategory(category)"
              >
                {{ category }}
              </button>
            </div>
          </div>
        </div>

        <div class="form-control mb-4">
          <label class="label">
            <span class="label-text">节点属性</span>
          </label>
          <div class="space-y-2">
            <div v-for="prop in nodeForm.properties" :key="prop.id">
              <div class="flex gap-2">
                <div class="flex-1">
                  <input
                    type="text"
                    placeholder="属性名（支持中文及标点，不能以数字开头）"
                    class="input input-bordered input-sm w-full"
                    :value="prop.key"
                    :disabled="isEdit && prop.key === 'name'"
                    :class="{
                      'input-disabled cursor-not-allowed': isEdit && prop.key === 'name',
                      'input-error': propertyErrors[prop.id],
                    }"
                    @input="updateNodeProperty(prop.id, $event.target.value, undefined)"
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
                  @input="updateNodeProperty(prop.id, undefined, $event.target.value)"
                />
                <button
                  type="button"
                  class="btn btn-sm btn-error"
                  :disabled="isEdit && prop.key === 'name'"
                  :class="{ 'btn-disabled': isEdit && prop.key === 'name' }"
                  @click="deleteNodeProperty(prop.id)"
                >
                  删除
                </button>
              </div>
            </div>
            <button type="button" class="btn btn-sm btn-outline" @click="addNodeProperty">
              添加属性
            </button>
          </div>
        </div>

        <div class="modal-action">
          <button type="submit" class="btn btn-primary" :disabled="loading">
            {{ loading ? (isEdit ? '更新中...' : '创建中...') : isEdit ? '更新' : '创建' }}
          </button>
          <button type="button" class="btn" @click="handleCancel">取消</button>
        </div>
      </form>
    </div>
  </dialog>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue';
import * as knowledgeApi from '../../../api/knowledge';

const props = defineProps({
  show: {
    type: Boolean,
    default: false,
  },
  isEdit: {
    type: Boolean,
    default: false,
  },
  nodeForm: {
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
  addNodeProperty: {
    type: Function,
    required: true,
  },
  deleteNodeProperty: {
    type: Function,
    required: true,
  },
  updateNodeProperty: {
    type: Function,
    required: true,
  },
  validateAllProperties: {
    type: Function,
    required: true,
  },
});

const emit = defineEmits(['submit', 'cancel']);

const knowledgeType = ref('entity');
const categoryInput = ref('');
const availableCategories = ref([]);

// 监听知识类型变化，加载对应的已有类别并更新labels
watch(knowledgeType, async (newType) => {
  await loadCategories(newType);
  updateLabels();
});

// 监听类别输入变化，实时更新labels
watch(categoryInput, () => {
  updateLabels();
});

// 更新labels数组
const updateLabels = () => {
  if (categoryInput.value && categoryInput.value.trim()) {
    props.nodeForm.labels = [knowledgeType.value, categoryInput.value.trim()];
  } else {
    props.nodeForm.labels = [knowledgeType.value];
  }
};

// 加载类别列表
const loadCategories = async (type) => {
  try {
    let result;
    if (type === 'entity') {
      result = await knowledgeApi.getEntityCategories();
    } else if (type === 'event') {
      result = await knowledgeApi.getEventCategories();
    } else if (type === 'model') {
      result = await knowledgeApi.getModelCategories();
    }

    if (result && result.flag && result.data) {
      // data格式可能是 { main: [], detail: [] } 或者 { 类别名: 数量, ... }
      if (Array.isArray(result.data.main)) {
        availableCategories.value = result.data.main;
      } else if (typeof result.data === 'object') {
        availableCategories.value = Object.keys(result.data);
      } else {
        availableCategories.value = [];
      }
    } else {
      availableCategories.value = [];
    }
  } catch (error) {
    console.error('加载类别失败:', error);
    availableCategories.value = [];
  }
};

// 快速选择已有类别
const selectCategory = (category) => {
  categoryInput.value = category;
};

const handleSubmit = () => {
  // 提交前验证所有属性
  if (!props.validateAllProperties()) {
    return; // 如果验证失败，不提交
  }
  emit('submit');
};

const handleCancel = () => {
  categoryInput.value = '';
  emit('cancel');
};

// 组件挂载时加载类别
onMounted(() => {
  loadCategories(knowledgeType.value);
});

// 监听show属性，当对话框打开时加载类别
watch(
  () => props.show,
  (newVal) => {
    if (newVal) {
      // 从现有labels推断知识类型和类别
      if (props.nodeForm.labels && props.nodeForm.labels.length > 0) {
        // 从labels中找到知识类型
        const foundType = props.nodeForm.labels.find((label) =>
          ['entity', 'event', 'model', 'tree'].includes(label.toLowerCase()),
        );

        if (foundType) {
          knowledgeType.value = foundType.toLowerCase();

          // 找到另一个非知识类型的label作为类别（tree类型除外）
          if (foundType.toLowerCase() !== 'tree') {
            const category = props.nodeForm.labels.find(
              (label) => !['entity', 'event', 'model', 'tree'].includes(label.toLowerCase()),
            );
            categoryInput.value = category || '';
          } else {
            categoryInput.value = '';
          }
        } else {
          // 没有找到有效的知识类型，使用默认值
          knowledgeType.value = '';
          categoryInput.value = '';
        }
      } else {
        // 如果没有labels，重置为默认值
        knowledgeType.value = '';
        categoryInput.value = '';
      }
      loadCategories(knowledgeType.value);
    } else {
      // modal关闭时，清理本地状态
      categoryInput.value = '';
      availableCategories.value = [];
    }
  },
);
</script>
