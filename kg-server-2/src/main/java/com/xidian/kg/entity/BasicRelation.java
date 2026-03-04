package com.xidian.kg.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Data
public class BasicRelation implements Serializable {
    /**
     * 关系实体类
     */

    private static final long serialVersionUID = 1L;
    // 关系id值
    private Long id;

    // 关系类型
    private String type;

    // 关系属性
    private Map<String, Object> properties;
}
