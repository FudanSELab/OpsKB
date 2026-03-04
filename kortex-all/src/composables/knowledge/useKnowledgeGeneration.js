import { reactive, ref } from 'vue';
import * as knowledgeApi from '../../api/knowledge';

/**
 * 知识生成和冲突消解流程管理 Composable
 */
export function useKnowledgeGeneration() {
  const loading = ref(false);

  // 生成状态
  const generationState = reactive({
    // 模态框显示状态
    showGenerationModal: false,
    showGenerationLoadingModal: false, // 知识生成加载提示模态框
    showConflictLoadingModal: false, // 冲突消解加载提示模态框
    showGenerationResultModal: false,
    showConflictResultModal: false,

    // 生成的数据
    generatedNodes: [],

    // 冲突消解结果
    resolutionResults: [],

    // 冲突消解进度
    totalNodes: 0,
    processedNodes: 0,
    conflictResolutionPhase: 'retrieval', // 'retrieval' | 'analysis'

    // 是否可以进行冲突消解
    canResolveConflict: false,

    // 原始输入数据（用于冲突消解时的提示）
    originalInputType: null, // 'text', 'image', 'file'
    originalInputData: null,
  });

  /**
   * 打开知识生成弹框
   */
  const openGenerationModal = () => {
    generationState.showGenerationModal = true;
  };

  /**
   * 关闭知识生成弹框
   */
  const closeGenerationModal = () => {
    generationState.showGenerationModal = false;
  };

  /**
   * 重置生成状态
   */
  const resetGenerationState = () => {
    generationState.generatedNodes = [];
    generationState.resolutionResults = [];
    generationState.canResolveConflict = false;
    generationState.originalInputType = null;
    generationState.originalInputData = null;
    generationState.showGenerationLoadingModal = false;
    generationState.showConflictLoadingModal = false;
    generationState.totalNodes = 0;
    generationState.processedNodes = 0;
  };

  /**
   * 开始知识生成
   * @param {Object} data 生成数据 { type: 'text'|'image'|'file', content/file }
   */
  const startGeneration = async (data) => {
    try {
      loading.value = true;

      // 关闭生成弹框
      generationState.showGenerationModal = false;

      // 重置状态
      resetGenerationState();

      // 显示加载提示（在重置之后）
      generationState.showGenerationLoadingModal = true;

      // 保存原始输入
      generationState.originalInputType = data.type;
      generationState.originalInputData = data;

      let result;

      // 根据类型调用不同的接口
      if (data.type === 'text') {
        result = await knowledgeApi.extractFromText(data.content, data.knowledgeType);
      } else if (data.type === 'image') {
        // 图片提取只返回节点数组
        const response = await knowledgeApi.extractNodesFromImage(data.file, data.knowledgeType);
        if (Array.isArray(response)) {
          result = {
            code: 200,
            message: '提取成功',
            nodes: response,
            nodeCount: response.length,
          };
        } else {
          result = response;
        }
      } else if (data.type === 'file') {
        result = await knowledgeApi.extractFromFile(data.file, data.knowledgeType);
      }

      // 检查结果
      if (!result || (result.code && result.code !== 200)) {
        throw new Error(result?.message || '知识提取失败');
      }

      // 处理不同的返回格式，只保存节点
      if (data.type === 'file') {
        // 文件提取返回的格式
        generationState.generatedNodes = result.nodes || [];
      } else if (data.type === 'image') {
        // 图片提取只有节点
        generationState.generatedNodes = result.nodes || result || [];
      } else {
        // 文本提取返回的格式
        generationState.generatedNodes = result.nodes || [];
      }

      // 如果有节点生成，启用冲突消解按钮
      if (generationState.generatedNodes.length > 0) {
        generationState.canResolveConflict = true;
      }

      // 关闭加载提示
      generationState.showGenerationLoadingModal = false;

      // 显示生成结果
      generationState.showGenerationResultModal = true;

      return {
        success: true,
        nodeCount: generationState.generatedNodes.length,
      };
    } catch (error) {
      console.error('知识生成失败:', error);
      // 关闭加载提示
      generationState.showGenerationLoadingModal = false;
      throw error;
    } finally {
      loading.value = false;
    }
  };

  /**
   * 开始冲突消解
   */
  const startConflictResolution = async () => {
    try {
      loading.value = true;

      // 清空之前的消解结果
      generationState.resolutionResults = [];
      generationState.totalNodes = generationState.generatedNodes.length;
      generationState.processedNodes = 0;
      generationState.conflictResolutionPhase = 'retrieval'; // 设置为检索阶段

      // 显示冲突消解加载提示
      generationState.showConflictLoadingModal = true;

      // 1. 先更新知识库文档并等待索引完成（后端使用默认配置）
      console.log('开始更新知识库文档...');
      const updateResult = await knowledgeApi.updateDocument();

      // 检查更新结果
      if (!updateResult || updateResult.code !== 200) {
        throw new Error(updateResult?.message || '知识库检索失败');
      }

      // 检查索引状态
      if (updateResult.status !== 'completed') {
        throw new Error(`知识库索引未完成，状态: ${updateResult.status || '未知'}`);
      }

      console.log('知识库文档更新成功，索引已完成');

      // 切换到分析阶段
      generationState.conflictResolutionPhase = 'analysis';

      // 2. 对每个节点逐个调用冲突消解接口（串行处理以显示进度）
      const results = [];
      for (let i = 0; i < generationState.generatedNodes.length; i++) {
        const node = generationState.generatedNodes[i];
        try {
          const result = await knowledgeApi.resolveConflict(node);
          results.push(result);
        } catch (error) {
          console.error('节点冲突消解失败:', node, error);
          results.push({
            target: JSON.stringify(node),
            similar_target: null,
            judge: '错误',
            judge_reason: error.message || '消解失败',
            finalKg: null,
          });
        }
        // 更新进度
        generationState.processedNodes = i + 1;
      }

      generationState.resolutionResults = results;

      // 关闭加载提示
      generationState.showConflictLoadingModal = false;

      // 显示冲突消解结果
      generationState.showConflictResultModal = true;

      return {
        success: true,
        totalNodes: results.length,
        newNodes: results.filter((r) => r.judge === '否').length,
        mergeNodes: results.filter((r) => r.judge === '是').length,
      };
    } catch (error) {
      console.error('冲突消解失败:', error);
      // 关闭加载提示
      generationState.showConflictLoadingModal = false;
      throw error;
    } finally {
      loading.value = false;
    }
  };

  /**
   * 插入知识库
   * 将用户选择的节点插入到知识库
   * @param {Object} selectedData { nodes: [{judge, finalKg}] }
   */
  const insertToKnowledgeBase = async (selectedData) => {
    try {
      loading.value = true;

      const insertResults = {
        successNodes: 0,
        failedNodes: 0,
        newNodes: 0,
        mergedNodes: 0,
        errors: [],
      };

      // 处理选中的节点
      for (const nodeItem of selectedData.nodes) {
        try {
          const { judge, finalKg } = nodeItem;

          // 验证节点数据有效性
          if (!finalKg) {
            insertResults.failedNodes++;
            insertResults.errors.push(`节点数据无效: finalKg 为空`);
            continue;
          }

          if (!finalKg.properties) {
            insertResults.failedNodes++;
            insertResults.errors.push(`节点数据无效: 缺少 properties 属性`);
            continue;
          }

          const nodeName = finalKg.properties.name || '未知';

          if (judge === '否') {
            // judge='否' → 新建节点：直接插入
            const nodeData = {
              labels: finalKg.labels || [],
              properties: finalKg.properties || {},
            };

            const result = await knowledgeApi.createNode(nodeData);

            if (result.flag) {
              insertResults.successNodes++;
              insertResults.newNodes++;
            } else {
              insertResults.failedNodes++;
              insertResults.errors.push(`新建节点失败: ${nodeName} - ${result.data}`);
            }
          } else if (judge === '是') {
            // judge='是' → 合并节点：更新已有节点
            if (!finalKg.id) {
              insertResults.failedNodes++;
              insertResults.errors.push(`合并节点失败: ${nodeName} - 缺少节点ID`);
              continue;
            }

            const result = await knowledgeApi.updateNodePropertiesById(
              finalKg.id,
              finalKg.properties || {},
              finalKg.labels || null,
            );

            if (result.flag) {
              insertResults.successNodes++;
              insertResults.mergedNodes++;
            } else {
              insertResults.failedNodes++;
              insertResults.errors.push(
                `合并节点失败: ${nodeName} (ID: ${finalKg.id}) - ${result.data}`,
              );
            }
          } else {
            // 未知的 judge 类型
            insertResults.failedNodes++;
            insertResults.errors.push(`未知的操作类型: ${judge} - 节点 ${nodeName}`);
          }
        } catch (error) {
          insertResults.failedNodes++;
          insertResults.errors.push(`节点处理异常: ${error.message}`);
        }
      }

      // 关闭冲突消解结果弹框
      generationState.showConflictResultModal = false;

      // 重置状态
      resetGenerationState();

      return {
        success: true,
        ...insertResults,
      };
    } catch (error) {
      console.error('插入知识库失败:', error);
      throw error;
    } finally {
      loading.value = false;
    }
  };

  /**
   * 关闭生成结果弹框
   */
  const closeGenerationResult = () => {
    generationState.showGenerationResultModal = false;
  };

  /**
   * 取消冲突消解
   */
  const cancelConflictResolution = () => {
    generationState.showConflictResultModal = false;
    // 不重置状态，允许用户重新尝试
  };

  /**
   * 提示用户并清除数据
   * 当用户想要生成新知识但已有未消解的数据时调用
   */
  const confirmAndReset = () => {
    if (generationState.canResolveConflict) {
      return confirm('当前有未消解的知识数据，重新生成将清除这些数据。是否继续？');
    }
    return true;
  };

  return {
    loading,
    generationState,
    openGenerationModal,
    closeGenerationModal,
    startGeneration,
    startConflictResolution,
    insertToKnowledgeBase,
    closeGenerationResult,
    cancelConflictResolution,
    confirmAndReset,
    resetGenerationState,
  };
}
