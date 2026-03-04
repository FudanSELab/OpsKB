package com.xidian.kg.dao;

import com.xidian.kg.entity.BasicNode;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface NodeDao {

    /**
     * 获取知识库中所有的节点信息
     * @return
     */
    public List<BasicNode> getAllNodes();

    /**
     * 获取所有节点的标签名称
     */
    public Set<String> getAllLabelName();

    /**
     * 按条件查询节点
     * @param node
     * @param exactMatch 是否精确匹配（true表示全字符匹配，false或null表示模糊匹配）
     * @return
     */
    public List<BasicNode> queryNode(BasicNode node, Boolean exactMatch);

    /**
     * 创建节点
     * 注意： 不会判断要插入的节点在当前数据库中是否已经存在
     * @param node  节点
     * @return
     */
    public int createNode(BasicNode node);

    public int getNodeNum();
    /**
     * 获取实体类知识中所有大分类和小分类的名称以及对应关系
     * 封装返回的数据格式：
     * {
     *     大分类1：[对应的小分类列表],
     *     大分类2: [对应的小分类列表],
     *     ……
     * }
     */
    public Map<String, Set<String>> getEntityCategories();

    /**
     * 获取事件类知识中所有大分类和小分类的名称以及对应关系
     * 封装返回的数据格式：
     * {
     *     大分类1：[对应的小分类列表],
     *     大分类2: [对应的小分类列表],
     *     ……
     * }
     */
    public Map<String, Set<String>>  getEventCategories();

    /**
     * 获取模型类知识中所有大分类和小分类的名称以及对应关系
     * 封装返回的数据格式：
     * {
     *     大分类1：[对应的小分类列表],
     *     大分类2: [对应的小分类列表],
     *     ……
     * }
     */
    public Map<String, Set<String>>  getModelCategories();

    /**
     * 获取策略树类知识中所有大分类和小分类的名称以及对应关系
     * 封装返回的数据格式：
     * {
     *     大分类1：[对应的小分类列表],
     *     大分类2: [对应的小分类列表],
     *     ……
     * }
     */
    public Map<String, Set<String>>  getTreeCategories();

    /**
     * 给知识库中的节点添加属性键值对
     * @param nodeName 要添加属性对的节点名称
     * @param prop 要添加的属性键值对
     * @return
     */
    public int addPropertyToNode(String nodeName, Map<String, String> prop);

    /**
     * 删除知识库中某个节点的属性
     * @param nodeName 要删除属性的节点名称
     * @param prop 要删除的属性字段名
     * @return 删除的属性个数
     */
    public int removePropertyFromNode(String nodeName, List<String> prop);

    /**
     * 设置节点的属性值。
     * @param nodeName 要设置属性的节点名称
     * @param properties 要给节点设置的属性键值对
     * @return 该节点设置的属性条数
     */
    public int setNodeProperty(String nodeName, Map<String, Object> properties);

    /**
     * 根据传入的类别列表获取每个类别的节点个数
     * @param category 类别列表，类别指的是大类别，即知识库中的标签
     * @return 传入的
     */
    public Map<String,Long> getCountByCategory(List<String> category);


    public Map<String,Long> getCountByCategorySph();
    /**
     * 通过名称查询节点
     * @param node
     * @return
     */
    public List<BasicNode> queryNodeByName(BasicNode node);

    /**
     * 删除节点和相关关系
     * @param node        节点
     * @return
     */
    public Integer delNode(BasicNode node);

    /**
     * 从json文件批量导入节点
     * @param filePath
     * @return
     */
    public Long loadNodeFromJson(String filePath);

    /**
     * 根据节点ID更新节点属性（完全替换模式）
     * 注意：此方法会完全替换节点的所有属性，未提供的属性将被删除
     * @param nodeId 节点ID
     * @param properties 要设置的属性键值对，必须包含name属性
     * @return 设置的属性条数
     */
    public int updateNodePropertiesById(Long nodeId, Map<String, Object> properties,List<String> labels);

    /**
     * 根据节点名称和类型查询图数据
     * @param name 节点名称
     * @param type 查询类型：1-一跳关系，2-两跳关系，3-全部关系
     * @return 包含节点列表和关系列表的Map
     */
    public Map<String, Object> queryGraphByNameAndType(String name, Integer type);

}
