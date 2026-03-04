/**
 * 决策相关的API接口
 */

// const API_BASE_URL = 'http://115.190.140.186:8053/api';
const API_BASE_URL = 'http://127.0.0.1:8080/api';

/**
 * 获取决策树结构
 * @param {number} scenarioId 场景ID
 * @param {string} treeVersion 树版本号
 * @returns {Promise} API响应
 */
export const getTree = async (scenarioId, treeVersion) => {
  try {
    const response = await fetch(`${API_BASE_URL}/tree/getTree`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        scenarioId,
        treeVersion,
      }),
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    return await response.json();
  } catch (error) {
    console.error('获取决策树结构失败:', error);
    throw error;
  }
};

/**
 * 获取样本列表（分页）
 * @param {number} scenarioId 场景ID
 * @param {string} treeVersion 树版本号
 * @param {number} current 当前页号，默认为1
 * @param {number} pageSize 每页大小，默认为10
 * @returns {Promise} API响应
 */
export const getSampleList = async (scenarioId, treeVersion, current = 1, pageSize = 10) => {
  try {
    const response = await fetch(`${API_BASE_URL}/tree/getSampleList`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        scenarioId,
        treeVersion,
        current,
        pageSize,
      }),
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    return await response.json();
  } catch (error) {
    console.error('获取样本列表失败:', error);
    throw error;
  }
};

/**
 * 根据ID获取样本详情
 * @param {number} sampleId 样本ID
 * @returns {Promise} API响应
 */
export const getSampleById = async (sampleId) => {
  try {
    const response = await fetch(`${API_BASE_URL}/tree/getSampleById?sampleId=${sampleId}`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    return await response.json();
  } catch (error) {
    console.error('获取样本详情失败:', error);
    throw error;
  }
};

/**
 * 获取实战样本列表（分页）
 * @param {number} scenarioId 场景ID
 * @param {string} treeVersion 树版本号
 * @param {number} current 当前页号，默认为1
 * @param {number} pageSize 每页大小，默认为10
 * @returns {Promise} API响应
 */
export const getFactSampleList = async (scenarioId, treeVersion, current = 1, pageSize = 10) => {
  try {
    const response = await fetch(`${API_BASE_URL}/fact/getSampleList`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        scenarioId,
        treeVersion,
        current,
        pageSize,
      }),
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    return await response.json();
  } catch (error) {
    console.error('获取实战样本列表失败:', error);
    throw error;
  }
};

/**
 * 根据ID获取实战样本详情
 * @param {number} sampleId 样本ID
 * @returns {Promise} API响应
 */
export const getFactSampleById = async (sampleId) => {
  try {
    const response = await fetch(`${API_BASE_URL}/fact/getSampleById?sampleId=${sampleId}`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    return await response.json();
  } catch (error) {
    console.error('获取实战样本详情失败:', error);
    throw error;
  }
};

/**
 * 获取所有场景及树版本列表
 * @returns {Promise} API响应
 */
export const getScenarios = async () => {
  try {
    const response = await fetch(`${API_BASE_URL}/tree/getScenarios`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    return await response.json();
  } catch (error) {
    console.error('获取场景列表失败:', error);
    throw error;
  }
};

/**
 * 开始训练
 * @param {number} scenarioId 场景ID
 * @param {string} treeVersion 树版本号
 * @param {number} batchSize 批次大小（可选）
 * @returns {Promise} API响应
 */
export const startTraining = async (scenarioId, treeVersion, batchSize) => {
  try {
    let url = `${API_BASE_URL}/AutoConduct/startTraining?scenarioId=${scenarioId}&treeVersion=${treeVersion}`;
    if (batchSize) {
      url += `&batchSize=${batchSize}`;
    }

    const response = await fetch(url, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
      },
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    return await response.json();
  } catch (error) {
    console.error('开始训练失败:', error);
    throw error;
  }
};

/**
 * 获取训练状态
 * @param {number} scenarioId 场景ID
 * @returns {Promise} API响应
 */
export const getTrainingStatus = async (scenarioId) => {
  try {
    const response = await fetch(
      `${API_BASE_URL}/AutoConduct/trainingStatus?scenarioId=${scenarioId}`,
      {
        method: 'GET',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
        },
      },
    );

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    return await response.json();
  } catch (error) {
    console.error('获取训练状态失败:', error);
    throw error;
  }
};

/**
 * 停止训练
 * @param {number} scenarioId 场景ID
 * @returns {Promise} API响应
 */
export const stopTraining = async (scenarioId) => {
  try {
    const response = await fetch(
      `${API_BASE_URL}/AutoConduct/stopTraining?scenarioId=${scenarioId}`,
      {
        method: 'GET',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
        },
      },
    );

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    return await response.json();
  } catch (error) {
    console.error('停止训练失败:', error);
    throw error;
  }
};

/**
 * 策略搜索 - 根据事件ID和策略树版本，使用决策树定位候选策略集合
 * @param {number} eventId 事件ID
 * @param {string} treeVersion 策略树版本号
 * @returns {Promise} API响应
 */
export const strategyLocation = async (eventId, treeVersion) => {
  try {
    const response = await fetch(`${API_BASE_URL}/core/location`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        eventId,
        treeVersion,
      }),
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    return await response.json();
  } catch (error) {
    console.error('策略搜索失败:', error);
    throw error;
  }
};

/**
 * UCB策略选择 - 从候选策略中使用UCB算法选择最优策略ID集合
 * @param {number} eventId 事件ID
 * @param {string} treeVersion 策略树版本号
 * @returns {Promise} API响应
 */
export const ucbSelect = async (eventId, treeVersion) => {
  try {
    const response = await fetch(`${API_BASE_URL}/core/ucbSelect`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        eventId,
        treeVersion,
      }),
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    return await response.json();
  } catch (error) {
    console.error('UCB策略选择失败:', error);
    throw error;
  }
};

/**
 * 生成方案集合 - 根据事件和选中的策略ID生成具体的处置方案集合
 * @param {number} eventId 事件ID
 * @param {string} treeVersion 策略树版本号
 * @returns {Promise} API响应
 */
export const planGenerate = async (eventId, treeVersion) => {
  try {
    const response = await fetch(`${API_BASE_URL}/core/planGenerate`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        eventId,
        treeVersion,
      }),
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    return await response.json();
  } catch (error) {
    console.error('生成方案集合失败:', error);
    throw error;
  }
};

/**
 * 单次训练（策略优选执行） - 执行完整的单次训练流程
 * @param {number} eventId 事件ID
 * @param {string} treeVersion 策略树版本号
 * @returns {Promise} API响应
 */
export const trainOnce = async (eventId, treeVersion) => {
  try {
    const response = await fetch(`${API_BASE_URL}/core/trainOnce`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        eventId,
        treeVersion,
      }),
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    return await response.json();
  } catch (error) {
    console.error('单次训练失败:', error);
    throw error;
  }
};

/**
 * 导入外部策略树
 * @returns {Promise} API响应
 */
export const importExternalTree = async () => {
  try {
    const response = await fetch(`${API_BASE_URL}/tree/importExternalTree`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    return await response.json();
  } catch (error) {
    console.error('导入外部策略树失败:', error);
    throw error;
  }
};

/**
 * 通用错误处理函数
 * @param {Error} error 错误对象
 * @param {string} operation 操作名称
 */
export const handleApiError = (error, operation) => {
  console.error(`${operation}失败:`, error);

  // 可以根据具体的错误类型进行不同的处理
  if (error.name === 'TypeError' && error.message.includes('fetch')) {
    return {
      code: 0,
      message: '网络连接失败，请检查网络设置',
      data: null,
    };
  }

  if (error.message.includes('404')) {
    return {
      code: 404,
      message: '请求的资源不存在',
      data: null,
    };
  }

  if (error.message.includes('500')) {
    return {
      code: 500,
      message: '服务器内部错误',
      data: null,
    };
  }

  return {
    code: -1,
    message: error.message || '未知错误',
    data: null,
  };
};
