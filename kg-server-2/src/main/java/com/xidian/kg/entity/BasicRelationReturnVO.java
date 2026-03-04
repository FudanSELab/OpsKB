package com.xidian.kg.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class BasicRelationReturnVO implements Serializable {
    /**
     * 将关系封装，与前端交互的数据类型封装
     */
    private static final long serialVersionUID = 1L;

    // 关系开始节点
    private BasicNode start;

    // 关系结束节点
    private BasicNode end;

    // 关系
    private QueryRelation relation;
}
