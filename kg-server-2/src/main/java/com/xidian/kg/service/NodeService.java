package com.xidian.kg.service;

import com.xidian.kg.controller.util.Result;
import com.xidian.kg.entity.BasicNode;
import java.util.List;
import java.util.Map;

public interface NodeService {

    /**
     * 根据类别查询节点
     * @param  category_main: 大类别名称，即实体类、事件类、模型类
     * @param category_detail： 小类别名称，即传感探测类资源知识，情报处理类资源知识等等
     * @return 该类别下的所有节点信息（json字符串格式）
     */
    public Result queryNodeByCategory(String category_main,String category_detail);

    /**
     * 新建一个节点（不判断是否重复）
     */
    public Result createNode(BasicNode node);

    /**
     * 查询实体类中所有的类别和细分类别及对应关系
     * @return
     */
    public Result queryEntityCategories();

    /**
     * 查询事件类中所有的类别和细分类别及对应关系
     * @return
     */
    public Result queryEventCategories();

    /**
     * 查询模型类中所有的类别和细分类别及对应关系
     * @return
     */
    public Result queryModelCategories();

    /**
     * 查询策略树类中所有的类别和细分类别及对应关系
     * @return
     */
    public Result queryTreeCategories();

    /**
     * 根据节点名称查询节点
     * @param name: 节点名称
     * @param exactMatch: 是否精确匹配（true表示全字符匹配，false或null表示模糊匹配）
     * @return 封装的Result对象
     */
    public Result queryNodeByName(String name, Boolean exactMatch);

    /**
     * 向知识库中的节点添加属性
     * @param nodeName 要添加属性的节点名称
     * @param properties 要添加的属性键值对
     * @return 是否添加成功封装的Result对象
     */
    public Result addPropertiesToNode(String nodeName, Map<String,String> properties);

    /**
     * 删除知识库中某个节点的属性
     * @param nodeName 要删除属性的节点名称
     * @param properties 要删除的属性字段名
     * @return 是否删除成功封装的Result对象
     */
    public Result removePropertiesFromNode(String nodeName, List<String> properties);

    /**
     * 设置知识库中节点的属性值
     * 注意：如果属性不存在则自动创建，如果存在则修改其值
     * @param nodeName 要设置属性值的节点名称
     * @param properties 要设置的属性键值对
     * @return 是否设置成功包装的Result字符串对象
     */
    public Result setNodeProperty(String nodeName, Map<String,Object> properties);

    /**
     * 获得实体类中每个类别的节点个数
     * @return 实体类下所有类别和节点个数键值对组成的map集合，封装后的Result对象字符串
     */
    public Result getEntityCountByCategory();

    /**
     * 获得事件类中每个类别的节点个数
     * @return 事件类下所有类别和节点个数键值对组成的map集合，封装后的Result对象字符串
     */
    public Result getEventCountByCategory();

    /**
     * 获得模型类中每个类别的节点个数
     * @return 模型类下所有类别和节点个数键值对组成的map集合，封装后的Result对象字符串
     */
    public Result getModelCountByCategory();

    /**
     * 获得策略树类中每个类别的节点个数
     * @return 策略树类下所有类别和节点个数键值对组成的map集合，封装后的Result对象字符串
     */
    public Result getTreeCountByCategory();

    /**
     * 删除一个节点
     */
    public Result deleteNode(BasicNode node);

    /**
     * 获取所有节点总数
     * @return
     */
    public Result getNodeTotal();

    /**
     * 获取模型类节点信息
     * @param name
     * @return
     */
    public Result getModelNode(String name);

    /**
     * @return 实体类、事件类和模型类的知识数量
     */
    public Result getNodeCountByCategory();

    /**
     * 根据节点ID更新节点属性（完全替换模式）
     * 注意：此方法会完全替换节点的所有属性，未提供的属性将被删除
     * @param nodeId 节点ID
     * @param properties 要设置的属性键值对，必须包含name属性
     * @param labels 要设置的标签列表
     * @return 是否更新成功封装的Result对象
     */
    public Result updateNodePropertiesById(Long nodeId, Map<String, Object> properties,List<String> labels);

    /**
     * 根据节点名称和类型查询图数据
     * @param name 节点名称
     * @param type 查询类型：1-一跳关系，2-两跳关系，3-全部关系
     * @return 包含节点列表和关系列表的Result对象
     */
    public Result queryGraphByNameAndType(String name, Integer type);

}
