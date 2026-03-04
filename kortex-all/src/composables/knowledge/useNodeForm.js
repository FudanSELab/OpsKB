import { reactive, ref } from 'vue';
import { validatePropertyKey } from '../../utils/neo4jValidation';

export function useNodeForm() {
  const nodeForm = reactive({
    id: null,
    labels: [],
    properties: [], // 改为数组，每个元素包含 {id, key, value}
  });

  const propertyErrors = ref({}); // 存储每个属性的验证错误

  const resetNodeForm = () => {
    nodeForm.id = null;
    nodeForm.labels = [];
    nodeForm.properties = [];
    propertyErrors.value = {};
  };

  const initNodeForm = (node) => {
    if (node) {
      nodeForm.id = node.id;
      nodeForm.labels = [...(node.labels || [])];
      // 将对象属性转换为数组格式
      nodeForm.properties = Object.entries(node.properties || {}).map(([key, value]) => ({
        id: `${key}_${Date.now()}_${Math.random()}`,
        key,
        value,
      }));
    } else {
      resetNodeForm();
      // 默认添加name属性
      nodeForm.properties = [{ id: `name_${Date.now()}`, key: 'name', value: '' }];
    }
  };

  const addNodeProperty = () => {
    nodeForm.properties.push({
      id: `prop_${Date.now()}_${Math.random()}`,
      key: '',
      value: '',
    });
  };

  const deleteNodeProperty = (id) => {
    const index = nodeForm.properties.findIndex((prop) => prop.id === id);
    if (index !== -1) {
      nodeForm.properties.splice(index, 1);
    }
  };

  const updateNodeProperty = (id, newKey, newValue) => {
    const prop = nodeForm.properties.find((p) => p.id === id);
    if (prop) {
      if (newKey !== undefined) {
        prop.key = newKey;
        // 验证新的键名
        validateProperty(id, newKey);
      }
      if (newValue !== undefined) prop.value = newValue;
    }
  };

  // 验证单个属性
  const validateProperty = (id, key) => {
    const validation = validatePropertyKey(key);
    if (!validation.valid) {
      propertyErrors.value[id] = validation.error;
    } else {
      // 检查重复的键名
      const duplicates = nodeForm.properties.filter(
        (p) => p.id !== id && p.key.trim().toLowerCase() === key.trim().toLowerCase(),
      );
      if (duplicates.length > 0) {
        propertyErrors.value[id] = `属性名 "${key.trim()}" 重复`;
      } else {
        delete propertyErrors.value[id];
      }
    }
  };

  // 验证所有属性
  const validateAllProperties = () => {
    propertyErrors.value = {};
    const keyCount = {};
    let hasError = false;

    nodeForm.properties.forEach((prop) => {
      const key = prop.key.trim();

      // 验证单个键名
      const validation = validatePropertyKey(key);
      if (!validation.valid) {
        propertyErrors.value[prop.id] = validation.error;
        hasError = true;
        return;
      }

      // 检查重复的键名（不区分大小写）
      const lowerKey = key.toLowerCase();
      if (keyCount[lowerKey]) {
        propertyErrors.value[prop.id] = `属性名 "${key}" 重复`;
        hasError = true;
      } else {
        keyCount[lowerKey] = true;
      }
    });

    return !hasError;
  };

  // 辅助函数：将数组格式转换为对象格式（用于提交数据）
  const getPropertiesObject = () => {
    const obj = {};
    nodeForm.properties.forEach((prop) => {
      if (prop.key.trim()) {
        obj[prop.key.trim()] = prop.value;
      }
    });
    return obj;
  };

  return {
    nodeForm,
    propertyErrors,
    resetNodeForm,
    initNodeForm,
    addNodeProperty,
    deleteNodeProperty,
    updateNodeProperty,
    validateAllProperties,
    getPropertiesObject,
  };
}
