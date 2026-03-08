/**
 * 知识图谱相关的API接口
 */

const API_BASE_URL = '';
let activeKnowledgeBaseId = 'fault-kb';
let activeStorageSource = 'NEO4J';

const appendKbId = (
  url,
  kbId = activeKnowledgeBaseId,
  storageSource = activeStorageSource,
) => {
  const queryParams = [];
  if (kbId) {
    queryParams.push(`kbId=${encodeURIComponent(kbId)}`);
  }
  if (storageSource) {
    queryParams.push(`storage=${encodeURIComponent(storageSource)}`);
  }
  if (queryParams.length === 0) return url;
  const joiner = url.includes('?') ? '&' : '?';
  return `${url}${joiner}${queryParams.join('&')}`;
};
/**
 * 通用请求封装
 * @param {string} url 请求路径
 * @param {Object} options 请求选项
 * @returns {Promise} API响应
 */
const request = async (url, options = {}) => {
  try {
    const response = await fetch(API_BASE_URL + url, {
      headers: {
        'Content-Type': 'application/json',
        ...options.headers,
      },
      ...options,
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    return await response.json();
  } catch (error) {
    console.error('API请求失败:', error);
    throw error;
  }
};

// ==================== 节点相关API ====================

/**
 * 根据名称查询节点
 * @param {string} name 节点名称
 * @param {boolean} exactMatch 是否精确匹配（可选，默认false为模糊匹配）
 * @returns {Promise} API响应
 */
export const queryNodeByName = async (name, exactMatch = false) => {
  let url = `/knowledgebase/queryByName?name=${encodeURIComponent(name)}`;
  if (exactMatch) {
    url += `&exactMatch=true`;
  }
  return await request(appendKbId(url));
};

/**
 * 根据分类查询节点
 * @param {string} categoryMain 主分类
 * @param {string} categoryDetail 详细分类（可选）
 * @returns {Promise} API响应
 */
export const queryNodeByCategory = async (categoryMain, categoryDetail, page = null, size = null) => {
  let url = `/knowledgebase/queryNodeByCategory?category_main=${encodeURIComponent(categoryMain)}`;
  if (categoryDetail) {
    url += `&category_detail=${encodeURIComponent(categoryDetail)}`;
  }
  if (page !== null) {
    url += `&page=${page}`;
  }
  if (size !== null) {
    url += `&size=${size}`;
  }
  return await request(appendKbId(url));
};

/**
 * 获取模型节点
 * @param {string} name 模型名称
 * @returns {Promise} API响应
 */
export const getModelNode = async (name) => {
  return await request(appendKbId(`/knowledgebase/queryByName?name=${encodeURIComponent(name)}&exactMatch=true`));
};

/**
 * 获取实体分类列表
 * @returns {Promise} API响应
 */
export const getEntityCategories = async () => {
  return await request(appendKbId('/knowledgebase/getEntityCategories'));
};

/**
 * 获取事件分类列表
 * @returns {Promise} API响应
 */
export const getEventCategories = async () => {
  return await request(appendKbId('/knowledgebase/getEventCategories'));
};

/**
 * 获取模型分类列表
 * @returns {Promise} API响应
 */
export const getModelCategories = async () => {
  return await request(appendKbId('/knowledgebase/getModelCategories'));
};

/**
 * 获取节点总数
 * @returns {Promise} API响应
 */
export const getNodeTotal = async () => {
  return await request(appendKbId('/knowledgebase/getNodeTotal'));
};

/**
 * 获取按分类统计的节点数量
 * @returns {Promise} API响应
 */
export const getNodeCountByCategory = async () => {
  return await request(appendKbId('/knowledgebase/getNodeCountByCategory'));
};

/**
 * 获取按分类统计的实体数量
 * @returns {Promise} API响应
 */
export const getEntityCountByCategory = async () => {
  return await request(appendKbId('/knowledgebase/getEntityCountByCategory'));
};

/**
 * 获取按分类统计的事件数量
 * @returns {Promise} API响应
 */
export const getEventCountByCategory = async () => {
  return await request(appendKbId('/knowledgebase/getEventCountByCategory'));
};

/**
 * 获取按分类统计的模型数量
 * @returns {Promise} API响应
 */
export const getModelCountByCategory = async () => {
  return await request(appendKbId('/knowledgebase/getModelCountByCategory'));
};

/**
 * 获取树的分类列表
 * @returns {Promise} API响应
 */
export const getTreeCategories = async () => {
  return await request(appendKbId('/knowledgebase/getTreeCategories'));
};

/**
 * 获取按分类统计的树数量
 * @returns {Promise} API响应
 */
export const getTreeCountByCategory = async () => {
  return await request(appendKbId('/knowledgebase/getTreeCountByCategory'));
};

/**
 * 创建节点
 * @param {Object} nodeData 节点数据
 * @returns {Promise} API响应
 */
export const createNode = async (nodeData) => {
  return await request(appendKbId('/node/create'), {
    method: 'POST',
    body: JSON.stringify(nodeData),
  });
};

/**
 * 删除节点
 * @param {Object} nodeData 节点数据
 * @returns {Promise} API响应
 */
export const deleteNode = async (nodeData) => {
  return await request('/node/delete', {
    method: 'POST',
    body: JSON.stringify(nodeData),
  });
};

/**
 * 根据节点ID更新节点属性和标签
 * @param {number} nodeId 节点ID
 * @param {Object} properties 属性对象
 * @param {Array} labels 标签数组（可选）
 * @returns {Promise} API响应
 */
export const updateNodePropertiesById = async (nodeId, properties, labels = null) => {
  const body = { nodeId, properties };
  if (labels !== null) {
    body.labels = labels;
  }
  return await request('/node/updatePropertiesById', {
    method: 'POST',
    body: JSON.stringify(body),
  });
};

// ==================== 关系相关API ====================

/**
 * 构建节点参数（优先使用ID，否则使用name）
 * @param {Object} node 节点对象
 * @returns {Object} 节点参数对象
 */
const buildNodeParam = (node) => {
  if (!node) {
    return null;
  }

  // 优先使用ID（推荐方式，更精确和高效）
  if (node.id !== undefined && node.id !== null) {
    return { id: node.id };
  }

  // 回退到使用name（向后兼容）
  if (node.properties && node.properties.name) {
    return {
      properties: {
        name: node.properties.name,
      },
    };
  }

  // 如果都没有，尝试直接返回节点对象（兼容旧格式）
  return node;
};

/**
 * 构建关系请求数据（支持ID和name）
 * @param {Object} start 起始节点
 * @param {Object} end 结束节点
 * @param {Object} relation 关系数据
 * @returns {Object} 关系请求数据
 */
export const buildRelationData = (start, end, relation) => {
  return {
    start: buildNodeParam(start),
    end: buildNodeParam(end),
    relation: relation,
  };
};

/**
 * 获取节点关系
 * @param {Object} nodeData 节点数据
 * @returns {Promise} API响应
 */
export const getNodeRelation = async (nodeData) => {
  return await request(appendKbId('/knowledgebase/getNodeRelation'), {
    method: 'POST',
    body: JSON.stringify(nodeData),
  });
};

/**
 * 创建关系
 * @param {Object} relationData 关系数据，支持直接传入或通过buildRelationData构建
 * @returns {Promise} API响应
 */
export const createRelation = async (relationData) => {
  return await request('/relation/create', {
    method: 'POST',
    body: JSON.stringify(relationData),
  });
};

/**
 * 删除关系
 * @param {Object} relationData 关系数据，支持直接传入或通过buildRelationData构建
 * @returns {Promise} API响应
 */
export const deleteRelation = async (relationData) => {
  return await request('/relation/delete', {
    method: 'POST',
    body: JSON.stringify(relationData),
  });
};

/**
 * 更新关系属性
 * @param {Object} relationData 关系数据，支持直接传入或通过buildRelationData构建
 * @returns {Promise} API响应
 */
export const updateRelationProperties = async (relationData) => {
  return await request('/relation/updateRelationProperties', {
    method: 'POST',
    body: JSON.stringify(relationData),
  });
};

// ==================== 主要功能API ====================

/**
 * 获取所有数据
 * @returns {Promise} API响应
 */
export const getAll = async (limit) => {
  const url = limit ? appendKbId(`/knowledgebase/getAll?limit=${limit}`) : appendKbId('/knowledgebase/getAll');
  return await request(url);
};

export const listKnowledgeBases = async () => {
  return await request('/knowledgebase/list');
};

export const setActiveKnowledgeBase = (kbId) => {
  if (kbId) {
    activeKnowledgeBaseId = kbId;
  }
  return activeKnowledgeBaseId;
};

export const getActiveKnowledgeBase = () => activeKnowledgeBaseId;

export const setActiveStorageSource = (source) => {
  if (source) {
    activeStorageSource = source;
  }
  return activeStorageSource;
};

export const getActiveStorageSource = () => activeStorageSource;

/**
 * 从CSV文件加载数据
 * @param {File} file CSV文件
 * @param {string} type 数据类型
 * @returns {Promise} API响应
 */
export const loadFromCSV = async (file, type) => {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('type', type);

  return await request(appendKbId('/main/loadFromCSV'), {
    method: 'POST',
    headers: {}, // FormData 不需要 Content-Type
    body: formData,
  });
};

// ==================== 知识抽取和冲突消解相关API ====================

/**
 * 仅提取文本节点与关系
 * @param {string} text 文本内容
 * @param {string} type 知识类型 (entity | event | model)
 * @param {string} token 可选的 workflow token
 * @returns {Promise} API响应
 */
export const extractFromText = async (text, type = 'entity', token = null) => {
  const url = token
    ? `/tool/extractFromText?token=${encodeURIComponent(token)}`
    : '/tool/extractFromText';
  return await request(url, {
    method: 'POST',
    body: JSON.stringify({ text, type }),
  });
};

/**
 * 仅提取图片节点
 * @param {File} file 图片文件
 * @param {string} type 知识类型 (entity | event | model)
 * @param {string} token 可选的 workflow token
 * @returns {Promise} API响应
 */
export const extractNodesFromImage = async (file, type = 'entity', token = null) => {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('type', type);

  const url = token
    ? `/tool/extractNodesFromImage?token=${encodeURIComponent(token)}`
    : '/tool/extractNodesFromImage';
  return await request(url, {
    method: 'POST',
    headers: {}, // FormData 不需要 Content-Type
    body: formData,
  });
};

/**
 * 仅提取
 * @param {File} file TXT文件
 * @param {string} type 知识类型 (entity | event | model)
 * @param {string} token 可选的 workflow token
 * @returns {Promise} API响应
 */
export const extractFromFile = async (file, type = 'entity', token = null) => {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('type', type);

  const url = token
    ? `/tool/extractFromFile?token=${encodeURIComponent(token)}`
    : '/tool/extractFromFile';
  return await request(url, {
    method: 'POST',
    headers: {}, // FormData 不需要 Content-Type
    body: formData,
  });
};

/**
 * 冲突消解
 * @param {Object} targetNode 目标节点对象
 * @param {string} token 可选的 workflow token
 * @returns {Promise} API响应
 */
export const resolveConflict = async (targetNode, token = null) => {
  const url = token
    ? `/tool/resolveConflict?token=${encodeURIComponent(token)}`
    : '/tool/resolveConflict';
  return await request(url, {
    method: 'POST',
    body: JSON.stringify({ target: JSON.stringify(targetNode) }),
  });
};

/**
 * 更新知识库文档
 * 后端使用默认配置，无需传递参数
 * @returns {Promise} API响应
 */
export const updateDocument = async () => {
  return await request('/tool/updateDocument', {
    method: 'POST',
  });
};

// ==================== 融合策略树相关API ====================

/**
 * 融合工作流 - 根据事件和类别进行融合处理，返回关系数组
 * @param {string} events JSON字符串数组格式的事件数据（事件对象数组序列化后的JSON字符串）
 * @param {string} categories JSON字符串数组格式的树知识名称数据（树名称字符串数组的JSON字符串）
 * @param {string} token 可选的 workflow token
 * @returns {Promise} API响应
 * @example
 * // events 格式: JSON.stringify([JSON.stringify(event1), JSON.stringify(event2)])
 * // categories 格式: JSON.stringify(["树名称1", "树名称2"])
 */
export const fusion = async (events, categories, token = '') => {
  const url = `/tool/fusion?token=${encodeURIComponent(token)}`;
  return await request(url, {
    method: 'POST',
    body: JSON.stringify({ events, categories }),
  });
};

// ==================== 图谱查询相关API ====================

/**
 * 查询图数据库节点的关系图谱
 * @param {string} name 节点名称
 * @param {number} type 查询类型：1-一跳关系，2-两跳关系，3-全部关系
 * @returns {Promise} API响应，包含nodes和relations数组
 */
export const queryGraph = async (name, type = 1) => {
  return await request(appendKbId(`/knowledgebase/queryGraph?name=${encodeURIComponent(name)}&type=${type}`));
};
