package com.xidian.kg.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class BasicNode implements Serializable {
    /**
     * 节点实体类
     */

    private static final long serialVersionUID = 1L;
    // 节点id值
    private Long id;

    // 节点标签组成列表
    private List<String> labels;

    // 节点属性组成hashmap集合
    private Map<String, Object> properties;
}
