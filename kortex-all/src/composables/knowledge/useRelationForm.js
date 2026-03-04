import { reactive, ref } from 'vue';
import { validatePropertyKey } from '../../utils/neo4jValidation';

export function useRelationForm(searchNodesByName) {
  const relationForm = reactive({
    start: null,
    end: null,
    relation: {
      type: '',
      properties: [], // 改为数组，每个元素包含 {id, key, value}
    },
    // 搜索相关字段
    startSearch: '',
    endSearch: '',
    filteredStartNodes: [],
    filteredEndNodes: [],
    showStartDropdown: false,
    showEndDropdown: false,
    startSearchLoading: false,
    endSearchLoading: false,
  });

  const propertyErrors = ref({}); // 存储每个属性的验证错误

  // 防抖定时器
  let startSearchTimeout = null;
  let endSearchTimeout = null;

  const resetRelationForm = () => {
    // 清理防抖定时器
    if (startSearchTimeout) {
      clearTimeout(startSearchTimeout);
      startSearchTimeout = null;
    }
    if (endSearchTimeout) {
      clearTimeout(endSearchTimeout);
      endSearchTimeout = null;
    }

    relationForm.start = null;
    relationForm.end = null;
    relationForm.relation = { type: '', properties: [] };
    relationForm.startSearch = '';
    relationForm.endSearch = '';
    relationForm.filteredStartNodes = [];
    relationForm.filteredEndNodes = [];
    relationForm.showStartDropdown = false;
    relationForm.showEndDropdown = false;
    relationForm.startSearchLoading = false;
    relationForm.endSearchLoading = false;
    propertyErrors.value = {};
  };

  const initRelationForm = (relation = null) => {
    if (relation) {
      relationForm.start = relation.start;
      relationForm.end = relation.end;
      // 将对象属性转换为数组格式
      const properties = Array.isArray(relation.relation.properties)
        ? relation.relation.properties
        : Object.entries(relation.relation.properties || {}).map(([key, value]) => ({
            id: `${key}_${Date.now()}_${Math.random()}`,
            key,
            value,
          }));
      relationForm.relation = {
        ...relation.relation,
        properties,
      };
      relationForm.startSearch = relation.start?.properties?.name || relation.start?.id || '';
      relationForm.endSearch = relation.end?.properties?.name || relation.end?.id || '';
    } else {
      resetRelationForm();
    }
  };

  const onStartNodeSearch = () => {
    if (startSearchTimeout) {
      clearTimeout(startSearchTimeout);
    }

    relationForm.showStartDropdown = true;

    if (!relationForm.startSearch.trim()) {
      relationForm.filteredStartNodes = [];
      return;
    }

    relationForm.startSearchLoading = true;

    startSearchTimeout = setTimeout(async () => {
      try {
        relationForm.filteredStartNodes = await searchNodesByName(relationForm.startSearch);
      } catch (error) {
        console.error('搜索起始节点失败:', error);
        relationForm.filteredStartNodes = [];
      } finally {
        relationForm.startSearchLoading = false;
      }
    }, 300);
  };

  const onEndNodeSearch = () => {
    if (endSearchTimeout) {
      clearTimeout(endSearchTimeout);
    }

    relationForm.showEndDropdown = true;

    if (!relationForm.endSearch.trim()) {
      relationForm.filteredEndNodes = [];
      return;
    }

    relationForm.endSearchLoading = true;

    endSearchTimeout = setTimeout(async () => {
      try {
        relationForm.filteredEndNodes = await searchNodesByName(relationForm.endSearch);
      } catch (error) {
        console.error('搜索结束节点失败:', error);
        relationForm.filteredEndNodes = [];
      } finally {
        relationForm.endSearchLoading = false;
      }
    }, 300);
  };

  const selectStartNode = (node) => {
    relationForm.start = node;
    relationForm.startSearch = node.properties?.name || node.id;
    relationForm.showStartDropdown = false;
  };

  const selectEndNode = (node) => {
    relationForm.end = node;
    relationForm.endSearch = node.properties?.name || node.id;
    relationForm.showEndDropdown = false;
  };

  const onStartNodeFocus = () => {
    relationForm.showStartDropdown = true;
    if (
      relationForm.startSearch.trim() &&
      relationForm.filteredStartNodes.length === 0 &&
      !relationForm.startSearchLoading
    ) {
      onStartNodeSearch();
    }
  };

  const onEndNodeFocus = () => {
    relationForm.showEndDropdown = true;
    if (
      relationForm.endSearch.trim() &&
      relationForm.filteredEndNodes.length === 0 &&
      !relationForm.endSearchLoading
    ) {
      onEndNodeSearch();
    }
  };

  const onStartNodeBlur = () => {
    setTimeout(() => {
      relationForm.showStartDropdown = false;
    }, 200);
  };

  const onEndNodeBlur = () => {
    setTimeout(() => {
      relationForm.showEndDropdown = false;
    }, 200);
  };

  const addRelationProperty = () => {
    relationForm.relation.properties.push({
      id: `prop_${Date.now()}_${Math.random()}`,
      key: '',
      value: '',
    });
  };

  const deleteRelationProperty = (id) => {
    const index = relationForm.relation.properties.findIndex((prop) => prop.id === id);
    if (index !== -1) {
      relationForm.relation.properties.splice(index, 1);
    }
  };

  const updateRelationProperty = (id, newKey, newValue) => {
    const prop = relationForm.relation.properties.find((p) => p.id === id);
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
      const duplicates = relationForm.relation.properties.filter(
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

    relationForm.relation.properties.forEach((prop) => {
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
    relationForm.relation.properties.forEach((prop) => {
      if (prop.key.trim()) {
        obj[prop.key.trim()] = prop.value;
      }
    });
    return obj;
  };

  return {
    relationForm,
    propertyErrors,
    resetRelationForm,
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
    validateAllProperties,
    getPropertiesObject,
  };
}
