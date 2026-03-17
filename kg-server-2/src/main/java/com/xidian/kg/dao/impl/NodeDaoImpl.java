package com.xidian.kg.dao.impl;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xidian.kg.dao.NodeDao;
import com.xidian.kg.entity.BasicNode;
import lombok.SneakyThrows;
import org.apache.commons.collections.IteratorUtils;
import org.neo4j.ogm.model.Node;
import org.neo4j.ogm.model.Property;
import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.response.model.NodeModel;
import org.neo4j.ogm.session.Session;
import org.neo4j.driver.types.Path;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

@Component
public class NodeDaoImpl implements NodeDao {

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

    @Override
    public List<BasicNode> getNodesForKbWithLimit(List<String> kbAliases, List<String> labelFallbacks, int limit) {
        // MATCH (n)
        // WHERE n.kb_id IN $kbAliases
        //    OR (n.kb_id IS NULL AND any(l IN labels(n) WHERE l IN $labels))
        // RETURN n LIMIT $limit
        String cypher;
        Map<String, Object> params = new HashMap<>();
        params.put("kbAliases", kbAliases);
        params.put("limit", limit);
        if (labelFallbacks != null && !labelFallbacks.isEmpty()) {
            params.put("labels", labelFallbacks);
            cypher = "MATCH (n) WHERE n.kb_id IN $kbAliases " +
                    "OR (n.kb_id IS NULL AND any(l IN labels(n) WHERE l IN $labels)) " +
                    "RETURN n LIMIT $limit";
        } else {
            cypher = "MATCH (n) WHERE n.kb_id IN $kbAliases RETURN n LIMIT $limit";
        }
        Result query = session.query(cypher, params);
        List<BasicNode> nodeList = new ArrayList<>();
        for (Map<String, Object> map : query.queryResults()) {
            NodeModel queryNode = (NodeModel) map.get("n");
            BasicNode node = new BasicNode();
            node.setId(queryNode.getId());
            node.setLabels(Arrays.asList(queryNode.getLabels()));
            HashMap<String, Object> proMap = new HashMap<>();
            for (Property<String, Object> prop : queryNode.getPropertyList()) {
                proMap.put(prop.getKey(), prop.getValue());
            }
            node.setProperties(proMap);
            nodeList.add(node);
        }
        session.clear();
        return nodeList;
    }

    /**
     * 获取知识库中所有的节点信息
     * @return
     */
    public List<BasicNode> getAllNodes(){
        String cypherSql = "match (n) return n";
        System.out.println(cypherSql);
        Result query = session.query(cypherSql,new HashMap<>());
        ArrayList<BasicNode> nodeList = new ArrayList<>();
        Iterable<Map<String, Object>> maps = query.queryResults();
        for (Map<String, Object> map : maps) {
            NodeModel queryNode = (NodeModel) map.get("n");
            BasicNode startNodeVo = new BasicNode();
            startNodeVo.setId(queryNode.getId());
            startNodeVo.setLabels(Arrays.asList(queryNode.getLabels()));
            List<Property<String, Object>> propertyList = queryNode.getPropertyList();
            HashMap<String, Object> proMap = new HashMap<>();
            for (Property<String, Object> stringObjectProperty : propertyList) {
                if (proMap.containsKey(stringObjectProperty.getKey())) {
                    throw new RuntimeException("数据重复");
                }
                proMap.put(stringObjectProperty.getKey(), stringObjectProperty.getValue());
            }
            startNodeVo.setProperties(proMap);
            nodeList.add(startNodeVo);
        }
        session.clear();
        return nodeList;
    }

    /**
     * 获取所有的标签名称(去重之后的)
     * @return
     */
    public Set<String> getAllLabelName() {
        String cypherSql = "match (n) return distinct labels(n) as name";
        Result query = session.query(cypherSql, new HashMap<>());
        HashSet<String> labelNames = new HashSet<>();
        for (Map<String, Object> map : query.queryResults()) {
            String[] names = (String[]) map.get("name");
            for (String name : names) {
                labelNames.add(name);
            }
        }
        return labelNames;
    }

    /**
     * 按条件查询节点
     * @param node
     * @param exactMatch 是否精确匹配（true表示全字符匹配，false或null表示模糊匹配）
     * @return 返回节点集合
     */
    public List<BasicNode> queryNode(BasicNode node, Boolean exactMatch) {
        String cypherSql = "";
        if (node.getId() != null) {
            // 如果有id，则按照id查询
            cypherSql = String.format("MATCH (n) where id(n)=%s return n", node.getId());
        } else {
            // 如果没有id，再按照label和property查询
            String labels = "";
            if (node.getLabels() != null && node.getLabels().size() != 0) {
                labels = ":`" + String.join("`:`", node.getLabels()) + "`";
                cypherSql = String.format("match(n%s) return n", labels);
            }
            String propertyKey = "";
            String propertyValue = "";
            if (node.getProperties() != null && node.getProperties().size() != 0) {
                propertyKey = node.getProperties().keySet().iterator().next();
                propertyValue = node.getProperties().get(propertyKey).toString();
                // 根据 exactMatch 参数决定使用精确匹配还是模糊匹配
                if (exactMatch != null && exactMatch) {
                    // 精确匹配：使用 = 运算符
                    cypherSql = String.format("match(n) where n.%s = \"%s\" return n", propertyKey, propertyValue);
                } else {
                    // 模糊匹配：使用 contains 运算符
                    cypherSql = String.format("match(n) where n.%s contains \"%s\" return n", propertyKey, propertyValue);
                }
            }

        }
        System.out.println(cypherSql);
        Result query = session.query(cypherSql, new HashMap<>());
        ArrayList<BasicNode> nodeList = new ArrayList<>();
        Iterable<Map<String, Object>> maps = query.queryResults();
        for (Map<String, Object> map : maps) {
            NodeModel queryNode = (NodeModel) map.get("n");
            BasicNode startNodeVo = new BasicNode();
            startNodeVo.setId(queryNode.getId());
            startNodeVo.setLabels(Arrays.asList(queryNode.getLabels()));
            List<Property<String, Object>> propertyList = queryNode.getPropertyList();
            HashMap<String, Object> proMap = new HashMap<>();
            for (Property<String, Object> stringObjectProperty : propertyList) {
                if (proMap.containsKey(stringObjectProperty.getKey())) {
                    throw new RuntimeException("数据重复");
                }
                proMap.put(stringObjectProperty.getKey(), stringObjectProperty.getValue());
            }
            startNodeVo.setProperties(proMap);
            nodeList.add(startNodeVo);
        }
        session.clear();
        return nodeList;
    }

    /**
     * 创建节点
     * 注意： 不会判断要插入的节点在当前数据库中是否已经存在
     * @param node  节点
     * @return
     */
    public int createNode(BasicNode node) {
        String labels = "";
        if (node.getLabels() != null && node.getLabels().size() != 0) {
            labels = ":`" + String.join("`:`", node.getLabels()) + "`";
        }
        String property = "";
        if (node.getProperties() != null && node.getProperties().size() != 0) {
            property = propertiesMapToPropertiesStr(node.getProperties());
        }
        String cypherSql = String.format("%s(%s%s)","create ", labels, property);
        Result query = session.query(cypherSql, new HashMap<>());
        session.clear();
        return query.queryStatistics().getNodesCreated();
    }

    @Override
    public int getNodeNum() {
        String cyperSql = "Match(n) return count(n)";
        Result query = session.query(cyperSql,new HashMap<>());
        System.out.println("query.queryResults():"+query.queryResults());
        //query.queryResults();
        return 0;
    }

    /**
     * 删除节点和相关关系
     * @param node        节点
     * @return
     */
    public Integer delNode(BasicNode node) {
        String cypherSql = "";
        if (node.getId() != null) {
            cypherSql = String.format("MATCH (n) where id(n)=%s ", node.getId());
        } else {
            String labels = "";
            if (node.getLabels() != null && node.getLabels().size() != 0) {
                labels = ":`" + String.join("`:`", node.getLabels()) + "`";
            }
            String property = "";
            if (node.getProperties() != null && node.getProperties().size() != 0) {
                property = propertiesMapToPropertiesStr(node.getProperties());
            }
            cypherSql = String.format("match(n%s%s) ", labels, property);
        }
        cypherSql += "DETACH DELETE n";
        Result query = session.query(cypherSql, new HashMap<>());
        session.clear();
        return query.queryStatistics().getNodesDeleted();
    }

    /**
     * 获取实体类知识中所有大分类和小分类的名称以及对应关系
     * 封装返回的数据格式：
     * {
     *     大分类1：[对应的小分类列表],
     *     大分类2: [对应的小分类列表],
     *     ……
     * }
     */
    public Map<String, Set<String>>  getEntityCategories(){
        // 首先查询出实体类的所有大类别（标签名，除entity外）
        Map<String , Set<String>> result = new HashMap<>();
        String cypherSql1 = "match (n:entity) return distinct labels(n) as name";
        Result query1 = session.query(cypherSql1, new HashMap<>());
        HashSet<String> labelNames = new HashSet<>(); // 记录所有大分类名称
        for (Map<String, Object> map : query1.queryResults()) {
            String[] names = (String[]) map.get("name");
            for (String name : names) {
                labelNames.add(name);
            }
        }
        if(labelNames.contains("entity")){
            labelNames.remove("entity");
        }
        // 将所有实体类的标签名（除entity）作为键，值为新建的HashSet集合，放入最终结果。
        for(String label : labelNames){
            result.put(label,new HashSet<>());
        }
        String cypherSql2 = "match (n:entity) return n";
        Result query2 = session.query(cypherSql2, new HashMap<>());
        ArrayList<BasicNode> nodeList = new ArrayList<>();// 得到所有实体类节点信息组成的列表
        Iterable<Map<String, Object>> maps = query2.queryResults();
        for (Map<String, Object> map : maps) {
            NodeModel queryNode = (NodeModel) map.get("n");
            BasicNode startNodeVo = new BasicNode();
            startNodeVo.setId(queryNode.getId());
            startNodeVo.setLabels(Arrays.asList(queryNode.getLabels()));
            List<Property<String, Object>> propertyList = queryNode.getPropertyList();
            HashMap<String, Object> proMap = new HashMap<>();
            for (Property<String, Object> stringObjectProperty : propertyList) {
                if (proMap.containsKey(stringObjectProperty.getKey())) {
                    throw new RuntimeException("数据重复");
                }
                proMap.put(stringObjectProperty.getKey(), stringObjectProperty.getValue());
            }
            startNodeVo.setProperties(proMap);
            nodeList.add(startNodeVo);
        }
        for(BasicNode node : nodeList){
            for(String label : labelNames){
                if(node.getLabels().contains(label)){
                    if (node.getProperties().get("类型") != null && !"-".equals(node.getProperties().get("type")) ){
                        result.get(label).add(node.getProperties().get("type").toString());
                    }
                }
            }
        }
        return result;
    }

    /**
     * 获取事件类知识中所有大分类和小分类的名称以及对应关系
     * 封装返回的数据格式：
     * {
     *     大分类1：[对应的小分类列表],
     *     大分类2: [对应的小分类列表],
     *     ……
     * }
     */
    public Map<String, Set<String>>  getEventCategories(){
        // 首先查询出事件类的所有大类别（标签名，除event外）
        Map<String , Set<String>> result = new HashMap<>();
        String cypherSql1 = "match (n:event) return distinct labels(n) as name";
        Result query1 = session.query(cypherSql1, new HashMap<>());
        HashSet<String> labelNames = new HashSet<>(); // 记录所有大分类名称
        for (Map<String, Object> map : query1.queryResults()) {
            String[] names = (String[]) map.get("name");
            for (String name : names) {
                labelNames.add(name);
            }
        }
        if(labelNames.contains("event")){
            labelNames.remove("event");
        }
        // 将所有事件类的标签名（除event）作为键，值为新建的HashSet集合，放入最终结果。
        for(String label : labelNames){
            result.put(label,new HashSet<>());
        }
        String cypherSql2 = "match (n:event) return n";
        Result query2 = session.query(cypherSql2, new HashMap<>());
        ArrayList<BasicNode> nodeList = new ArrayList<>();// 得到所有事件类节点信息组成的列表
        Iterable<Map<String, Object>> maps = query2.queryResults();
        for (Map<String, Object> map : maps) {
            NodeModel queryNode = (NodeModel) map.get("n");
            BasicNode startNodeVo = new BasicNode();
            startNodeVo.setId(queryNode.getId());
            startNodeVo.setLabels(Arrays.asList(queryNode.getLabels()));
            List<Property<String, Object>> propertyList = queryNode.getPropertyList();
            HashMap<String, Object> proMap = new HashMap<>();
            for (Property<String, Object> stringObjectProperty : propertyList) {
                if (proMap.containsKey(stringObjectProperty.getKey())) {
                    throw new RuntimeException("数据重复");
                }
                proMap.put(stringObjectProperty.getKey(), stringObjectProperty.getValue());
            }
            startNodeVo.setProperties(proMap);
            nodeList.add(startNodeVo);
        }
        for(BasicNode node : nodeList){
            for(String label : labelNames){
                if(node.getLabels().contains(label)){
                    if (node.getProperties().get("type") != null && !"-".equals(node.getProperties().get("type"))){
                        result.get(label).add(node.getProperties().get("type").toString());
                    }
                }
            }
        }
        return result;
    }

    /**
     * 获取模型类知识中所有大分类和小分类的名称以及对应关系
     * 封装返回的数据格式：
     * {
     *     大分类1：[对应的小分类列表],
     *     大分类2: [对应的小分类列表],
     *     ……
     * }
     */
    public Map<String, Set<String>>  getModelCategories(){
        // 首先查询出模型类的所有大类别（标签名，除model外）
        Map<String , Set<String>> result = new HashMap<>();
        String cypherSql1 = "match (n:model) return distinct labels(n) as name";
        Result query1 = session.query(cypherSql1, new HashMap<>());
        HashSet<String> labelNames = new HashSet<>(); // 记录所有大分类名称
        for (Map<String, Object> map : query1.queryResults()) {
            String[] names = (String[]) map.get("name");
            for (String name : names) {
                labelNames.add(name);
            }
        }
        if(labelNames.contains("model")){
            labelNames.remove("model");
        }
        // 将所有模型类的标签名（model）作为键，值为新建的HashSet集合，放入最终结果。
        for(String label : labelNames){
            result.put(label,new HashSet<>());
        }
        String cypherSql2 = "match (n:model) return n";
        Result query2 = session.query(cypherSql2, new HashMap<>());
        ArrayList<BasicNode> nodeList = new ArrayList<>();// 得到所有事件类节点信息组成的列表
        Iterable<Map<String, Object>> maps = query2.queryResults();
        for (Map<String, Object> map : maps) {
            NodeModel queryNode = (NodeModel) map.get("n");
            BasicNode startNodeVo = new BasicNode();
            startNodeVo.setId(queryNode.getId());
            startNodeVo.setLabels(Arrays.asList(queryNode.getLabels()));
            List<Property<String, Object>> propertyList = queryNode.getPropertyList();
            HashMap<String, Object> proMap = new HashMap<>();
            for (Property<String, Object> stringObjectProperty : propertyList) {
                if (proMap.containsKey(stringObjectProperty.getKey())) {
                    throw new RuntimeException("数据重复");
                }
                proMap.put(stringObjectProperty.getKey(), stringObjectProperty.getValue());
            }
            startNodeVo.setProperties(proMap);
            nodeList.add(startNodeVo);
        }
        for(BasicNode node : nodeList){
            for(String label : labelNames){
                if(node.getLabels().contains(label)){
                    if (node.getProperties().get("type") != null && !"-".equals(node.getProperties().get("type"))){
                        result.get(label).add(node.getProperties().get("type").toString());
                    }
                }
            }
        }
        return result;
    }

    /**
     * 获取策略树类知识中所有大分类和小分类的名称以及对应关系
     * 封装返回的数据格式：
     * {
     *     大分类1：[对应的小分类列表],
     *     大分类2: [对应的小分类列表],
     *     ……
     * }
     */
    public Map<String, Set<String>>  getTreeCategories(){
        // 首先查询出策略树类的所有大类别（标签名，除tree外）
        Map<String , Set<String>> result = new HashMap<>();
        String cypherSql1 = "match (n:tree) return distinct labels(n) as name";
        Result query1 = session.query(cypherSql1, new HashMap<>());
        HashSet<String> labelNames = new HashSet<>(); // 记录所有大分类名称
        for (Map<String, Object> map : query1.queryResults()) {
            String[] names = (String[]) map.get("name");
            for (String name : names) {
                labelNames.add(name);
            }
        }
        if(labelNames.contains("tree")){
            labelNames.remove("tree");
        }
        // 将所有策略树类的标签名（除tree）作为键，值为新建的HashSet集合，放入最终结果。
        for(String label : labelNames){
            result.put(label,new HashSet<>());
        }
        String cypherSql2 = "match (n:tree) return n";
        Result query2 = session.query(cypherSql2, new HashMap<>());
        ArrayList<BasicNode> nodeList = new ArrayList<>();// 得到所有策略树类节点信息组成的列表
        Iterable<Map<String, Object>> maps = query2.queryResults();
        for (Map<String, Object> map : maps) {
            NodeModel queryNode = (NodeModel) map.get("n");
            BasicNode startNodeVo = new BasicNode();
            startNodeVo.setId(queryNode.getId());
            startNodeVo.setLabels(Arrays.asList(queryNode.getLabels()));
            List<Property<String, Object>> propertyList = queryNode.getPropertyList();
            HashMap<String, Object> proMap = new HashMap<>();
            for (Property<String, Object> stringObjectProperty : propertyList) {
                if (proMap.containsKey(stringObjectProperty.getKey())) {
                    throw new RuntimeException("数据重复");
                }
                proMap.put(stringObjectProperty.getKey(), stringObjectProperty.getValue());
            }
            startNodeVo.setProperties(proMap);
            nodeList.add(startNodeVo);
        }
        for(BasicNode node : nodeList){
            for(String label : labelNames){
                if(node.getLabels().contains(label)){
                    if (node.getProperties().get("type") != null && !"-".equals(node.getProperties().get("type"))){
                        result.get(label).add(node.getProperties().get("type").toString());
                    }
                }
            }
        }
        return result;
    }

    /**
     * 给知识库中的节点添加属性键值对
     * @param nodeName 要添加属性对的节点名称
     * @param prop 要添加的属性键值对
     * @return 该节点添加的属性条数
     */
    public int addPropertyToNode(String nodeName, Map<String, String> prop){
        String cypherSql = "match (n{name:'" + nodeName + "'}) set ";
        for (String proName : prop.keySet()){
            cypherSql += "n.`" + proName + "`='"+prop.get(proName) +"',";
        }
        cypherSql = cypherSql.substring(0,cypherSql.length()-1);
        System.out.println(cypherSql);
        Result query = session.query(cypherSql, new HashMap<>());
        session.clear();
        return query.queryStatistics().getPropertiesSet();
    }


    /**
     * 设置节点的属性值。
     * @param nodeName 要设置属性的节点名称
     * @param properties 要给节点设置的属性键值对
     * @return 该节点设置的属性条数
     */
    public int setNodeProperty(String nodeName, Map<String, Object> properties){
        String cypherSql = "match (n{name:'" + nodeName + "'}) set ";
        for (String proName : properties.keySet()){
            cypherSql += "n.`" + proName + "`='"+properties.get(proName).toString() +"',";
        }
        cypherSql = cypherSql.substring(0,cypherSql.length()-1);
        System.out.println(cypherSql);
        Result query = session.query(cypherSql, new HashMap<>());
        session.clear();
        return query.queryStatistics().getPropertiesSet();
    }

    /**
     * 删除知识库中某个节点的属性
     * @param nodeName 要删除属性的节点名称
     * @param prop 要删除的属性字段名
     * @return 删除的属性个数
     */
    public int removePropertyFromNode(String nodeName, List<String> prop){
        String cypherSql = "match (n{name:'" + nodeName +"'}) remove ";
        for(String propName : prop){
            cypherSql = cypherSql + "n.`" + propName + "`,";
        }
        cypherSql = cypherSql.substring(0,cypherSql.length() - 1);
        System.out.println(cypherSql);
        Result query = session.query(cypherSql, new HashMap<>());
        session.clear();
        return query.queryStatistics().getPropertiesSet();
    }

    /**
     * 根据传入的类别列表获取每个类别的节点个数
     * @param category 类别列表，类别指的是大类别，即知识库中的标签
     * @return 传入的类别在知识库中含有的节点个数
     */
    public Map<String,Long> getCountByCategory(List<String> category){
        System.out.println(category);
        Iterator<String> iterator = category.iterator();
        // 这种拼接方法当分类数过多时，neo4j执行太过缓慢，因此对category以5个为一组，进行分组查询
        Map<String,Long> result = new HashMap<>();
        for (int i = 0; i < category.size(); i++) {
            String MatchSql = "match (a0:"+category.get(i)+")return count(a0) as a0";
            Result query = session.query(MatchSql, new HashMap<>());
            for(Map query_count:query.queryResults()){
                result.put(category.get(i),(Long)query_count.get("a0"));
            }
        }
        /*List<List<String>> categories = new ArrayList<>();
        List<String> category_small = new ArrayList<>(); // 记录小分组
        int cateCount = 1;
        while(iterator.hasNext()){
            category_small.add(iterator.next());
            if(cateCount == 5){
                categories.add(category_small);
                category_small = new ArrayList<>();
                cateCount = 1;
            }
            cateCount++;
        }
        if(category_small.size()!= 0){
            categories.add(category_small);
        }
        System.out.println("==s"+categories.toString());
        for(List<String> categories_small : categories ){
            String cypherSql ="";
            String matchSql = "match (";
            String returnSql = " return ";
            for(int i = 0; i < categories_small.size(); i++){
                matchSql = matchSql + "a" + i + ":" + categories_small.get(i) + "),(" ;
                returnSql = returnSql + "count(distinct(a"+i+")) as a"+i+",";
            }
            cypherSql =matchSql.substring(0,matchSql.length()-2).concat(returnSql.substring(0,returnSql.length()-1));
            System.out.println(cypherSql);
            Result query = session.query(cypherSql, new HashMap<>());
            for(int i = 0; i < categories_small.size();i++){
                for(Map query_count:query.queryResults()){
                    result.put(categories_small.get(i),(Long)query_count.get("a"+i));
                }
            }
        }*/
        System.out.println("yuan"+result);
        return result;
    }

    @Override
    public Map<String, Long> getCountByCategorySph() {
        Map<String,Long> result = new HashMap<>();
        String matchSql = "match (a0:entity)return count(a0) as a0";
        Result query = session.query(matchSql, new HashMap<>());
        for(Map query_count:query.queryResults()){
            result.put("entity",(Long)query_count.get("a0"));
        }
        String matchSql2 = "match (a0:event)return count(a0) as a0";
        Result query2 = session.query(matchSql2, new HashMap<>());
        for(Map query_count:query2.queryResults()){
            result.put("event",(Long)query_count.get("a0"));
        }
        String matchSql3 = "match (a0:model)return count(a0) as a0";
        Result query3 = session.query(matchSql3, new HashMap<>());
        for(Map query_count:query3.queryResults()){
            result.put("model",(Long)query_count.get("a0"));
        }
        String matchSql4 = "match (a0:tree)return count(a0) as a0";
        Result query4 = session.query(matchSql4, new HashMap<>());
        for(Map query_count:query4.queryResults()){
            result.put("tree",(Long)query_count.get("a0"));
        }
        System.out.println("xianzai"+result);
        return result;
    }

    /**
     * 通过名称查询节点
     * @param node
     * @return
     */
    public List<BasicNode> queryNodeByName(BasicNode node) {
        String cypherSql = "";
        //获取label
        String labels = "";
        if (node.getLabels() != null && node.getLabels().size() != 0) {
            labels = ":`" + String.join("`:`", node.getLabels()) + "`";
        }
        String name = "";
        if (node.getProperties() != null && node.getProperties().size() != 0) {
            Map<String, Object> maps = node.getProperties();
            Object str = maps.get("name");
            name = "{name:\"" + str + "\"}" ;
        }
        cypherSql = String.format("match(n%s%s) return n", labels, name);
//        System.out.println(cypherSql);
        Result query = session.query(cypherSql, new HashMap<>());
        ArrayList<BasicNode> nodeList = new ArrayList<>();
        Iterable<Map<String, Object>> maps = query.queryResults();
        for (Map<String, Object> map : maps) {
            NodeModel queryNode = (NodeModel) map.get("n");
            BasicNode startNodeVo = new BasicNode();
            startNodeVo.setId(queryNode.getId());
            startNodeVo.setLabels(Arrays.asList(queryNode.getLabels()));
            List<Property<String, Object>> propertyList = queryNode.getPropertyList();
            HashMap<String, Object> proMap = new HashMap<>();
            for (Property<String, Object> stringObjectProperty : propertyList) {
                if (proMap.containsKey(stringObjectProperty.getKey())) {
                    throw new RuntimeException("数据重复");
                }
                proMap.put(stringObjectProperty.getKey(), stringObjectProperty.getValue());
            }
            startNodeVo.setProperties(proMap);
            nodeList.add(startNodeVo);
        }
        session.clear();
        return nodeList;
    }

    /**
     * 从json文件批量导入节点
     * @param filePath
     * @return
     */
    public Long loadNodeFromJson(String filePath){
        Long loadCount = 0L;
        String cypherSql = "CALL apoc.periodic.iterate(\n" +
                "   \"CALL apoc.load.json('"+filePath+"') YIELD value return value\",\n" +
                "   \"\n" +
                "     unwind {value} as row\n" +
                "     CALL apoc.create.nodes(row.labels,row.properties) yield node return node\",\n" +
                "   { batchSize:1000, \n" +
                "     iterateList:true, \n" +
                "     parallel:true\n" +
                "   }\n" +
                ");";
        System.out.println(cypherSql);
        Result query = session.query(cypherSql, new HashMap<>());
        Iterable<Map<String, Object>> maps = query.queryResults();
        for (Map<String, Object> map : maps) {
            loadCount += (Long) map.get("committedOperations");
        }
        session.clear();
        return loadCount;
    }

    /**
     * 给所有没有 kb_id 属性的节点设置 kb_id，用于数据隔离
     */
    @Override
    public void setKbIdForUntaggedNodes(String kbId) {
        String cypherSql = "MATCH (n) WHERE n.kb_id IS NULL SET n.kb_id = $kbId";
        Map<String, Object> params = new HashMap<>();
        params.put("kbId", kbId);
        session.query(cypherSql, params);
        session.clear();
    }

    /**
     * 根据节点ID更新节点属性（完全替换模式）
     * @param nodeId 节点ID
     * @param properties 要更新的属性键值对，必须包含name属性
     * @return 更新的属性条数
     */
    @Override
    public int updateNodePropertiesById(Long nodeId, Map<String, Object> properties,List<String> labels) {
        if (nodeId == null || properties == null || properties.isEmpty()) {
            return 0;
        }
        
        // 检查是否包含name属性
        if (!properties.containsKey("name")) {
            throw new IllegalArgumentException("属性中必须包含name字段");
        }
        
        Map<String, Object> params = new HashMap<>();
        params.put("nodeId", nodeId);
        params.put("newProperties", properties);
        params.put("newLabels", labels);
        // 使用完全替换模式：根据ID找到节点，设置新属性和新标签
        // SET n = $newProperties 会完全替换所有属性
        // apoc.create.setLabels 会完全替换所有标签
        String cypherSql = "MATCH (n) WHERE id(n) = $nodeId " +
        "SET n = $newProperties " +
        "WITH n " +
        "CALL apoc.create.setLabels(n, $newLabels) YIELD node " +
        "RETURN count(node)";
        
        System.out.println("执行Cypher语句: " + cypherSql);
        System.out.println("参数: " + params);
        
        Result query = session.query(cypherSql, params);
        return properties.size(); // 返回设置的属性数量
    }

    /**
     * 根据节点名称和类型查询图数据
     * @param name 节点名称
     * @param type 查询类型：1-一跳关系，2-两跳关系，3-全部关系
     * @return 包含节点列表和关系列表的Map
     */
    @Override
    public Map<String, Object> queryGraphByNameAndType(String name, Integer type) {
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> params = new HashMap<>();
        params.put("name", name);
        
        // 使用Map去重
        Map<Long, BasicNode> nodeMap = new HashMap<>();
        Map<Long, Map<String, Object>> relationMap = new HashMap<>();
        
        if (type == 1) {
            // 类型1：返回该节点为起始节点的直接关系和终止节点（一跳出边，限制10条防止渲染爆炸）
            String cypherSql = "MATCH path = (start {name: $name})-[r]->(end) RETURN path LIMIT 10";
            System.out.println("执行Cypher语句: " + cypherSql);
            System.out.println("参数: " + params);
            
            Result query = session.query(cypherSql, params);
            processPathResults(query.queryResults(), nodeMap, relationMap);
            
        } else if (type == 2) {
            // 类型2：返回该节点为起始节点的两跳关系（两跳出边）
            String cypherSql = "MATCH path = (start {name: $name})-[*1..2]->(end) RETURN path";
            System.out.println("执行Cypher语句: " + cypherSql);
            System.out.println("参数: " + params);
            
            Result query = session.query(cypherSql, params);
            processPathResults(query.queryResults(), nodeMap, relationMap);
            
        } else if (type == 3) {
            // 类型3：返回所有相关关系（包括入边和出边，每一层每一个）
            // 分别查询出边和入边，然后合并
            
            // 查询出边（从该节点出发的所有路径）
            String outboundSql = "MATCH path = (node {name: $name})-[*]->(other) RETURN path";
            System.out.println("执行出边Cypher语句: " + outboundSql);
            System.out.println("参数: " + params);
            
            Result outboundQuery = session.query(outboundSql, params);
            processPathResults(outboundQuery.queryResults(), nodeMap, relationMap);
            
            // 查询入边（到该节点的所有路径）
            String inboundSql = "MATCH path = (other)-[*]->(node {name: $name}) RETURN path";
            System.out.println("执行入边Cypher语句: " + inboundSql);
            System.out.println("参数: " + params);
            
            Result inboundQuery = session.query(inboundSql, params);
            processPathResults(inboundQuery.queryResults(), nodeMap, relationMap);
            
        } else {
            throw new IllegalArgumentException("type参数必须为1、2或3");
        }
        
        List<BasicNode> nodes = new ArrayList<>(nodeMap.values());
        List<Map<String, Object>> relations = new ArrayList<>(relationMap.values());
        
        result.put("nodes", nodes);
        result.put("relations", relations);
        
        session.clear();
        return result;
    }
    
    /**
     * 处理path格式的查询结果
     */
    private void processPathResults(Iterable<Map<String, Object>> maps, 
                                    Map<Long, BasicNode> nodeMap, 
                                    Map<Long, Map<String, Object>> relationMap) {
        for (Map<String, Object> map : maps) {
            List<Path.Segment> segments = extractSegments(map.get("path"));
            for (Path.Segment segment : segments) {
                // 处理起始节点
                org.neo4j.driver.types.Node startNode = segment.start();
                if (!nodeMap.containsKey(startNode.id())) {
                    BasicNode basicNode = new BasicNode();
                    basicNode.setId(startNode.id());
                    List<String> labelList = new ArrayList<>();
                    for (String label : startNode.labels()) {
                        labelList.add(label);
                    }
                    basicNode.setLabels(labelList);
                    basicNode.setProperties(startNode.asMap());
                    nodeMap.put(startNode.id(), basicNode);
                }
                
                // 处理结束节点
                org.neo4j.driver.types.Node endNode = segment.end();
                if (!nodeMap.containsKey(endNode.id())) {
                    BasicNode basicNode = new BasicNode();
                    basicNode.setId(endNode.id());
                    List<String> labelList = new ArrayList<>();
                    for (String label : endNode.labels()) {
                        labelList.add(label);
                    }
                    basicNode.setLabels(labelList);
                    basicNode.setProperties(endNode.asMap());
                    nodeMap.put(endNode.id(), basicNode);
                }
                
                // 处理关系
                org.neo4j.driver.types.Relationship rel = segment.relationship();
                if (!relationMap.containsKey(rel.id())) {
                    Map<String, Object> relationData = new HashMap<>();
                    relationData.put("id", rel.id());
                    relationData.put("type", rel.type());
                    relationData.put("startNodeId", rel.startNodeId());
                    relationData.put("endNodeId", rel.endNodeId());
                    relationData.put("properties", rel.asMap());
                    relationMap.put(rel.id(), relationData);
                }
            }
        }
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
