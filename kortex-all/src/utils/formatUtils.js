/**
 * 格式化条件对象，过滤掉 null 值并转为字符串
 */
export const formatConditions = (conditions) => {
  if (!conditions) return '';

  if (conditions.type && conditions.value !== undefined && conditions.value !== null) {
    return `${conditions.type}: ${conditions.value}`;
  }

  const nonNullEntries = Object.entries(conditions)
    .filter(([key, value]) => value !== null && value !== undefined)
    .map(([key, value]) => {
      const valueStr = Array.isArray(value) ? `[${value.join(', ')}]` : String(value);
      return `${key}: ${valueStr}`;
    });

  return nonNullEntries.join('\n');
};
