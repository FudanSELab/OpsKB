package com.xidian.kg.service.Impl;

//import com.sun.xml.internal.bind.v2.TODO;
import com.xidian.kg.controller.util.Result;
import com.xidian.kg.dao.NodeDao;
import com.xidian.kg.entity.BasicNode;
import com.xidian.kg.entity.GraphResource;
import com.xidian.kg.service.NodeService;
import com.xidian.kg.util.NodeCsvToJson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

@Service
public class NodeServiceImpl implements NodeService {

    @Autowired
    private NodeDao nodeDao;
    /**
     * 根据类别查询节点
     * @param category_main： 主类别，即传感探测类资源知识，情报处理类资源知识等等
     * @param category_detail: 细分类别，即陆地传感探测装置，水下传感探测装置等等
     * @return 该类别下的所有节点信息（json字符串格式）
     */
    @Override
    public Result queryNodeByCategory(String category_main, String category_detail) {
        BasicNode basicNode = new BasicNode();
        List<String> label = new ArrayList<>();
        if(category_main != null){
            label.add(category_main);
        }
        if(category_detail != null){
            label.add(category_detail);
        }
        basicNode.setLabels(label);
//        Map<String, Object> property = new HashMap<>();
//        if(category_detail != null){
//            property.put("type",category_detail); // 此处需要用到细分类别在知识库中存储的字段名叫什么，此处先用type代替，因为原来的知识库中有type字段
//        }
//        basicNode.setProperties(property);
        List<BasicNode> basicNodes = new ArrayList<>();
        try{
            basicNodes = nodeDao.queryNode(basicNode, false);
//            if (basicNodes.size()>50){
//                basicNodes = basicNodes.subList(0,50);
//            }
        }catch (Exception e){
            e.printStackTrace();
            return new Result(false,"发生错误，请稍后查询");
        }
        return new Result(true, basicNodes);
    }

    /**
     * 新建一个节点
     */
    @Override
    public Result createNode(BasicNode node) {
        List<String> label = new ArrayList<>();
        label = node.getLabels();
        Map<String,Object> properties = new HashMap<>();
        properties = node.getProperties();
        BasicNode basicNode = new BasicNode();
        basicNode.setLabels(label);
        basicNode.setProperties(properties);
        if(node.getId() != null){
            if (!nodeDao.queryNode(basicNode, false).isEmpty()){
                return new Result(false,"该节点已存在，创建失败！");
            } return new Result(false,"创建失败");
        } else {
            int result = nodeDao.createNode(basicNode);
            return new Result(true,result);
        }
    }

    /**
     * 查询实体类中所有的大分类和小分类以及对应关系
     * @return
     */
    @Override
    public Result queryEntityCategories() {
        Map<String, Set<String>> entityCategories = new HashMap<>();
        try{
            entityCategories  = nodeDao.getEntityCategories();
        }catch (Exception e){
            e.printStackTrace();
            return new Result(false,"发生错误，请稍后查询");
        }
        return new Result(true,entityCategories);

    }

    /**
     * 查询事件类中所有的大分类和小分类以及对应关系
     * @return
     */
    @Override
    public Result queryEventCategories() {
        Map<String, Set<String>> eventCategories = new HashMap<>();
        try{
            eventCategories  = nodeDao.getEventCategories();
        }catch (Exception e){
            e.printStackTrace();
            return new Result(false,"发生错误，请稍后查询");
        }
        return new Result(true,eventCategories);
    }

    /**
     * 查询模型类中所有的大分类和小分类以及对应关系
     * @return
     */
    @Override
    public Result queryModelCategories() {
        Map<String, Set<String>> modelCategories = new HashMap<>();
        try{
            modelCategories  = nodeDao.getModelCategories();
        }catch (Exception e){
            e.printStackTrace();
            return new Result(false,"发生错误，请稍后查询");
        }
        return new Result(true,modelCategories);
    }

    /**
     * 查询策略树类中所有的大分类和小分类以及对应关系
     * @return
     */
    @Override
    public Result queryTreeCategories() {
        Map<String, Set<String>> treeCategories = new HashMap<>();
        try{
            treeCategories  = nodeDao.getTreeCategories();
        }catch (Exception e){
            e.printStackTrace();
            return new Result(false,"发生错误，请稍后查询");
        }
        return new Result(true,treeCategories);
    }

    /**
     * 根据节点名称查询节点
     * @param name: 节点名称
     * @param exactMatch: 是否精确匹配（true表示全字符匹配，false或null表示模糊匹配）
     * @return 封装的Result对象
     */
    public Result queryNodeByName(String name, Boolean exactMatch){
        BasicNode node = new BasicNode();
        Map<String, Object> properties = new HashMap<>();
        properties.put("name",name);
        node.setProperties(properties);
        List<BasicNode> basicNodes = nodeDao.queryNode(node, exactMatch);
        if(basicNodes.size() == 0){
            return new Result(false,"当前暂无要查询节点");
        }else{
            return new Result(true,basicNodes);
        }
    }

    public Result getModelNode(String name){
        List<Map> result = new ArrayList<>();
        BasicNode node = new BasicNode();
        Map<String, Object> properties = new HashMap<>();
        properties.put("name",name);
        node.setProperties(properties);
        List<BasicNode> basicNodes = nodeDao.queryNode(node, false);
        if(basicNodes.size() == 0){
            return new Result(false,"当前暂无要查询节点");
        }
        for(BasicNode i : basicNodes){
            Map<String, Object> nodeResult = new HashMap<>();
            nodeResult.put("nodeInfo",i);
            nodeResult.put("graphInfo", GraphResource.graphData.get(i.getProperties().get("name")));
            result.add(nodeResult);
        }
        return new Result(true,result.get(0));
    }

    /**
     * 向知识库中的节点添加属性
     * @param nodeName 要添加属性的节点名称
     * @param properties 要添加的属性键值对
     * @return 是否添加成功封装的Result对象
     */
    public Result addPropertiesToNode(String nodeName, Map<String,String> properties){
        // 首先查询该节点是否存在该属性
        BasicNode node = new BasicNode();
        Map<String, Object> node_prop = new HashMap<>();
        node_prop.put("name",nodeName);
        node.setProperties(node_prop);
        List<BasicNode> basicNodes = nodeDao.queryNode(node, false);
        if(basicNodes == null || basicNodes.size() == 0){
            return new Result(false, "添加失败，不存在当前节点");
        }
        for(String property_add: properties.keySet()){
            if(basicNodes.get(0).getProperties().keySet().contains(property_add)){
                return new Result(false,"添加失败，当前节点已存在"+property_add+"属性");
            }
        }
        int i = nodeDao.addPropertyToNode(nodeName, properties);
        if(i > 0){
            return new Result(true,"添加成功,共添加了"+i+"条属性");
        }
        return new Result(false,"添加属性失败");
    }

    /**
     * 删除知识库中某个节点的属性
     * @param nodeName 要删除属性的节点名称
     * @param properties 要删除的属性字段名
     * @return 是否删除成功封装的Result对象
     */
    public Result removePropertiesFromNode(String nodeName, List<String> properties){
        Map<String,Object> property = new HashMap<>();
        property.put("name",nodeName);
        BasicNode basicNode = new BasicNode();
        basicNode.setProperties(property);
        List<BasicNode> node_result = nodeDao.queryNode(basicNode, false);
        if(node_result == null || node_result.size() == 0){
            return new Result(false,"删除失败，当前节点不存在");
        }
        for(String p : properties){
            if(!node_result.get(0).getProperties().keySet().contains(p)){
                return new Result(false,"删除失败，当前节点不存在"+p+"属性");
            }
        }
        int i = nodeDao.removePropertyFromNode(nodeName, properties);
        if(i > 0){
            return new Result(true, "删除成功，共删除了节点"+nodeName + "的" + i +"条属性");
        }
        return new Result(false,"删除属性失败");
    }

    /**
     * 修改知识库中节点的属性值
     * 注意：如果属性不存在则自动创建，如果存在则修改其值
     * @param nodeName 要修改属性值的节点名称
     * @param properties 要修改的属性键值对
     * @return 是否修改成功包装的Result字符串对象
     */
    public Result setNodeProperty(String nodeName, Map<String,Object> properties){
        if(properties.keySet().contains("name")){
            return new Result(false,"修改失败，不可修改节点的name属性值");
        }
        Map<String,Object> prop = new HashMap<>();
        prop.put("name",nodeName);
        BasicNode basicNode = new BasicNode();
        basicNode.setProperties(prop);
        List<BasicNode> basicNodes = nodeDao.queryNode(basicNode, false);
        if(basicNodes == null || basicNodes.size() == 0){
            return new Result(false,"修改失败，当前节点不存在");
        }
        // 移除了属性存在性检查，允许设置不存在的属性（自动创建）
        int i = nodeDao.setNodeProperty(nodeName, properties);
        if(i > 0){
            return new Result(true, "设置成功，共设置了" + i + "条属性");
        }
        return new Result(false,"设置属性值失败");
    }

    /**
     * 获得实体类中每个类别的节点个数
     * @return 实体类下所有类别和节点个数键值对组成的map集合，封装后的Result对象字符串
     */
    public Result getEntityCountByCategory(){
        Map<String, Set<String>> entityCategories = nodeDao.getEntityCategories(); // 大分类和小分类
        if(entityCategories.size() == 0){
            return new Result(false,"当前知识库中暂无实体类知识分类");
        }
        Set<String> categories = entityCategories.keySet();
        System.out.println(categories);
        Map<String, Long> countByCategory;
        try {
            countByCategory= nodeDao.getCountByCategory(new ArrayList<>(categories));
        }catch (Exception e){
            e.printStackTrace();
            return new Result(false,"查询失败，请稍后重试");
        }
        return new Result(true, countByCategory);
    }

    /**
     * 获得事件类中每个类别的节点个数
     * @return 事件类下所有类别和节点个数键值对组成的map集合，封装后的Result对象字符串
     */
    public Result getEventCountByCategory(){
        Map<String, Set<String>> eventCategories = nodeDao.getEventCategories();
        if(eventCategories.size() == 0){
            return new Result(false,"当前知识库中暂无事件类知识分类");
        }
        Set<String> categories = eventCategories.keySet();
        System.out.println(categories);
        Map<String, Long> countByCategory;
        try {
            countByCategory= nodeDao.getCountByCategory(new ArrayList<>(categories));
        }catch (Exception e){
            e.printStackTrace();
            return new Result(false,"查询失败，请稍后重试");
        }
        return new Result(true, countByCategory);
    }

    /**
     * 获得模型类中每个类别的节点个数
     * @return 模型类下所有类别和节点个数键值对组成的map集合，封装后的Result对象字符串
     */
    public Result getModelCountByCategory(){
        Map<String, Set<String>> modelCategories = nodeDao.getModelCategories();
        if(modelCategories.size() == 0){
            return new Result(false,"当前知识库中暂无模型类知识分类");
        }
        Set<String> categories = modelCategories.keySet();
        System.out.println(categories);
        Map<String, Long> countByCategory;
        try {
            countByCategory= nodeDao.getCountByCategory(new ArrayList<>(categories));
        }catch (Exception e){
            e.printStackTrace();
            return new Result(false,"查询失败，请稍后重试");
        }
        return new Result(true, countByCategory);
    }

    /**
     * 获得策略树类中每个类别的节点个数
     * @return 策略树类下所有类别和节点个数键值对组成的map集合，封装后的Result对象字符串
     */
    public Result getTreeCountByCategory(){
        Map<String, Set<String>> treeCategories = nodeDao.getTreeCategories();
        if(treeCategories.size() == 0){
            return new Result(false,"当前知识库中暂无策略树类知识分类");
        }
        Set<String> categories = treeCategories.keySet();
        System.out.println(categories);
        Map<String, Long> countByCategory;
        try {
            countByCategory= nodeDao.getCountByCategory(new ArrayList<>(categories));
        }catch (Exception e){
            e.printStackTrace();
            return new Result(false,"查询失败，请稍后重试");
        }
        return new Result(true, countByCategory);
    }

    /**
     * 删除节点
     * @param node
     * @return
     */
    @Override
    public Result deleteNode(BasicNode node) {
        List<String> label = node.getLabels();
        Map<String,Object> properties = node.getProperties();
        BasicNode basicNode = new BasicNode();
        basicNode.setLabels(label);
        basicNode.setProperties(properties);
        if(nodeDao.queryNodeByName(basicNode).size() > 0){
            Integer result = nodeDao.delNode(basicNode);
            return new Result(true,result);
        }else {
            return new Result(false,"删除失败，节点不存在！");
        }
    }


    public Result getNodeCountByCategory(){
        List<String> labels = new ArrayList<>();
        labels.add("entity");
        labels.add("model");
        labels.add("event");
        labels.add("tree");
        try{
            //Map<String, Long> countByCategory = nodeDao.getCountByCategory(labels);
            Map<String, Long> countByCategory2 = nodeDao.getCountByCategorySph();
            return new Result(true,countByCategory2);
        }catch (Exception e){
            e.printStackTrace();
            return new Result(false, "发生未知错误");
        }
    }

    /**
     * 获取所有节点总数
     * @return
     */
    @Override
    public Result getNodeTotal() {
        System.out.println("nums:"+nodeDao.getNodeNum());
        List<BasicNode> allNodes = nodeDao.getAllNodes();
        Integer result ;
        try{
            result = allNodes.size();
        }catch (Exception e){
            e.printStackTrace();
            return new Result(false,"发生错误，请稍后查询");
        }
        return new Result(true,result);
    }

    /**
     * 根据节点ID更新节点属性（完全替换模式）
     * 注意：此方法会完全替换节点的所有属性，未提供的属性将被删除
     * @param nodeId 节点ID
     * @param properties 要设置的属性键值对，必须包含name属性
     * @param labels 要设置的标签列表
     * @return 是否更新成功封装的Result对象
     */
    @Override
    public Result updateNodePropertiesById(Long nodeId, Map<String, Object> properties,List<String> labels) {
        if (nodeId == null) {
            return new Result(false, "节点ID不能为空");
        }
        
        if (labels == null || labels.isEmpty()) {
            return new Result(false, "标签不能为空");
        }
        
        if (properties == null || properties.isEmpty()) {
            return new Result(false, "属性不能为空");
        }
        
        if (!properties.containsKey("name")) {
            return new Result(false, "属性中必须包含name字段");
        }
        
        try {
            // 首先检查节点是否存在
            BasicNode queryNode = new BasicNode();
            queryNode.setId(nodeId);
            List<BasicNode> existingNodes = nodeDao.queryNode(queryNode, false);
            
            if (existingNodes.isEmpty()) {
                return new Result(false, "指定ID的节点不存在");
            }
            
            // 执行更新操作
            int updatedCount = nodeDao.updateNodePropertiesById(nodeId, properties,labels);
            
            if (updatedCount > 0) {
                return new Result(true, "节点属性更新成功，更新了 " + updatedCount + " 个属性");
            } else {
                return new Result(false, "节点属性更新失败");
            }
            
        } catch (IllegalArgumentException e) {
            return new Result(false, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "更新节点属性时发生错误：" + e.getMessage());
        }
    }

    /**
     * 根据节点名称和类型查询图数据
     * @param name 节点名称
     * @param type 查询类型：1-一跳关系，2-两跳关系，3-全部关系
     * @return 包含节点列表和关系列表的Result对象
     */
    @Override
    public Result queryGraphByNameAndType(String name, Integer type) {
        // 参数校验
        if (name == null || name.trim().isEmpty()) {
            return new Result(false, "节点名称不能为空");
        }
        
        if (type == null || (type != 1 && type != 2 && type != 3)) {
            return new Result(false, "type参数必须为1、2或3");
        }
        
        try {
            // 首先检查节点是否存在
            BasicNode node = new BasicNode();
            Map<String, Object> properties = new HashMap<>();
            properties.put("name", name);
            node.setProperties(properties);
            List<BasicNode> basicNodes = nodeDao.queryNode(node, false);
            
            if (basicNodes == null || basicNodes.isEmpty()) {
                return new Result(false, "未找到名称为 " + name + " 的节点");
            }
            
            // 查询图数据
            Map<String, Object> graphData = nodeDao.queryGraphByNameAndType(name, type);
            
            if (graphData == null) {
                return new Result(false, "查询图数据失败");
            }
            
            List<BasicNode> nodes = (List<BasicNode>) graphData.get("nodes");
            List<Map<String, Object>> relations = (List<Map<String, Object>>) graphData.get("relations");
            
            if (nodes.isEmpty() && relations.isEmpty()) {
                return new Result(true, "该节点没有相关的关系数据");
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("nodes", nodes);
            result.put("relations", relations);
            result.put("nodeCount", nodes.size());
            result.put("relationCount", relations.size());
            
            return new Result(true, result);
            
        } catch (IllegalArgumentException e) {
            return new Result(false, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "查询图数据时发生错误：" + e.getMessage());
        }
    }

}
