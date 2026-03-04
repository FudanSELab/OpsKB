package com.xidian.kg.service;

import com.xidian.kg.controller.util.Result;
import com.xidian.kg.entity.BasicNode;
import com.xidian.kg.entity.BasicRelationReturnVO;

public interface RelationService {

    /**
     * 新建关系
     * @param basicRelationReturnVO
     * @return
     */
    public Result createRelation(BasicRelationReturnVO basicRelationReturnVO);

    /**
     * 删除关系
     * @param basicRelationReturnVO
     * @return
     */
    public Result deleteRelation(BasicRelationReturnVO basicRelationReturnVO);

    /**
     * 获取某一节点的所有关系
     * @param basicNode
     * @return
     */
    public Result getNodeRelation(BasicNode basicNode);

    /**
     * 修改关系属性值
     * @param basicRelationReturnVO
     * @return
     */
    public Result updateRelationProperties(BasicRelationReturnVO basicRelationReturnVO);
}
