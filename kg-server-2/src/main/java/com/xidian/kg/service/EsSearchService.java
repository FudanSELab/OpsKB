package com.xidian.kg.service;

import com.xidian.kg.controller.util.Result;

/**
 * 基于 Elasticsearch 的搜索服务
 * 该服务与现有 Neo4j 服务并存，前端可单独调用以从 ES 中检索数据。
 */
public interface EsSearchService {

    /**
     * 按关键字搜索节点名称及属性
     *
     * @param keyword 关键字
     * @return Result 包含 ES 命中的文档列表
     */
    Result searchByKeyword(String keyword);
}

