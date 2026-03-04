package com.xidian.kg.service;

import java.util.Map;

/**
 * 文件相关工具服务接口
 * 处理文档上传和索引状态检查
 */
public interface ToolWithFile {
    
    /**
     * 更新文档并检查索引状态
     * @param datasetId 数据集ID
     * @param documentId 文档ID
     * @param filePath 本地文件路径
     * @param token Authorization Bearer token
     * @return 处理结果，包含状态码和消息
     */
    Map<String, Object> updateDocumentAndCheckStatus(String datasetId, String documentId, String filePath, String token);
}
