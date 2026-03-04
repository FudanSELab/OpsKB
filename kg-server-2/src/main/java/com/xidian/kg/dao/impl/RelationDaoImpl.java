package com.xidian.kg.dao.impl;

import com.fasterxml.jackson.core.JsonGenerator;
import com.xidian.kg.entity.QueryRelation;
import org.apache.commons.collections.IteratorUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xidian.kg.dao.RelationDao;
import com.xidian.kg.entity.BasicNode;
import com.xidian.kg.entity.BasicRelationReturnVO;
import lombok.SneakyThrows;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Path;
import org.neo4j.driver.types.Relationship;
import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.session.Session;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

@Component
public class RelationDaoImpl implements RelationDao {

    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        // 对mapper进行配置，生成的键值对，键不带引号。
        // 因为neo4j语句中，如create (n:teacher{name:'张三'})，如果name带了引号，会发生错误
        mapper.configure(JsonGenerator.Feature.QUOTE_FIELD_NAMES, false);
    }


    @SneakyThrows
    public static String propertiesMapToPropertiesStr(Map<String,Object> map) {
//        map.entrySet().removeIf(entry -> Func.isEmpty(entry.getValue()));
        return mapper.writeValueAsString(map);
    }

    @Resource
    private Session session;

    public Session getSession() {
        return this.session;
    }


    /**
     * 转化neo4j默认查询的参数为自定返回类型
     *
     * @param selfContainedSegment
     * @return Neo4jBasicRelationReturn
     */
    public BasicRelationReturnVO changeToNeo4jBasicRelationReturnVO(Path.Segment selfContainedSegment) {
        BasicRelationReturnVO neo4JBasicRelationReturnVO = new BasicRelationReturnVO();
        //start
        Node start = selfContainedSegment.start();
        BasicNode startNodeVo = new BasicNode();
        startNodeVo.setId(start.id());
        startNodeVo.setLabels(IteratorUtils.toList(start.labels().iterator()));
        startNodeVo.setProperties(start.asMap());
        neo4JBasicRelationReturnVO.setStart(startNodeVo);
        //end
        Node end = selfContainedSegment.end();
        BasicNode endNodeVo = new BasicNode();
        endNodeVo.setId(end.id());
        endNodeVo.setLabels(IteratorUtils.toList(end.labels().iterator()));
        endNodeVo.setProperties(end.asMap());
        neo4JBasicRelationReturnVO.setEnd(endNodeVo);
        //relationship
        QueryRelation neo4JQueryRelation = new QueryRelation();
        Relationship relationship = selfContainedSegment.relationship();
        neo4JQueryRelation.setStart(relationship.startNodeId());
        neo4JQueryRelation.setEnd(relationship.endNodeId());
        neo4JQueryRelation.setId(relationship.id());
        neo4JQueryRelation.setType(relationship.type());
        neo4JQueryRelation.setProperties(relationship.asMap());
        neo4JBasicRelationReturnVO.setRelation(neo4JQueryRelation);
        return neo4JBasicRelationReturnVO;
    }

    /**
     * 获取知识库中所有关系
     * @return 所有关系组成的列表
     */
    public List<BasicRelationReturnVO> getAllRelations() {
//        RelationVO relationVO = formatRelation(relationDTO);
        String cypherSql = String.format("MATCH p=()-[]->()RETURN p");
        System.out.println(cypherSql);
        long startTime = System.currentTimeMillis();
        Result query = session.query(cypherSql, new HashMap<>());
        Iterable<Map<String, Object>> maps = query.queryResults();
        ArrayList<BasicRelationReturnVO> returnList = new ArrayList<>();
        for (Map<String, Object> map : maps) {
            List<Path.Segment> ps = extractSegments(map.get("p"));
            for (Path.Segment p : ps) {
                returnList.add(changeToNeo4jBasicRelationReturnVO(p));
            }
        }
        session.clear();
        return returnList;
    }

    /**
     * 获取所有的关系名称
     * @return 所有关系名称组成的列表
     */
    public Set<String> getAllRelationType() {
        String cypherSql = "MATCH ()-[r]-() RETURN distinct type(r) as name";
        Result query = session.query(cypherSql, new HashMap<>());
        HashSet<String> relationTypes = new HashSet<>();
        for (Map<String, Object> map : query.queryResults()) {
            relationTypes.add(map.get("name").toString());
        }
        return relationTypes;
    }

    /**
     * 查询关系（支持通过ID或name查询节点）
     * 只匹配起始节点、结束节点和关系类型，不匹配属性
     * @param basicRelationReturnVO
     * @return
     */
    @Override
    public Integer queryRelation(BasicRelationReturnVO basicRelationReturnVO) {
        BasicNode start = basicRelationReturnVO.getStart();
        BasicNode end = basicRelationReturnVO.getEnd();
        QueryRelation relation = basicRelationReturnVO.getRelation();
        
        String cypherSql = "";
        // 构建MATCH语句
        if (start.getId() != null || end.getId() != null) {
            // 使用单个MATCH语句配合WHERE子句，避免笛卡尔积
            cypherSql = "MATCH (n), (m) WHERE ";
            List<String> conditions = new ArrayList<>();
            
            if (start.getId() != null) {
                conditions.add("id(n)=" + start.getId());
            } else if (start.getProperties() != null && start.getProperties().get("name") != null) {
                conditions.add("n.name=\"" + start.getProperties().get("name") + "\"");
            }
            
            if (end.getId() != null) {
                conditions.add("id(m)=" + end.getId());
            } else if (end.getProperties() != null && end.getProperties().get("name") != null) {
                conditions.add("m.name=\"" + end.getProperties().get("name") + "\"");
            }
            
            cypherSql += String.join(" AND ", conditions);
            
            String relationType = "";
            if(relation != null && relation.getType() != null){
                relationType = ":`" + relation.getType() + "`";
            }
            cypherSql += "\nMATCH p=(n)-[r" + relationType + "]->(m) RETURN p";
        } else {
            // 使用name的传统方式
            String startName = "";
            if (start.getProperties() != null && start.getProperties().size() != 0) {
                Map<String, Object> maps = start.getProperties();
                startName = "{name:\"" + maps.get("name") + "\"}";
            }
            String endName = "";
            if (end.getProperties() != null && end.getProperties().size() != 0) {
                Map<String, Object> maps = end.getProperties();
                endName = "{name:\"" + maps.get("name") + "\"}";
            }
            String relationType = "";
            if(relation != null && relation.getType() != null){
                relationType = ":`" + relation.getType() + "`";
            }
            cypherSql = String.format("MATCH p=(n%s)-[r%s]->(m%s) RETURN p",startName,relationType,endName);
        }
        
        System.out.println("查询关系Cypher语句: " + cypherSql);
        Result query = session.query(cypherSql, new HashMap<>());
        Iterable<Map<String, Object>> maps = query.queryResults();
        ArrayList<BasicRelationReturnVO> returnList = new ArrayList<>();
        for (Map<String, Object> map : maps) {
            List<Path.Segment> ps = extractSegments(map.get("p"));
            for (Path.Segment p : ps) {
                returnList.add(changeToNeo4jBasicRelationReturnVO(p));
            }
        }
        session.clear();
        return returnList.size();
    }

    /**
     * 新建关系（支持通过ID或name指定节点）
     * @param basicRelationReturnVO
     * @return
     */
    @Override
    public Integer createRelation(BasicRelationReturnVO basicRelationReturnVO) {
        BasicNode start = basicRelationReturnVO.getStart();
        BasicNode end = basicRelationReturnVO.getEnd();
        
        String cypherSql = "";
        
        // 构建MATCH语句
        if (start.getId() != null || end.getId() != null) {
            // 使用单个MATCH语句配合WHERE子句，避免笛卡尔积
            cypherSql = "MATCH (n), (m) WHERE ";
            List<String> conditions = new ArrayList<>();
            
            if (start.getId() != null) {
                conditions.add("id(n)=" + start.getId());
            } else if (start.getProperties() != null && start.getProperties().get("name") != null) {
                conditions.add("n.name=\"" + start.getProperties().get("name") + "\"");
            }
            
            if (end.getId() != null) {
                conditions.add("id(m)=" + end.getId());
            } else if (end.getProperties() != null && end.getProperties().get("name") != null) {
                conditions.add("m.name=\"" + end.getProperties().get("name") + "\"");
            }
            
            cypherSql += String.join(" AND ", conditions);
        } else {
            // 使用name的传统方式
            String startName = "";
            if (start.getProperties() != null && start.getProperties().size() != 0) {
                Map<String, Object> maps = start.getProperties();
                startName = "{name:\"" + maps.get("name") + "\"}";
            }
            String endName = "";
            if (end.getProperties() != null && end.getProperties().size() != 0) {
                Map<String, Object> maps = end.getProperties();
                endName = "{name:\"" + maps.get("name") + "\"}";
            }
            cypherSql = String.format("%s(n%s),(m%s)","MATCH ",startName,endName);
        }
        
        // 构建CREATE关系语句
        QueryRelation relation = basicRelationReturnVO.getRelation();
        String relationType = "";
        if(relation.getType() != null){
            relationType = ":`" + relation.getType() + "`";
        }
        String relationProperties = "";
        if(relation.getProperties() != null && relation.getProperties().size() != 0){
            relationProperties = propertiesMapToPropertiesStr(relation.getProperties());
        }
        cypherSql += String.format("\n%s(n)-[r%s%s]->(m)","CREATE ",relationType,relationProperties);
        
        System.out.println("创建关系Cypher语句: " + cypherSql);
        Result query = session.query(cypherSql, new HashMap<>());
        session.clear();
        return query.queryStatistics().getRelationshipsCreated();
    }

    /**
     * 删除关系（支持通过ID或name指定节点）
     * @param basicRelationReturnVO
     * @return
     */
    @Override
    public Integer deleteRelation(BasicRelationReturnVO basicRelationReturnVO) {
        BasicNode start = basicRelationReturnVO.getStart();
        BasicNode end = basicRelationReturnVO.getEnd();
        
        String cypherSql = "";
        
        // 构建MATCH语句
        if (start.getId() != null || end.getId() != null) {
            // 使用单个MATCH语句配合WHERE子句，避免笛卡尔积
            cypherSql = "MATCH (n), (m) WHERE ";
            List<String> conditions = new ArrayList<>();
            
            if (start.getId() != null) {
                conditions.add("id(n)=" + start.getId());
            } else if (start.getProperties() != null && start.getProperties().get("name") != null) {
                conditions.add("n.name=\"" + start.getProperties().get("name") + "\"");
            }
            
            if (end.getId() != null) {
                conditions.add("id(m)=" + end.getId());
            } else if (end.getProperties() != null && end.getProperties().get("name") != null) {
                conditions.add("m.name=\"" + end.getProperties().get("name") + "\"");
            }
            
            cypherSql += String.join(" AND ", conditions);
            
            QueryRelation relation = basicRelationReturnVO.getRelation();
            String relationType = "";
            if(relation != null && relation.getType() != null){
                relationType = ":`" + relation.getType() + "`";
            }
            cypherSql += "\nMATCH (n)-[r" + relationType + "]->(m)";
        } else {
            // 使用name的传统方式
            String startName = "";
            if (start.getProperties() != null && start.getProperties().size() != 0) {
                Map<String, Object> maps = start.getProperties();
                startName = "{name:\"" + maps.get("name") + "\"}";
            }
            String endName = "";
            if (end.getProperties() != null && end.getProperties().size() != 0) {
                Map<String, Object> maps = end.getProperties();
                endName = "{name:\"" + maps.get("name") + "\"}";
            }
            QueryRelation relation = basicRelationReturnVO.getRelation();
            String relationType = "";
            if(relation != null && relation.getType() != null){
                relationType = ":`" + relation.getType() + "`";
            }
            cypherSql = String.format("%s(n%s)-[r%s]->(m%s)","MATCH ",startName,relationType,endName);
        }
        
        // 构建DELETE语句
        cypherSql += "\nDELETE r";
        
        System.out.println("删除关系Cypher语句: " + cypherSql);
        Result query = session.query(cypherSql, new HashMap<>());
        session.clear();
        return query.queryStatistics().getRelationshipsDeleted();
    }

    /**
     * 获取某一节点的所有关系
     * @return
     */
    @Override
    public List<BasicRelationReturnVO> getNodeRelationForward(BasicNode basicNode) {
        Map<String,Object> basicNodeProperties = basicNode.getProperties();
        String cypherSql = String.format("match p=(n{name:\"%s\"})-[r]->(m) return p",basicNodeProperties.get("name"));
        System.out.println(cypherSql);
        Result query = session.query(cypherSql, new HashMap<>());
        Iterable<Map<String, Object>> maps = query.queryResults();
        ArrayList<BasicRelationReturnVO> returnList = new ArrayList<>();
        for (Map<String, Object> map : maps) {
            List<Path.Segment> ps = extractSegments(map.get("p"));
            for (Path.Segment p : ps) {
                returnList.add(changeToNeo4jBasicRelationReturnVO(p));
            }
        }
        session.clear();
        return returnList;
    }

    @Override
    public List<BasicRelationReturnVO> getNodeRelationOpposite(BasicNode basicNode) {
        Map<String,Object> basicNodeProperties = basicNode.getProperties();
        String cypherSql = String.format("match p=(n)-[r]->(m{name:\"%s\"}) return p",basicNodeProperties.get("name"));
        System.out.println(cypherSql);
        Result query = session.query(cypherSql, new HashMap<>());
        Iterable<Map<String, Object>> maps = query.queryResults();
        ArrayList<BasicRelationReturnVO> returnList = new ArrayList<>();
        for (Map<String, Object> map : maps) {
            List<Path.Segment> ps = extractSegments(map.get("p"));
            for (Path.Segment p : ps) {
                returnList.add(changeToNeo4jBasicRelationReturnVO(p));
            }
        }
        session.clear();
        return returnList;
    }

    /**
     * 修改关系属性值（支持通过ID或name指定节点）
     * @param basicRelationReturnVO
     * @return
     */
    @Override
    public Integer updateRelationProperties(BasicRelationReturnVO basicRelationReturnVO) {
        BasicNode start = basicRelationReturnVO.getStart();
        BasicNode end = basicRelationReturnVO.getEnd();
        QueryRelation relation = basicRelationReturnVO.getRelation();
        
        // 检查关系对象是否为null
        if (relation == null) {
            System.out.println("关系对象为空");
            return 0;
        }
        
        String cypherSql = "";
        
        // 构建MATCH语句
        if (start.getId() != null || end.getId() != null) {
            // 使用单个MATCH语句配合WHERE子句，避免笛卡尔积
            cypherSql = "MATCH (n), (m) WHERE ";
            List<String> conditions = new ArrayList<>();
            
            if (start.getId() != null) {
                conditions.add("id(n)=" + start.getId());
            } else if (start.getProperties() != null && start.getProperties().get("name") != null) {
                conditions.add("n.name=\"" + start.getProperties().get("name") + "\"");
            }
            
            if (end.getId() != null) {
                conditions.add("id(m)=" + end.getId());
            } else if (end.getProperties() != null && end.getProperties().get("name") != null) {
                conditions.add("m.name=\"" + end.getProperties().get("name") + "\"");
            }
            
            cypherSql += String.join(" AND ", conditions);
            
            String relationType = "";
            if(relation.getType() != null){
                relationType = ":`" + relation.getType() + "`";
            }
            cypherSql += "\nMATCH (n)-[r" + relationType + "]->(m)";
        } else {
            // 使用name的传统方式
            String startName = "";
            if (start.getProperties() != null && start.getProperties().size() != 0) {
                Map<String, Object> maps = start.getProperties();
                startName = "{name:\"" + maps.get("name") + "\"}";
            }
            String endName = "";
            if (end.getProperties() != null && end.getProperties().size() != 0) {
                Map<String, Object> maps = end.getProperties();
                endName = "{name:\"" + maps.get("name") + "\"}";
            }
            String relationType = "";
            if(relation.getType() != null){
                relationType = ":`" + relation.getType() + "`";
            }
            cypherSql = String.format("MATCH (n%s)-[r%s]->(m%s)",startName,relationType,endName);
        }
        
        // 构建SET语句
        Map<String, Object> queryParams = new HashMap<>();
        if(relation.getProperties() != null && !relation.getProperties().isEmpty()){
            // 有属性值，设置具体属性
            String relationProperties = propertiesMapToPropertiesStr(relation.getProperties());
            cypherSql += String.format("\nSET r = %s",relationProperties);
        } else {
            // 属性为空或null，清空关系的所有属性
            // 使用参数化查询来设置空属性对象
            cypherSql += "\nSET r = $props";
            queryParams.put("props", new HashMap<String, Object>());
        }
        
        System.out.println("修改关系属性Cypher语句: " + cypherSql);
        Result query = session.query(cypherSql, queryParams);
        session.clear();
        return query.queryStatistics().getPropertiesSet();
    }

    /**
     * 从json文件中加载关系
     * @param filePath
     * @return
     */
    public Long loadRelationFromJson(String filePath){
        Long loadCount = 0L;
        String cypherSql = "CALL apoc.periodic.iterate(\n" +
                "   \"CALL apoc.load.json('"+ filePath +"') YIELD value return value\",\n" +
                "   \"unwind {value} as row\n" +
                "    match (n) where n.name=row.start\n" +
                "    match (m) where m.name=row.end\n" +
                "    CALL apoc.create.relationship(n,row.type,row.properties,m) yield rel return rel\",\n" +
                "   { batchSize:10000, \n" +
                "     iterateList:true, \n" +
                "     parallel:true\n" +
                "   }\n" +
                ");";
        System.out.println(cypherSql);
        Result query = session.query(cypherSql, new HashMap<>());
        ArrayList<BasicNode> nodeList = new ArrayList<>();
        Iterable<Map<String, Object>> maps = query.queryResults();
        for (Map<String, Object> map : maps) {
            loadCount += (Long) map.get("committedOperations");
        }
        session.clear();
        return loadCount;
    }

    private List<Path.Segment> extractSegments(Object pathObj) {
        List<Path.Segment> segments = new ArrayList<>();
        if (pathObj == null) {
            return segments;
        }
        if (pathObj instanceof Path) {
            Path path = (Path) pathObj;
            for (Path.Segment segment : path) {
                segments.add(segment);
            }
            return segments;
        }
        if (pathObj instanceof Path.Segment[]) {
            segments.addAll(Arrays.asList((Path.Segment[]) pathObj));
            return segments;
        }
        if (pathObj instanceof Iterable) {
            for (Object item : (Iterable<?>) pathObj) {
                if (item instanceof Path.Segment) {
                    segments.add((Path.Segment) item);
                }
            }
        }
        return segments;
    }

}
