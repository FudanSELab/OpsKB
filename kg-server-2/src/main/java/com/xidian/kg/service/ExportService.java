package com.xidian.kg.service;

/**
 * Neo4j数据导出服务接口
 * 用于在节点或关系发生变化时导出数据
 */
public interface ExportService {
    
    /**
     * 执行Neo4j数据导出
     * 调用APOC插件的export.json.all方法导出所有数据到data.txt文件
     */
    void exportData();
}
