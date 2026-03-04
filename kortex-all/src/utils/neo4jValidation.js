/**
 * Neo4j 属性键名验证工具
 */

// Neo4j 保留字（部分常用的）
const RESERVED_WORDS = [
  'all',
  'and',
  'as',
  'asc',
  'ascending',
  'by',
  'call',
  'case',
  'contains',
  'create',
  'delete',
  'descending',
  'detach',
  'distinct',
  'else',
  'end',
  'exists',
  'false',
  'foreach',
  'in',
  'is',
  'limit',
  'match',
  'merge',
  'not',
  'null',
  'on',
  'optional',
  'or',
  'order',
  'remove',
  'return',
  'set',
  'skip',
  'then',
  'true',
  'union',
  'unwind',
  'when',
  'where',
  'with',
  'xor',
  'yield',
];

/**
 * 验证 Neo4j 属性键名是否有效（黑名单模式）
 * @param {string} key 属性键名
 * @returns {Object} { valid: boolean, error: string }
 */
export function validatePropertyKey(key) {
  // 去除首尾空格
  const trimmedKey = key.trim();

  // 不能为空
  if (!trimmedKey) {
    return { valid: false, error: '属性名不能为空' };
  }

  // 不能以数字开头
  if (/^\d/.test(trimmedKey)) {
    return { valid: false, error: '属性名不能以数字开头' };
  }

  // 不能是保留字（不区分大小写）- 仅对英文属性名检查
  if (RESERVED_WORDS.includes(trimmedKey.toLowerCase())) {
    return { valid: false, error: `"${trimmedKey}" 是 Neo4j 保留字，不能作为属性名` };
  }

  return { valid: true, error: '' };
}

/**
 * 批量验证属性键名
 * @param {Array} properties 属性数组 [{key, value}]
 * @returns {Object} { valid: boolean, errors: Object }
 */
export function validateProperties(properties) {
  const errors = {};
  let hasError = false;

  const keyCount = {};

  properties.forEach((prop, index) => {
    const key = prop.key.trim();

    // 验证单个键名
    const validation = validatePropertyKey(key);
    if (!validation.valid) {
      errors[prop.id] = validation.error;
      hasError = true;
      return;
    }

    // 检查重复的键名
    if (keyCount[key]) {
      errors[prop.id] = `属性名 "${key}" 重复`;
      hasError = true;
    } else {
      keyCount[key] = true;
    }
  });

  return { valid: !hasError, errors };
}
