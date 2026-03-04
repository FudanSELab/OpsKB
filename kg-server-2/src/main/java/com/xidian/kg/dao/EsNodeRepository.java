package com.xidian.kg.dao;

import com.xidian.kg.entity.EsNodeDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

/**
 * Elasticsearch 节点文档仓库
 * 与 Neo4j 的 NodeDao 并存，前端可通过不同接口分别访问。
 */
public interface EsNodeRepository extends ElasticsearchRepository<EsNodeDocument, String> {

    /**
     * 根据名称进行全文检索（默认分词）
     */
    List<EsNodeDocument> findByNameContaining(String keyword);
}

