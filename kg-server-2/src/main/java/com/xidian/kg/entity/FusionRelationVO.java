package com.xidian.kg.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * Fusion工作流返回的关系实体VO
 */
@Data
public class FusionRelationVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 关系类型（如：聚类、融合、调用）
     */
    private String type;
    
    /**
     * 关系属性
     */
    private Map<String, Object> properties;
    
    /**
     * 起始节点名称
     */
    private String start;
    
    /**
     * 结束节点名称
     */
    private String end;
}

