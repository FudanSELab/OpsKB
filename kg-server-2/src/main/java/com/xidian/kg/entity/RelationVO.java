package com.xidian.kg.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class RelationVO implements Serializable {
    /**
     * 将关系的实体类，转换换成cypherSql需要字符串类型的vo
     * 在封装dao层的时候会用到
     */

    private static final long serialVersionUID = 1L;

    // 关系名称
    private String relationLabelName;

    // 开始标签名称
    private String startLabelName;

    // 开始节点属性
    private String startNodeProperties;

    // 关系属性
    private String relationProperties;

    // 结束节点条件
    private String endNodeProperties;

    // 结束标签名称
    private String endLabelName;

    // 查询层级
    private String level;

}
