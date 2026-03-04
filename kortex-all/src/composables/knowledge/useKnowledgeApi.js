import { ref } from 'vue';
import * as knowledgeAPI from '../../api/knowledge.js';

/**
 * 知识图谱 API Composable
 * 封装响应式状态和业务逻辑
 */
export function useKnowledgeApi() {
  const loading = ref(false);
  const error = ref(null);

  /**
   * 通用请求包装器 - 处理 loading 和 error 状态
   * @param {Function} apiFunc API 函数
   * @returns {Promise} API 响应
   */
  const wrapRequest = async (apiFunc) => {
    try {
      loading.value = true;
      error.value = null;
      const result = await apiFunc();
      return result;
    } catch (err) {
      console.error('API请求失败:', err);
      error.value = err.message || '网络请求失败';
      return { flag: false, data: error.value };
    } finally {
      loading.value = false;
    }
  };

  // ==================== API 方法封装 ====================
  const api = {
    // 节点相关API
    async queryNodeByName(name, exactMatch = false) {
      return await wrapRequest(() => knowledgeAPI.queryNodeByName(name, exactMatch));
    },

    async queryNodeByCategory(categoryMain, categoryDetail) {
      return await wrapRequest(() =>
        knowledgeAPI.queryNodeByCategory(categoryMain, categoryDetail),
      );
    },

    async getModelNode(name) {
      return await wrapRequest(() => knowledgeAPI.getModelNode(name));
    },

    async getEntityCategories() {
      return await wrapRequest(() => knowledgeAPI.getEntityCategories());
    },

    async getEventCategories() {
      return await wrapRequest(() => knowledgeAPI.getEventCategories());
    },

    async getModelCategories() {
      return await wrapRequest(() => knowledgeAPI.getModelCategories());
    },

    async getTreeCategories() {
      return await wrapRequest(() => knowledgeAPI.getTreeCategories());
    },

    async getNodeTotal() {
      return await wrapRequest(() => knowledgeAPI.getNodeTotal());
    },

    async getNodeCountByCategory() {
      return await wrapRequest(() => knowledgeAPI.getNodeCountByCategory());
    },

    async getEntityCountByCategory() {
      return await wrapRequest(() => knowledgeAPI.getEntityCountByCategory());
    },

    async getEventCountByCategory() {
      return await wrapRequest(() => knowledgeAPI.getEventCountByCategory());
    },

    async getModelCountByCategory() {
      return await wrapRequest(() => knowledgeAPI.getModelCountByCategory());
    },

    async getTreeCountByCategory() {
      return await wrapRequest(() => knowledgeAPI.getTreeCountByCategory());
    },

    async createNode(nodeData) {
      return await wrapRequest(() => knowledgeAPI.createNode(nodeData));
    },

    async deleteNode(nodeData) {
      return await wrapRequest(() => knowledgeAPI.deleteNode(nodeData));
    },

    async updateNodePropertiesById(nodeId, properties, labels = null) {
      return await wrapRequest(() =>
        knowledgeAPI.updateNodePropertiesById(nodeId, properties, labels),
      );
    },

    // 关系相关API
    // 辅助函数：构建关系请求数据（支持ID和name）
    buildRelationData(start, end, relation) {
      return knowledgeAPI.buildRelationData(start, end, relation);
    },

    async getNodeRelation(nodeData) {
      return await wrapRequest(() => knowledgeAPI.getNodeRelation(nodeData));
    },

    async queryGraph(name, type) {
      return await wrapRequest(() => knowledgeAPI.queryGraph(name, type));
    },

    async createRelation(relationData) {
      return await wrapRequest(() => knowledgeAPI.createRelation(relationData));
    },

    async deleteRelation(relationData) {
      return await wrapRequest(() => knowledgeAPI.deleteRelation(relationData));
    },

    async updateRelationProperties(relationData) {
      return await wrapRequest(() => knowledgeAPI.updateRelationProperties(relationData));
    },

    // 主要功能API
    async getAll() {
      return await wrapRequest(() => knowledgeAPI.getAll());
    },

    async listKnowledgeBases() {
      return await wrapRequest(() => knowledgeAPI.listKnowledgeBases());
    },

    setActiveKnowledgeBase(kbId) {
      return knowledgeAPI.setActiveKnowledgeBase(kbId);
    },

    getActiveKnowledgeBase() {
      return knowledgeAPI.getActiveKnowledgeBase();
    },

    setActiveStorageSource(source) {
      return knowledgeAPI.setActiveStorageSource(source);
    },

    getActiveStorageSource() {
      return knowledgeAPI.getActiveStorageSource();
    },

    async loadFromCSV(file, type) {
      return await wrapRequest(() => knowledgeAPI.loadFromCSV(file, type));
    },
    // 融合相关API
    async fusion(events, categories) {
      return await wrapRequest(() => knowledgeAPI.fusion(events, categories));
    },
  };

  return {
    loading,
    error,
    api,
  };
}
