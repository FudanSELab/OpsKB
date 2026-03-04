package com.xidian.kg;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xidian.kg.controller.util.Result;
import com.xidian.kg.dao.impl.NodeDaoImpl;
import com.xidian.kg.dao.impl.RelationDaoImpl;
import com.xidian.kg.entity.BasicNode;
import com.xidian.kg.entity.BasicRelationReturnVO;
import com.xidian.kg.entity.QueryRelation;
import com.xidian.kg.service.NodeService;
import com.xidian.kg.service.RelationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
class KgApplicationTests {

    @Autowired
    private NodeService nodeService;

    @Autowired
    private RelationDaoImpl relationDao;

    @Autowired
    private NodeDaoImpl nodeDao;

    @Autowired
    private RelationService relationService;

    @Test
    void contextLoads() {
    }

    @Test
    void getAllLabelsTest(){
        System.out.println(nodeDao.getAllLabelName());
    }

    @Test
    void getAllRelationTypes(){
        System.out.println(relationDao.getAllRelationType());
    }

    @Test
    void queryNodeTest(){
        BasicNode basicNode = new BasicNode();
        List<String> labels = new ArrayList<>();
        labels.add("entity");
        basicNode.setLabels(labels);
        Map<String, Object> properties = new HashMap<>();
        properties.put("type","通用飞机");
        properties.put("flightSpeed","亚音速");
        properties.put("name","AB95单发活塞式轻型飞机");
        basicNode.setProperties(properties);
        System.out.println(nodeDao.queryNode(basicNode, false));
    }

    @Test
    void insertNode(){
        BasicNode node = new BasicNode();
        List<String> labels = new ArrayList<>();
        labels.add("countries");
        node.setLabels(labels);
        Map<String, Object> properties = new HashMap<>();
        properties.put("name","1");
        node.setProperties(properties);
        System.out.println(nodeDao.createNode(node));
    }

    @Test
    void delNode(){
        BasicNode node = new BasicNode();
        List<String> labels = new ArrayList<>();
        labels.add("countries");
        node.setLabels(labels);
        Map<String, Object> properties = new HashMap<>();
        properties.put("name","1");
        node.setProperties(properties);
        System.out.println(nodeDao.delNode(node));
    }

    @Test
    void getAllNodes(){
        System.out.println(nodeDao.getAllNodes().size());
        System.out.println(nodeDao.getAllNodes().get(0).getProperties().get("assertElement"));
        System.out.println(nodeDao.getAllNodes().get(0).getLabels());
    }

    @Test
    void getAllRelations() throws JsonProcessingException {
//        System.out.println(relationDao.getAllRelations().size());
//        System.out.println("------------------------------------------------");
        System.out.println(new ObjectMapper().writeValueAsString(relationDao.getAllRelations()));
    }

    @Test
    void getEntityCategories(){
//        System.out.println(nodeDao.getEntityCategories().size());
        System.out.println(nodeDao.getEntityCategories());
//        System.out.println(nodeDao.getEntityCategories().get(0).getLabels());
//        System.out.println("---------------------");
//        System.out.println(nodeDao.getEntityCategories().get(0).getProperties().get("type"));

    }

    @Test
    void getEventCategories(){
        System.out.println(nodeDao.getEventCategories());
    }

    @Test
    void getModelCategories(){
        System.out.println(nodeDao.getModelCategories());
    }

    @Test
    void addPropertyToNode(){
        Map<String, String> properties = new HashMap<>();
        properties.put("t1","t1_value");
        properties.put("t2","t2_value");
        int i = nodeDao.addPropertyToNode("111", properties);
        System.out.println(i);
    }

    @Test
    void removePropertyFromNode(){
        List<String> propList = new ArrayList<>();
        propList.add("type");
//        propList.add("country");
        int i = nodeDao.removePropertyFromNode("111", propList);
        System.out.println(i);
    }

    @Test
    void setNodeProperties(){
        Map<String,Object> properties = new HashMap<>();
        properties.put("name","222");
        int i = nodeDao.setNodeProperty("111", properties);
        System.out.println(i);
    }

    @Test
    void getEntityCountByCategory(){
        List<String> category = new ArrayList<>();
        category.add("传感探测类");
        category.add("情报处理类");
        System.out.println(nodeDao.getCountByCategory(category));
    }


    @Test
    void queryNodeByName() {
        BasicNode basicNode = new BasicNode();
        List<String> labels = new ArrayList<>();
        labels.add("countries");
        basicNode.setLabels(labels);
        Map<String, Object> properties = new HashMap<>();
        properties.put("name", "奥地利");
        basicNode.setProperties(properties);
        System.out.println(nodeDao.queryNodeByName(basicNode));
    }

    @Test
    void createRelation(){
        BasicRelationReturnVO basicRelationReturnVO = new BasicRelationReturnVO();
        BasicNode start = new BasicNode();
        Map<String,Object> startProperties = new HashMap<>();
        startProperties.put("name","智利");
        start.setProperties(startProperties);
        basicRelationReturnVO.setStart(start);

        BasicNode end = new BasicNode();
        Map<String,Object> endProperties = new HashMap<>();
        endProperties.put("name","日本");
        end.setProperties(endProperties);
        basicRelationReturnVO.setEnd(end);

        QueryRelation relation = new QueryRelation();
        relation.setType("弟弟");
        Map<String,Object> relationProperties = new HashMap<>();
        relationProperties.put("brother","zz");
        relation.setProperties(relationProperties);
        basicRelationReturnVO.setRelation(relation);

        System.out.println(relationDao.createRelation(basicRelationReturnVO));
    }

    @Test
    void queryRelation(){
        BasicRelationReturnVO basicRelationReturnVO = new BasicRelationReturnVO();
        BasicNode start = new BasicNode();
        Map<String,Object> startProperties = new HashMap<>();
        startProperties.put("name","1");
        start.setProperties(startProperties);
        basicRelationReturnVO.setStart(start);

        BasicNode end = new BasicNode();
        Map<String,Object> endProperties = new HashMap<>();
        endProperties.put("name","2");
        end.setProperties(endProperties);
        basicRelationReturnVO.setEnd(end);

        QueryRelation relation = new QueryRelation();
        relation.setType("儿子");
        basicRelationReturnVO.setRelation(relation);

        System.out.println(relationDao.createRelation(basicRelationReturnVO));
    }

    @Test
    void delRelation(){
        BasicRelationReturnVO basicRelationReturnVO = new BasicRelationReturnVO();
        BasicNode start = new BasicNode();
        Map<String,Object> startProperties = new HashMap<>();
        startProperties.put("name","智利");
        start.setProperties(startProperties);
        basicRelationReturnVO.setStart(start);

        BasicNode end = new BasicNode();
        Map<String,Object> endProperties = new HashMap<>();
        endProperties.put("name","日本");
        end.setProperties(endProperties);
        basicRelationReturnVO.setEnd(end);

        QueryRelation relation = new QueryRelation();
        relation.setType("弟弟");
        Map<String,Object> relationProperties = new HashMap<>();
        relationProperties.put("brother","zz");
        relation.setProperties(relationProperties);
        basicRelationReturnVO.setRelation(relation);

        System.out.println(relationDao.deleteRelation(basicRelationReturnVO));
    }

//    @Test
//    void getNodeRelation(){
//        BasicNode basicNode = new BasicNode();
//        Map<String,Object> maps = new HashMap<>();
//        maps.put("name","伊朗");
//        basicNode.setProperties(maps);
//        System.out.println(relationDao.getNodeRelation(basicNode));
//    }

    @Test
    void getNodeRelationService(){
        BasicNode basicNode = new BasicNode();
        Map<String,Object> maps = new HashMap<>();
        maps.put("name","伊朗");
        basicNode.setProperties(maps);
        System.out.println(relationService.getNodeRelation(basicNode));
    }

    @Test
    void updateRelationProperties(){
        BasicRelationReturnVO basicRelationReturnVO = new BasicRelationReturnVO();
        BasicNode start = new BasicNode();
        Map<String,Object> startProperties = new HashMap<>();
        startProperties.put("name","zhongdian");
        start.setProperties(startProperties);
        basicRelationReturnVO.setStart(start);

        BasicNode end = new BasicNode();
        Map<String,Object> endProperties = new HashMap<>();
        endProperties.put("name","Zhongguo");
        end.setProperties(endProperties);
        basicRelationReturnVO.setEnd(end);

        QueryRelation relation = new QueryRelation();
        Map<String,Object> relationProperties = new HashMap<>();
        relationProperties.put("brother","bb");
        relation.setProperties(relationProperties);
        basicRelationReturnVO.setRelation(relation);

        System.out.println(relationDao.updateRelationProperties(basicRelationReturnVO));
    }

    @Test
    void getEntityCountByCategoryService(){
        Result entityCountByCategory = nodeService.getEntityCountByCategory();
        System.out.println(entityCountByCategory);
    }

    @Test
    void loadNodeFromJson(){
        Long count = nodeDao.loadNodeFromJson("file:///111.json");
        System.out.println(count);
    }

    @Test
    void loadRelationFromJson(){
        Long aLong = relationDao.loadRelationFromJson("file:///relation.json");
        System.out.println(aLong);
    }

    @Test
    void deleteFile(){
        File file = new File("D:\\neo4j\\neo4j-community-3.5.31\\import\\json\\ccccc.csv.json");
        System.out.println(file.delete());
    }
}
