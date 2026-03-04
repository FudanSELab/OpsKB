/**
 * 策略ID与名称的映射配置
 */
export const strategyNameMap = {
  10001: '距离优先',
  10002: '欺骗+距离优先',
  10003: '干扰+距离优先',
  10004: '捕获+距离优先',
  10005: '摧毁+距离优先',
  10006: '遗传算法',
};

/**
 * 根据策略ID获取策略名称
 * @param {number|string} strategyId - 策略ID
 * @returns {string} 策略名称，如果未找到则返回 "策略{ID}"
 */
export function getStrategyName(strategyId) {
  return strategyNameMap[strategyId] || `策略${strategyId}`;
}

/**
 * 获取所有策略列表
 * @returns {Array} 策略列表 [{id, name}]
 */
export function getAllStrategies() {
  return Object.entries(strategyNameMap).map(([id, name]) => ({
    id: parseInt(id),
    name,
  }));
}
