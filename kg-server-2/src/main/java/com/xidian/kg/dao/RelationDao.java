package com.xidian.kg.dao;

import com.xidian.kg.entity.BasicNode;
import com.xidian.kg.entity.BasicRelationReturnVO;

import java.util.List;
import java.util.Set;

public interface RelationDao {

    /**
     * 获取知识库中所有关系
     * @return
     */
    public List<BasicRelationReturnVO> getAllRelations();

    /**
     * 获取所有的关系名称
     * @return
     */
    public Set<String> getAllRelationType();

    /**
     * 查询关系
     * @param basicRelationReturnVO
     * @return
     */
    public Integer queryRelation(BasicRelationReturnVO basicRelationReturnVO);

    /**
     * 创建新关系
     * @param basicRelationReturnVO
     * @return
     */
    public Integer createRelation(BasicRelationReturnVO basicRelationReturnVO);

    /**
     * 删除关系
     * @param basicRelationReturnVO
     * @return
     */
    public Integer deleteRelation(BasicRelationReturnVO basicRelationReturnVO);

    /**
     * 获取某一节点的所有正向关系
     * @return
     */
    public List<BasicRelationReturnVO> getNodeRelationForward(BasicNode basicNode);

    /**
     * 获取某一节点的所有反向关系
     * @param basicNode
     * @return
     */
    public List<BasicRelationReturnVO> getNodeRelationOpposite(BasicNode basicNode);

    /**
     * 修改关系的属性值
     * @param basicRelationReturnVO
     * @return
     */
    public Integer updateRelationProperties(BasicRelationReturnVO basicRelationReturnVO);

    /**
     * 从json文件中加载关系,写入知识库
     * @param filePath : json文件路径
     * @return 写入知识库的知识个数
     */
    public Long loadRelationFromJson(String filePath);
}
