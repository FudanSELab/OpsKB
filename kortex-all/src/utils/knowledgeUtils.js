/**
 * 高亮搜索关键词
 */
export const highlightSearchTerm = (text, searchTerm) => {
  if (!searchTerm || !text) return text;

  const regex = new RegExp(`(${searchTerm.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')})`, 'gi');
  return text.replace(regex, '<mark class="bg-yellow-200 text-yellow-900 px-1 rounded">$1</mark>');
};

/**
 * 格式化节点名称
 */
export const formatNodeName = (node) => {
  return node?.properties?.name || node?.id || '未知节点';
};

/**
 * 格式化标签列表
 */
export const formatLabels = (labels) => {
  if (!labels || !Array.isArray(labels)) return '';
  return labels.join(' · ');
};
