package com.xidian.kg.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;
import java.util.Map;

/**
 * Elasticsearch 中与 Neo4j 节点对应的文档结构
 * 保持与 BasicNode 大体一致，方便前端同时兼容两种后端数据源。
 */
@Data
@NoArgsConstructor
@Document(indexName = "kb_nodes")
public class EsNodeDocument {

    /**
     * ES 文档主键，通常使用 Neo4j 节点 ID 或业务唯一 ID
     */
    @Id
    private String id;

    /**
     * 节点的名称，作为主要检索字段
     */
    @Field(type = FieldType.Text)
    private String name;

    /**
     * 节点标签，与 Neo4j labels 对应
     */
    @Field(type = FieldType.Keyword)
    private List<String> labels;

    /**
     * 其他属性，使用对象存储，便于灵活扩展
     */
    @Field(type = FieldType.Object)
    private Map<String, Object> properties;
}

