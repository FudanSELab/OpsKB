package com.xidian.kg.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
public class QueryRelation implements Serializable {
    /**
     * 查询关系的时候返回的对象封装的实体类
     */
    private static final long serialVersionUID = 1L;


    // 开始节点id
    private Long start;

    // 结束节点id
    private Long end;

    // 关系类型
    private String type;

    // id
    private Long id;

    // 标签属性
    private Map<String, Object> properties;
}
