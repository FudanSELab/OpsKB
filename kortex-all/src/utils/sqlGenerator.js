/**
 * SQL语句生成工具
 */

/**
 * 转义SQL字符串
 */
const escapeSQLString = (str) => {
  if (str === null || str === undefined) return 'NULL';
  return `'${String(str).replace(/'/g, "''")}'`;
};

/**
 * 转义JSON字段
 */
const escapeSQLJson = (obj) => {
  if (obj === null || obj === undefined) return 'NULL';
  const jsonStr = JSON.stringify(obj);
  return `'${jsonStr.replace(/'/g, "''")}'`;
};

/**
 * 生成INSERT语句
 */
const generateInsertSQL = (node, treeVersion, scenarioId) => {
  const fields = [];
  const values = [];

  // 必填字段
  fields.push('tree_id');
  values.push(node.treeId);

  fields.push('node_type');
  values.push(escapeSQLString(node.nodeType));

  fields.push('parent_id');
  values.push(node.parentId !== null ? node.parentId : 'NULL');

  fields.push('child_ids');
  values.push(escapeSQLJson(node.childIds || []));

  fields.push('tree_version');
  values.push(escapeSQLString(treeVersion));

  fields.push('scenario_id');
  values.push(scenarioId);

  // 可选字段
  fields.push('conditions');
  values.push(escapeSQLJson(node.conditions));

  fields.push('strategy_id');
  values.push(node.strategyId !== null && node.strategyId !== undefined ? node.strategyId : 'NULL');

  const sql = `INSERT INTO strategy_trees (${fields.join(', ')}) VALUES (${values.join(', ')});`;
  return sql;
};

/**
 * 生成UPDATE语句
 */
const generateUpdateSQL = (node, treeVersion, scenarioId) => {
  const updates = [];

  updates.push(`node_type = ${escapeSQLString(node.nodeType)}`);
  updates.push(`parent_id = ${node.parentId !== null ? node.parentId : 'NULL'}`);
  updates.push(`child_ids = ${escapeSQLJson(node.childIds || [])}`);
  updates.push(`conditions = ${escapeSQLJson(node.conditions)}`);
  updates.push(
    `strategy_id = ${node.strategyId !== null && node.strategyId !== undefined ? node.strategyId : 'NULL'}`,
  );
  updates.push(`tree_version = ${escapeSQLString(treeVersion)}`);
  updates.push(`scenario_id = ${scenarioId}`);

  const sql = `UPDATE strategy_trees SET ${updates.join(', ')} WHERE tree_id = ${node.treeId} AND tree_version = ${escapeSQLString(treeVersion)} AND scenario_id = ${scenarioId};`;
  return sql;
};

/**
 * 生成DELETE语句
 */
const generateDeleteSQL = (treeId, treeVersion, scenarioId) => {
  return `DELETE FROM strategy_trees WHERE tree_id = ${treeId} AND tree_version = ${escapeSQLString(treeVersion)} AND scenario_id = ${scenarioId};`;
};

/**
 * 比较两个节点是否相同
 */
const isNodeEqual = (node1, node2) => {
  if (!node1 || !node2) return false;

  return (
    node1.nodeType === node2.nodeType &&
    node1.parentId === node2.parentId &&
    JSON.stringify(node1.childIds) === JSON.stringify(node2.childIds) &&
    JSON.stringify(node1.conditions) === JSON.stringify(node2.conditions) &&
    node1.strategyId === node2.strategyId
  );
};

/**
 * 生成SQL语句集合
 * @param {Array} originalData 原始数据
 * @param {Array} currentData 当前数据
 * @param {string} treeVersion 树版本
 * @param {number} scenarioId 场景ID
 * @returns {Array} SQL语句数组
 */
export const generateSQLStatements = (originalData, currentData, treeVersion, scenarioId) => {
  const statements = [];

  // 如果没有原始数据，说明是全新创建
  if (!originalData || originalData.length === 0) {
    currentData.forEach((node) => {
      statements.push(generateInsertSQL(node, treeVersion, scenarioId));
    });
    return statements;
  }

  // 创建节点映射
  const originalMap = new Map();
  originalData.forEach((node) => {
    originalMap.set(node.treeId, node);
  });

  const currentMap = new Map();
  currentData.forEach((node) => {
    currentMap.set(node.treeId, node);
  });

  // 检查删除的节点
  originalMap.forEach((originalNode, treeId) => {
    if (!currentMap.has(treeId)) {
      statements.push(generateDeleteSQL(treeId, treeVersion, scenarioId));
    }
  });

  // 检查新增和修改的节点
  currentMap.forEach((currentNode, treeId) => {
    const originalNode = originalMap.get(treeId);

    if (!originalNode) {
      // 新增节点
      statements.push(generateInsertSQL(currentNode, treeVersion, scenarioId));
    } else if (!isNodeEqual(originalNode, currentNode)) {
      // 修改节点
      statements.push(generateUpdateSQL(currentNode, treeVersion, scenarioId));
    }
  });

  return statements;
};

/**
 * 生成完整的批量插入SQL（用于全新树）
 */
export const generateBatchInsertSQL = (treeData, treeVersion, scenarioId) => {
  if (!treeData || treeData.length === 0) {
    return [];
  }

  const statements = [];

  // 添加清理语句（可选）
  statements.push(`-- 清理旧数据`);
  statements.push(
    `DELETE FROM strategy_trees WHERE tree_version = ${escapeSQLString(treeVersion)} AND scenario_id = ${scenarioId};`,
  );
  statements.push('');

  // 添加所有节点
  statements.push(`-- 插入新数据`);
  treeData.forEach((node) => {
    statements.push(generateInsertSQL(node, treeVersion, scenarioId));
  });

  return statements;
};

/**
 * 导出单个节点的SQL（用于调试）
 */
export const generateNodeSQL = (node, operation, treeVersion, scenarioId) => {
  switch (operation) {
    case 'insert':
      return generateInsertSQL(node, treeVersion, scenarioId);
    case 'update':
      return generateUpdateSQL(node, treeVersion, scenarioId);
    case 'delete':
      return generateDeleteSQL(node.treeId, treeVersion, scenarioId);
    default:
      throw new Error(`Unknown operation: ${operation}`);
  }
};
