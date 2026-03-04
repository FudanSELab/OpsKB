/**
 * 事件生成相关的API接口
 */

// const API_BASE_URL = 'http://115.190.140.186:8053/api';
const API_BASE_URL = 'http://127.0.0.1:8080/api';

/**
 * 生成事件数据
 * @param {Object} configData 配置数据
 * @returns {Promise} API响应
 */
export const generateEvents = async (configData) => {
  try {
    // 转换配置数据为API要求的格式
    const requestPayload = transformConfigToPayload(configData);

    const response = await fetch(`${API_BASE_URL}/events/generate`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(requestPayload),
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    return await response.json();
  } catch (error) {
    console.error('生成事件数据失败:', error);
    throw error;
  }
};

/**
 * 将页面配置数据转换为API要求的格式
 * @param {Object} configData 页面配置数据
 * @returns {Object} API请求参数
 */
const transformConfigToPayload = (configData) => {
  // 转换防区数据
  const zones = configData.zones.map((zone) => ({
    id: zone.id,
    center: {
      longitude: parseFloat(zone.center.longitude),
      latitude: parseFloat(zone.center.latitude),
    },
  }));

  // 转换资源数据
  const resources = configData.resources.map((resource, index) => ({
    id: resource.id,
    type: resource.type,
    position: {
      longitude: parseFloat(resource.position.longitude),
      latitude: parseFloat(resource.position.latitude),
    },
    zoneIndex: 0, // 默认为0，可根据实际需求调整
    cost: 0, // 默认为0，可根据实际需求调整
    successRate: 0, // 默认为0，可根据实际需求调整
    attributes: resource.params ? JSON.stringify(resource.params) : '',
  }));

  // 构建请求参数
  const payload = {
    scenarioId: 1, // 默认场景ID，可根据需求调整
    event: 'a', // 事件描述，可根据需求调整
    zones: zones,
    resources: resources,
    targetConfig: configData.target_config || '',
    environment: {
      visibility: {
        low: parseFloat(configData.environment.visibility.low) || 0,
        medium: parseFloat(configData.environment.visibility.medium) || 0,
        high: parseFloat(configData.environment.visibility.high) || 0,
      },
    },
    generation: {
      mode: configData.generation.mode,
      interval:
        configData.generation.mode === 'interval'
          ? {
              intervalSeconds: parseInt(configData.generation.interval.interval_seconds) || 0,
              perIntervalCount: parseInt(configData.generation.interval.per_interval_count) || 0,
            }
          : {
              intervalSeconds: 0,
              perIntervalCount: 0,
            },
      count:
        configData.generation.mode === 'by_count'
          ? {
              total: parseInt(configData.generation.count.total) || 0,
            }
          : {
              total: 0,
            },
    },
    params: {
      alpha: 5, // 默认参数，可根据需求调整
      gamma: 3, // 默认参数，可根据需求调整
      kintensity: 2, // 默认参数，可根据需求调整
    },
  };

  return payload;
};

/**
 * 获取事件列表
 * @param {number} current 当前页码，默认1
 * @param {number} size 每页数量，默认10
 * @returns {Promise} API响应
 */
export const getEventList = async (current = 1, size = 20) => {
  try {
    const response = await fetch(`${API_BASE_URL}/events?page=${current}&size=${size}`, {
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
    console.error('获取事件列表失败:', error);
    throw error;
  }
};

/**
 * 获取事件详情
 * @param {number|string} eventId 事件ID
 * @returns {Promise} API响应
 */
export const getEventDetail = async (eventId) => {
  try {
    const response = await fetch(`${API_BASE_URL}/events/${eventId}`, {
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
    console.error('获取事件详情失败:', error);
    throw error;
  }
};

/**
 * 获取资源列表（如果有相关接口）
 * @returns {Promise} API响应
 */
export const getResources = async () => {
  try {
    const response = await fetch(`${API_BASE_URL}/resources`, {
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
    console.error('获取资源列表失败:', error);
    throw error;
  }
};
