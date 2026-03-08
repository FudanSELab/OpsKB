package com.xidian.kg.service.Impl;

import com.xidian.kg.controller.util.Result;
import com.xidian.kg.entity.BasicNode;
import com.xidian.kg.entity.BasicRelationReturnVO;
import com.xidian.kg.service.KnowledgeBaseReadService;
import com.xidian.kg.service.MainService;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class KnowledgeBaseReadServiceImpl implements KnowledgeBaseReadService {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeBaseReadServiceImpl.class);
    private static final String DEFAULT_KB_ID = "fault-kb";
    private static final int ES_DEFAULT_LIMIT = 1000;
    private static final Set<String> PROMCOPILOT_ENTITY_LABELS = new HashSet<>(Arrays.asList(
            "container", "deployment", "namespace", "node", "pod", "replicaset", "service",
            "api", "statefulset", "metric", "label_value_pair"
    ));
    private static final Set<String> DEFAULT_NODE_LABELS = new HashSet<>(Arrays.asList("entity", "event", "model", "tree"));

    private enum SourceType {
        NEO4J,
        ES
    }

    private static class SourceDef {
        private final SourceType type;
        private final String esIndex;
        private final boolean readOnly;

        private SourceDef(SourceType type, String esIndex, boolean readOnly) {
            this.type = type;
            this.esIndex = esIndex;
            this.readOnly = readOnly;
        }
    }

    private static class EsPageResult {
        final List<BasicNode> nodes;
        final long total;
        EsPageResult(List<BasicNode> nodes, long total) {
            this.nodes = nodes;
            this.total = total;
        }
    }

    private static class KnowledgeBaseDef {
        private final String id;
        private final String name;
        private final SourceType defaultSource;
        private final Map<SourceType, SourceDef> sources;

        private KnowledgeBaseDef(String id, String name, SourceType defaultSource, Map<SourceType, SourceDef> sources) {
            this.id = id;
            this.name = name;
            this.defaultSource = defaultSource;
            this.sources = sources;
        }
    }

    private static class GraphData {
        private final List<BasicNode> nodes;
        private final List<BasicRelationReturnVO> relations;

        private GraphData(List<BasicNode> nodes, List<BasicRelationReturnVO> relations) {
            this.nodes = nodes;
            this.relations = relations;
        }
    }

    @Value("${kb.read.es.index1:kb_nodes_1}")
    private String esIndex1;

    @Value("${kb.read.es.index2:kb_nodes_2}")
    private String esIndex2;

    @Value("${kb.read.es.index3:kb_nodes_3}")
    private String esIndex3;

    @Autowired
    private MainService mainService;

    @Autowired(required = false)
    private RestHighLevelClient esClient;

    @Override
    public Result listKnowledgeBases() {
        List<Map<String, Object>> items = new ArrayList<>();
        for (KnowledgeBaseDef def : getKnowledgeBases()) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", def.id);
            item.put("name", def.name);
            item.put("defaultSelected", DEFAULT_KB_ID.equals(def.id));
            item.put("defaultStorageType", def.defaultSource.name());
            item.put("sourceType", def.defaultSource.name());

            List<String> availableSources = def.sources.keySet().stream()
                    .map(Enum::name)
                    .collect(Collectors.toList());
            item.put("availableSources", availableSources);

            Map<String, Object> sourceSettings = new HashMap<>();
            for (Map.Entry<SourceType, SourceDef> sourceEntry : def.sources.entrySet()) {
                SourceDef sourceDef = sourceEntry.getValue();
                Map<String, Object> sourceInfo = new HashMap<>();
                sourceInfo.put("readOnly", sourceDef.readOnly);
                if (sourceDef.esIndex != null) {
                    sourceInfo.put("index", sourceDef.esIndex);
                }
                sourceSettings.put(sourceEntry.getKey().name(), sourceInfo);
            }
            item.put("sourceSettings", sourceSettings);

            SourceDef defaultSource = def.sources.get(def.defaultSource);
            item.put("readOnly", defaultSource != null && defaultSource.readOnly);
            if (defaultSource != null && defaultSource.esIndex != null) {
                item.put("index", defaultSource.esIndex);
            }
            items.add(item);
        }
        return new Result(true, items);
    }

    @Override
    public Result getAll(String kbId, String storageType, Integer limit) {
        KnowledgeBaseDef kb = resolveKnowledgeBase(kbId);
        SourceDef source = resolveSource(kb, storageType);

        if (source.type == SourceType.NEO4J) {
            return buildNeo4jAllResult(kb, limit);
        }

        try {
            int esSize = (limit != null && limit > 0) ? limit : ES_DEFAULT_LIMIT;
            List<BasicNode> nodes = fetchEsNodes(source, null, null, esSize);
            List<List> payload = new ArrayList<>();
            payload.add(nodes);
            payload.add(new ArrayList<>());
            return new Result(true, payload);
        } catch (Exception e) {
            return esError(kb, source, "读取全量图谱数据失败", e);
        }
    }

    @Override
    public Result queryNodeByName(String kbId, String storageType, String name, Boolean exactMatch) {
        KnowledgeBaseDef kb = resolveKnowledgeBase(kbId);
        SourceDef source = resolveSource(kb, storageType);

        if (source.type == SourceType.NEO4J) {
            Result result = buildNeo4jAllResult(kb);
            if (!result.isFlag()) {
                return result;
            }
            GraphData graphData = extractGraphData(result);
            if (graphData == null) {
                return new Result(false, "Neo4j 数据格式错误");
            }
            List<BasicNode> matched = new ArrayList<>();
            for (BasicNode node : graphData.nodes) {
                String nodeName = getNodeName(node);
                if (nodeName == null) {
                    continue;
                }
                if (matchName(nodeName, name, exactMatch)) {
                    matched.add(node);
                }
            }
            return new Result(true, matched);
        }

        try {
            List<BasicNode> nodes = fetchEsNodes(source, name, exactMatch, 100);
            return new Result(true, nodes);
        } catch (Exception e) {
            return esError(kb, source, "ES 节点检索失败", e);
        }
    }

    @Override
    public Result queryNodeByCategory(String kbId, String storageType, String categoryMain, String categoryDetail, Integer page, Integer size) {
        KnowledgeBaseDef kb = resolveKnowledgeBase(kbId);
        SourceDef source = resolveSource(kb, storageType);

        if (source.type == SourceType.NEO4J) {
            Result result = buildNeo4jAllResult(kb);
            if (!result.isFlag()) {
                return result;
            }
            GraphData graphData = extractGraphData(result);
            if (graphData == null) {
                return new Result(false, "Neo4j 数据格式错误");
            }
            List<BasicNode> filtered = new ArrayList<>();
            for (BasicNode node : graphData.nodes) {
                if (matchCategory(node, categoryMain, categoryDetail)) {
                    filtered.add(node);
                }
            }
            return new Result(true, filtered);
        }

        // ES：先拉全量数据，用 Java matchCategory 过滤（保证与侧边栏分类一致），再分页返回给前端
        try {
            int pageNum = (page != null && page > 0) ? page : 1;
            int pageSize = (size != null && size > 0) ? size : 10;

            List<BasicNode> allNodes = fetchEsNodes(source, null, null, ES_DEFAULT_LIMIT);
            List<BasicNode> filtered = new ArrayList<>();
            for (BasicNode node : allNodes) {
                if (matchCategory(node, categoryMain, categoryDetail)) {
                    filtered.add(node);
                }
            }

            long total = filtered.size();
            int from = (pageNum - 1) * pageSize;
            List<BasicNode> pageNodes = filtered.subList(
                    Math.min(from, filtered.size()),
                    Math.min(from + pageSize, filtered.size())
            );

            Map<String, Object> data = new HashMap<>();
            data.put("nodes", pageNodes);
            data.put("total", total);
            data.put("page", pageNum);
            data.put("size", pageSize);
            return new Result(true, data);
        } catch (Exception e) {
            return esError(kb, source, "ES 分类查询失败", e);
        }
    }

    @Override
    public Result queryGraph(String kbId, String storageType, String name, Integer type) {
        KnowledgeBaseDef kb = resolveKnowledgeBase(kbId);
        SourceDef source = resolveSource(kb, storageType);

        if (source.type == SourceType.NEO4J) {
            Result allResult = buildNeo4jAllResult(kb);
            if (!allResult.isFlag()) {
                return allResult;
            }
            GraphData graphData = extractGraphData(allResult);
            if (graphData == null) {
                return new Result(false, "Neo4j 数据格式错误");
            }

            BasicNode target = null;
            for (BasicNode node : graphData.nodes) {
                String nodeName = getNodeName(node);
                if (matchName(nodeName, name, false)) {
                    target = node;
                    break;
                }
            }
            if (target == null) {
                Map<String, Object> empty = new HashMap<>();
                empty.put("nodes", new ArrayList<>());
                empty.put("relations", new ArrayList<>());
                empty.put("sourceType", "NEO4J");
                return new Result(true, empty);
            }

            Set<Long> nodeIds = new HashSet<>();
            nodeIds.add(target.getId());
            List<Map<String, Object>> relationPayload = new ArrayList<>();
            for (BasicRelationReturnVO rel : graphData.relations) {
                if (rel == null || rel.getStart() == null || rel.getEnd() == null) {
                    continue;
                }
                Long startId = rel.getStart().getId();
                Long endId = rel.getEnd().getId();
                if (!Objects.equals(startId, target.getId()) && !Objects.equals(endId, target.getId())) {
                    continue;
                }
                nodeIds.add(startId);
                nodeIds.add(endId);

                Map<String, Object> item = new HashMap<>();
                item.put("id", rel.getRelation() == null ? null : rel.getRelation().getId());
                item.put("type", rel.getRelation() == null ? null : rel.getRelation().getType());
                item.put("properties", rel.getRelation() == null ? new HashMap<>() : rel.getRelation().getProperties());
                item.put("startNodeId", startId);
                item.put("endNodeId", endId);
                relationPayload.add(item);
            }

            List<BasicNode> relatedNodes = graphData.nodes.stream()
                    .filter(node -> node.getId() != null && nodeIds.contains(node.getId()))
                    .collect(Collectors.toList());

            Map<String, Object> graph = new HashMap<>();
            graph.put("nodes", relatedNodes);
            graph.put("relations", relationPayload);
            graph.put("sourceType", "NEO4J");
            return new Result(true, graph);
        }

        try {
            List<BasicNode> nodes = fetchEsNodes(source, name, false, 100);
            Map<String, Object> graph = new HashMap<>();
            graph.put("nodes", nodes);
            graph.put("relations", new ArrayList<>());
            graph.put("sourceType", "ES");
            return new Result(true, graph);
        } catch (Exception e) {
            return esError(kb, source, "ES 图谱查询失败", e);
        }
    }

    @Override
    public Result getNodeRelation(String kbId, String storageType, BasicNode node) {
        KnowledgeBaseDef kb = resolveKnowledgeBase(kbId);
        SourceDef source = resolveSource(kb, storageType);

        if (source.type == SourceType.ES) {
            return new Result(true, new ArrayList<>());
        }

        Result allResult = buildNeo4jAllResult(kb);
        if (!allResult.isFlag()) {
            return allResult;
        }
        GraphData graphData = extractGraphData(allResult);
        if (graphData == null) {
            return new Result(false, "Neo4j 数据格式错误");
        }

        Long targetId = node == null ? null : node.getId();
        String targetName = node == null ? null : getNodeName(node);
        List<BasicRelationReturnVO> matched = new ArrayList<>();
        for (BasicRelationReturnVO rel : graphData.relations) {
            if (rel == null || rel.getStart() == null || rel.getEnd() == null) {
                continue;
            }
            boolean idMatched = targetId != null &&
                    (Objects.equals(rel.getStart().getId(), targetId) || Objects.equals(rel.getEnd().getId(), targetId));
            boolean nameMatched = targetName != null && !targetName.trim().isEmpty() &&
                    (Objects.equals(getNodeName(rel.getStart()), targetName) || Objects.equals(getNodeName(rel.getEnd()), targetName));
            if (idMatched || nameMatched) {
                matched.add(rel);
            }
        }
        List<List<BasicRelationReturnVO>> wrapped = new ArrayList<>();
        wrapped.add(matched);
        return new Result(true, wrapped);
    }

    @Override
    public Result getEntityCategories(String kbId, String storageType) {
        return categoriesByBaseLabel(kbId, storageType, "entity", new HashMap<>());
    }

    @Override
    public Result getEventCategories(String kbId, String storageType) {
        return categoriesByBaseLabel(kbId, storageType, "event", new HashMap<>());
    }

    @Override
    public Result getModelCategories(String kbId, String storageType) {
        return categoriesByBaseLabel(kbId, storageType, "model", new HashMap<>());
    }

    @Override
    public Result getTreeCategories(String kbId, String storageType) {
        return categoriesByBaseLabel(kbId, storageType, "tree", new HashMap<>());
    }

    @Override
    public Result getEntityCountByCategory(String kbId, String storageType) {
        return countByBaseLabel(kbId, storageType, "entity");
    }

    @Override
    public Result getEventCountByCategory(String kbId, String storageType) {
        return countByBaseLabel(kbId, storageType, "event");
    }

    @Override
    public Result getModelCountByCategory(String kbId, String storageType) {
        return countByBaseLabel(kbId, storageType, "model");
    }

    @Override
    public Result getTreeCountByCategory(String kbId, String storageType) {
        return countByBaseLabel(kbId, storageType, "tree");
    }

    @Override
    public Result getNodeCountByCategory(String kbId, String storageType) {
        KnowledgeBaseDef kb = resolveKnowledgeBase(kbId);
        SourceDef source = resolveSource(kb, storageType);

        try {
            Map<String, Long> counts = new HashMap<>();
            if (source.type == SourceType.ES) {
                // ES：所有文档均为 entity，按索引统计总数
                long entityTotal = 0L;
                for (long c : buildEsCountFromIndices(source).values()) {
                    entityTotal += c;
                }
                counts.put("entity", entityTotal);
                counts.put("event", 0L);
                counts.put("model", 0L);
                counts.put("tree", 0L);
            } else {
                List<BasicNode> allNodes = getNeo4jNodes(kb);
                counts.put("entity", countNodesInBaseCategory(kb, allNodes, "entity"));
                counts.put("event", countNodesInBaseCategory(kb, allNodes, "event"));
                counts.put("model", countNodesInBaseCategory(kb, allNodes, "model"));
                counts.put("tree", countNodesInBaseCategory(kb, allNodes, "tree"));
            }
            return new Result(true, counts);
        } catch (Exception e) {
            if (source.type == SourceType.ES) {
                return esError(kb, source, "ES 分类统计失败", e);
            }
            log.error("Neo4j 分类统计失败: kbId={}", kb.id, e);
            return new Result(false, "Neo4j 分类统计失败（" + e.getMessage() + "）");
        }
    }

    @Override
    public Result getNodeTotal(String kbId, String storageType) {
        KnowledgeBaseDef kb = resolveKnowledgeBase(kbId);
        SourceDef source = resolveSource(kb, storageType);

        if (source.type == SourceType.NEO4J) {
            try {
                return new Result(true, (long) getNeo4jNodes(kb).size());
            } catch (Exception e) {
                log.error("Neo4j 节点总数查询失败: kbId={}", kb.id, e);
                return new Result(false, "Neo4j 节点总数查询失败（" + e.getMessage() + "）");
            }
        }

        if (esClient == null) {
            return new Result(false, "Elasticsearch 客户端未配置");
        }
        try {
            SearchRequest request = new SearchRequest(parseEsIndices(source.esIndex));
            request.indicesOptions(IndicesOptions.lenientExpandOpen());
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            sourceBuilder.size(0);
            sourceBuilder.trackTotalHits(true);
            sourceBuilder.query(QueryBuilders.matchAllQuery());
            request.source(sourceBuilder);
            SearchResponse response = esClient.search(request, RequestOptions.DEFAULT);
            long total = response.getHits().getTotalHits() == null ? 0L : response.getHits().getTotalHits().value;
            return new Result(true, total);
        } catch (Exception e) {
            return esError(kb, source, "ES 节点总数查询失败", e);
        }
    }

    private Result categoriesByBaseLabel(String kbId, String storageType, String baseLabel, Map<String, Set<String>> defaultValue) {
        KnowledgeBaseDef kb = resolveKnowledgeBase(kbId);
        SourceDef source = resolveSource(kb, storageType);

        try {
            if (source.type == SourceType.ES) {
                // ES：分类直接由索引名决定（每个索引 = 一个 detail 分类），全部归在 "entity" 下
                // 避免 ES_DEFAULT_LIMIT 截断导致部分索引漏掉
                if (!"entity".equalsIgnoreCase(baseLabel)) {
                    return new Result(true, defaultValue); // ES 只有 entity 分类
                }
                Map<String, Set<String>> categories = buildEsCategoriesFromIndices(source);
                return new Result(true, categories.isEmpty() ? defaultValue : categories);
            }

            List<BasicNode> allNodes = getNeo4jNodes(kb);
            Map<String, Set<String>> categories = new HashMap<>();
            for (BasicNode node : allNodes) {
                if (!isNodeInBaseCategory(kb, node, baseLabel)) {
                    continue;
                }
                String bucket = firstNonBaseLabel(node, baseLabel);
                if (bucket == null) {
                    bucket = "default";
                }
                Set<String> details = categories.computeIfAbsent(bucket, k -> new LinkedHashSet<>());
                Object type = node.getProperties() == null ? null : node.getProperties().get("type");
                if (type != null) {
                    details.add(String.valueOf(type));
                }
            }
            return new Result(true, categories.isEmpty() ? defaultValue : categories);
        } catch (Exception e) {
            if (source.type == SourceType.ES) {
                return esError(kb, source, "ES 分类列表读取失败", e);
            }
            log.error("Neo4j 分类列表读取失败: kbId={}", kb.id, e);
            return new Result(false, "Neo4j 分类列表读取失败（" + e.getMessage() + "）");
        }
    }

    private Result countByBaseLabel(String kbId, String storageType, String baseLabel) {
        KnowledgeBaseDef kb = resolveKnowledgeBase(kbId);
        SourceDef source = resolveSource(kb, storageType);

        try {
            if (source.type == SourceType.ES) {
                if (!"entity".equalsIgnoreCase(baseLabel)) {
                    return new Result(true, new HashMap<>()); // ES 只有 entity 分类
                }
                Map<String, Long> counts = buildEsCountFromIndices(source);
                return new Result(true, counts);
            }

            List<BasicNode> allNodes = getNeo4jNodes(kb);
            Map<String, Long> counts = new HashMap<>();
            for (BasicNode node : allNodes) {
                if (!isNodeInBaseCategory(kb, node, baseLabel)) {
                    continue;
                }
                String bucket = firstNonBaseLabel(node, baseLabel);
                if (bucket == null) {
                    bucket = "default";
                }
                counts.put(bucket, counts.getOrDefault(bucket, 0L) + 1L);
            }
            return new Result(true, counts);
        } catch (Exception e) {
            if (source.type == SourceType.ES) {
                return esError(kb, source, "ES 分类统计读取失败", e);
            }
            log.error("Neo4j 分类统计读取失败: kbId={}", kb.id, e);
            return new Result(false, "Neo4j 分类统计读取失败（" + e.getMessage() + "）");
        }
    }

    private Result buildNeo4jAllResult(KnowledgeBaseDef kb) {
        return buildNeo4jAllResult(kb, null);
    }

    private Result buildNeo4jAllResult(KnowledgeBaseDef kb, Integer limit) {
        Result raw = mainService.getAllNodesAndRelations();
        if (raw == null || !raw.isFlag()) {
            return new Result(false, raw == null ? "Neo4j 数据读取失败" : raw.getData());
        }

        GraphData graphData = extractGraphData(raw);
        if (graphData == null) {
            return new Result(false, "Neo4j 数据格式错误");
        }

        List<BasicNode> filteredNodes = graphData.nodes.stream()
                .filter(node -> nodeBelongsToKb(kb.id, node))
                .collect(Collectors.toList());

        if (limit != null && limit > 0 && filteredNodes.size() > limit) {
            filteredNodes = filteredNodes.subList(0, limit);
        }

        Set<Long> nodeIds = filteredNodes.stream()
                .map(BasicNode::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<BasicRelationReturnVO> filteredRelations = graphData.relations.stream()
                .filter(rel -> rel != null && rel.getStart() != null && rel.getEnd() != null)
                .filter(rel -> nodeIds.contains(rel.getStart().getId()) && nodeIds.contains(rel.getEnd().getId()))
                .collect(Collectors.toList());

        List<List> payload = new ArrayList<>();
        payload.add(filteredNodes);
        payload.add(filteredRelations);
        return new Result(true, payload);
    }

    private GraphData extractGraphData(Result result) {
        if (result == null || !(result.getData() instanceof List)) {
            return null;
        }
        List<?> payload = (List<?>) result.getData();
        if (payload.size() < 2) {
            return null;
        }

        List<BasicNode> nodes = castList(payload.get(0), BasicNode.class);
        List<BasicRelationReturnVO> relations = castList(payload.get(1), BasicRelationReturnVO.class);
        return new GraphData(nodes, relations);
    }

    private <T> List<T> castList(Object source, Class<T> targetClass) {
        if (!(source instanceof List)) {
            return new ArrayList<>();
        }
        List<?> rawList = (List<?>) source;
        List<T> out = new ArrayList<>();
        for (Object item : rawList) {
            if (targetClass.isInstance(item)) {
                out.add(targetClass.cast(item));
            }
        }
        return out;
    }

    private List<BasicNode> getNeo4jNodes(KnowledgeBaseDef kb) {
        Result result = buildNeo4jAllResult(kb);
        if (!result.isFlag()) {
            return new ArrayList<>();
        }
        GraphData data = extractGraphData(result);
        return data == null ? new ArrayList<>() : data.nodes;
    }

    private boolean nodeBelongsToKb(String kbId, BasicNode node) {
        if (node == null || node.getProperties() == null) {
            return false;
        }
        Map<String, Object> props = node.getProperties();
        String marker = firstNonBlank(
                asString(props.get("kb_id")),
                asString(props.get("kbId")),
                asString(props.get("knowledge_base")),
                asString(props.get("knowledgeBase")),
                asString(props.get("kb")),
                asString(props.get("source_kb"))
        );

        if (marker == null) {
            // 兼容历史数据：PromCopilot 历史导入可能没有 kb_id，按标签兜底识别
            if ("promcopilot".equals(kbId) && node.getLabels() != null) {
                for (String label : node.getLabels()) {
                    if (label != null && PROMCOPILOT_ENTITY_LABELS.contains(label.toLowerCase(Locale.ROOT))) {
                        return true;
                    }
                }
            }
            // 未标注 kb_id 的节点不归入任何知识库，避免数据错乱
            return false;
        }

        String normalized = marker.trim().toLowerCase(Locale.ROOT);
        for (String alias : kbAliases(kbId)) {
            if (normalized.equals(alias)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断节点是否属于指定知识库下的某个“基础分类”（entity / event / model / tree）。
     * 对于 PromCopilot 的 Neo4j 数据，我们按 PROMCOPILOT_ENTITY_LABELS
     * 将这些节点视为 entity 类节点，方便前端统一按实体分类展示。
     */
    private boolean isNodeInBaseCategory(KnowledgeBaseDef kb, BasicNode node, String baseLabel) {
        if (node == null || baseLabel == null) {
            return false;
        }
        // PromCopilot 下的实体类节点：使用容器 / Pod 等标签兜底当作 entity
        if ("promcopilot".equals(kb.id) && "entity".equalsIgnoreCase(baseLabel)) {
            if (!nodeBelongsToKb(kb.id, node) || node.getLabels() == null) {
                return false;
            }
            for (String label : node.getLabels()) {
                if (label != null && PROMCOPILOT_ENTITY_LABELS.contains(label.toLowerCase(Locale.ROOT))) {
                    return true;
                }
            }
            return false;
        }
        // 其他情况沿用原有按标签匹配逻辑
        return hasLabel(node, baseLabel);
    }

    private long countNodesInBaseCategory(KnowledgeBaseDef kb, List<BasicNode> nodes, String baseLabel) {
        long count = 0;
        if (nodes == null || baseLabel == null) {
            return 0L;
        }
        for (BasicNode node : nodes) {
            if (isNodeInBaseCategory(kb, node, baseLabel)) {
                count++;
            }
        }
        return count;
    }

    private Set<String> kbAliases(String kbId) {
        Set<String> aliases = new HashSet<>();
        aliases.add(kbId);
        if ("fault-kb".equals(kbId)) {
            aliases.add("fault");
            aliases.add("故障知识库");
        } else if ("promcopilot".equals(kbId)) {
            aliases.add("prom");
            aliases.add("系统上下文知识库");
        } else if ("logcopilot".equals(kbId)) {
            aliases.add("log");
            aliases.add("日志知识库");
        }
        return aliases.stream().map(s -> s.toLowerCase(Locale.ROOT)).collect(Collectors.toSet());
    }

    private KnowledgeBaseDef resolveKnowledgeBase(String kbId) {
        String target = (kbId == null || kbId.trim().isEmpty()) ? DEFAULT_KB_ID : kbId.trim();
        for (KnowledgeBaseDef def : getKnowledgeBases()) {
            if (def.id.equals(target)) {
                return def;
            }
        }
        log.warn("未知知识库ID: {}, 回退到默认知识库: {}", kbId, DEFAULT_KB_ID);
        return getKnowledgeBases().stream()
                .filter(def -> DEFAULT_KB_ID.equals(def.id))
                .findFirst()
                .orElse(getKnowledgeBases().get(0));
    }

    private SourceDef resolveSource(KnowledgeBaseDef kb, String storageType) {
        if (storageType != null && !storageType.trim().isEmpty()) {
            try {
                SourceType requested = SourceType.valueOf(storageType.trim().toUpperCase(Locale.ROOT));
                SourceDef selected = kb.sources.get(requested);
                if (selected != null) {
                    return selected;
                }
            } catch (IllegalArgumentException ignored) {
                log.warn("未知存储类型: {}, kbId={}, 使用默认存储", storageType, kb.id);
            }
        }

        SourceDef fallback = kb.sources.get(kb.defaultSource);
        if (fallback != null) {
            return fallback;
        }
        return kb.sources.values().iterator().next();
    }

    private List<KnowledgeBaseDef> getKnowledgeBases() {
        return Arrays.asList(
                new KnowledgeBaseDef(
                        "fault-kb",
                        "故障知识库",
                        SourceType.NEO4J,
                        buildSources(esIndex1)
                ),
                new KnowledgeBaseDef(
                        "promcopilot",
                        "系统上下文知识库",
                        SourceType.NEO4J,
                        buildSources(esIndex2)
                ),
                new KnowledgeBaseDef(
                        "logcopilot",
                        "日志知识库",
                        SourceType.ES,
                        buildSources(esIndex3)
                )
        );
    }

    private Map<SourceType, SourceDef> buildSources(String esIndex) {
        Map<SourceType, SourceDef> sources = new LinkedHashMap<>();
        sources.put(SourceType.NEO4J, new SourceDef(SourceType.NEO4J, null, false));
        sources.put(SourceType.ES, new SourceDef(SourceType.ES, esIndex, true));
        return sources;
    }

    private Result esError(KnowledgeBaseDef kb, SourceDef source, String message, Exception e) {
        log.error("{}: kbId={}, index={}", message, kb.id, source.esIndex, e);
        return new Result(false, message + "（" + e.getMessage() + "）");
    }

    /**
     * ES 分类列表：每个索引名 = 一个 entity 子分类，采样获取 type 子项
     */
    private Map<String, Set<String>> buildEsCategoriesFromIndices(SourceDef source) throws IOException {
        if (esClient == null) {
            throw new IllegalStateException("Elasticsearch 客户端未配置");
        }
        String[] indices = parseEsIndices(source.esIndex);
        Map<String, Set<String>> categories = new LinkedHashMap<>();
        for (String index : indices) {
            Set<String> details = new LinkedHashSet<>();
            SearchRequest req = new SearchRequest(index);
            req.indicesOptions(IndicesOptions.lenientExpandOpen());
            SearchSourceBuilder sb = new SearchSourceBuilder();
            sb.size(500);
            sb.trackTotalHits(true);
            sb.query(QueryBuilders.matchAllQuery());
            req.source(sb);
            try {
                SearchResponse resp = esClient.search(req, RequestOptions.DEFAULT);
                long total = resp.getHits().getTotalHits() == null ? 0L : resp.getHits().getTotalHits().value;
                if (total == 0) {
                    continue; // 空索引跳过
                }
                for (SearchHit hit : resp.getHits().getHits()) {
                    Object type = hit.getSourceAsMap().get("type");
                    if (type != null && !String.valueOf(type).trim().isEmpty()) {
                        details.add(String.valueOf(type));
                    }
                }
                categories.put(index, details);
            } catch (Exception e) {
                log.warn("ES 索引 {} 分类采样失败，跳过: {}", index, e.getMessage());
            }
        }
        return categories;
    }

    /**
     * ES 分类计数：每个索引单独统计文档数
     */
    private Map<String, Long> buildEsCountFromIndices(SourceDef source) throws IOException {
        if (esClient == null) {
            throw new IllegalStateException("Elasticsearch 客户端未配置");
        }
        String[] indices = parseEsIndices(source.esIndex);
        Map<String, Long> counts = new LinkedHashMap<>();
        for (String index : indices) {
            SearchRequest req = new SearchRequest(index);
            req.indicesOptions(IndicesOptions.lenientExpandOpen());
            SearchSourceBuilder sb = new SearchSourceBuilder();
            sb.size(0);
            sb.trackTotalHits(true);
            sb.query(QueryBuilders.matchAllQuery());
            req.source(sb);
            try {
                SearchResponse resp = esClient.search(req, RequestOptions.DEFAULT);
                long total = resp.getHits().getTotalHits() == null ? 0L : resp.getHits().getTotalHits().value;
                if (total > 0) {
                    counts.put(index, total);
                }
            } catch (Exception e) {
                log.warn("ES 索引 {} 计数失败，跳过: {}", index, e.getMessage());
            }
        }
        return counts;
    }

    private EsPageResult fetchEsNodesPaged(SourceDef source, QueryBuilder query, int from, int size) throws IOException {
        if (esClient == null) {
            throw new IllegalStateException("Elasticsearch 客户端未配置");
        }
        SearchRequest request = new SearchRequest(parseEsIndices(source.esIndex));
        request.indicesOptions(IndicesOptions.lenientExpandOpen());
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.from(from);
        sourceBuilder.size(size);
        sourceBuilder.trackTotalHits(true);
        sourceBuilder.query(query != null ? query : QueryBuilders.matchAllQuery());
        request.source(sourceBuilder);
        SearchResponse response = esClient.search(request, RequestOptions.DEFAULT);
        long total = response.getHits().getTotalHits() == null ? 0L : response.getHits().getTotalHits().value;
        List<BasicNode> nodes = new ArrayList<>();
        for (SearchHit hit : response.getHits().getHits()) {
            nodes.add(toBasicNode(hit));
        }
        return new EsPageResult(nodes, total);
    }

    private QueryBuilder buildEsCategoryQuery(@Nullable String categoryMain, @Nullable String categoryDetail) {
        boolean hasMain = categoryMain != null && !categoryMain.trim().isEmpty();
        boolean hasDetail = categoryDetail != null && !categoryDetail.trim().isEmpty();
        if (!hasMain && !hasDetail) {
            return QueryBuilders.matchAllQuery();
        }
        BoolQueryBuilder outer = QueryBuilders.boolQuery();
        if (hasMain) {
            String cat = categoryMain.trim().toLowerCase(Locale.ROOT);
            BoolQueryBuilder f = QueryBuilders.boolQuery();
            f.should(QueryBuilders.termQuery("labels", cat));
            f.should(QueryBuilders.termQuery("labels.keyword", cat));
            // 兼容：ES 文档无 labels 字段时，Java 层默认将其归为 entity
            if ("entity".equals(cat)) {
                BoolQueryBuilder noLabels = QueryBuilders.boolQuery();
                noLabels.mustNot(QueryBuilders.existsQuery("labels"));
                f.should(noLabels);
            }
            f.minimumShouldMatch(1);
            outer.must(f);
        }
        if (hasDetail) {
            String cat = categoryDetail.trim().toLowerCase(Locale.ROOT);
            BoolQueryBuilder f = QueryBuilders.boolQuery();
            f.should(QueryBuilders.termQuery("labels", cat));
            f.should(QueryBuilders.termQuery("labels.keyword", cat));
            // 兼容：Java 层以索引名作为 detail 标签（如 openstack_templates）
            f.should(QueryBuilders.termQuery("_index", cat));
            f.minimumShouldMatch(1);
            outer.must(f);
        }
        return outer;
    }

    private List<BasicNode> fetchEsNodes(SourceDef source, @Nullable String keyword, @Nullable Boolean exactMatch, int size) throws IOException {
        if (esClient == null) {
            throw new IllegalStateException("Elasticsearch 客户端未配置");
        }

        SearchRequest request = new SearchRequest(parseEsIndices(source.esIndex));
        request.indicesOptions(IndicesOptions.lenientExpandOpen());
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.size(size);
        sourceBuilder.query(buildEsQuery(keyword, exactMatch));
        request.source(sourceBuilder);

        SearchResponse response = esClient.search(request, RequestOptions.DEFAULT);
        List<BasicNode> nodes = new ArrayList<>();
        for (SearchHit hit : response.getHits().getHits()) {
            nodes.add(toBasicNode(hit));
        }
        return nodes;
    }

    private QueryBuilder buildEsQuery(@Nullable String keyword, @Nullable Boolean exactMatch) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return QueryBuilders.matchAllQuery();
        }
        String trimmed = keyword.trim();
        if (Boolean.TRUE.equals(exactMatch)) {
            BoolQueryBuilder bool = QueryBuilders.boolQuery();
            bool.should(QueryBuilders.termQuery("name.keyword", trimmed));
            bool.should(QueryBuilders.termQuery("name", trimmed));
            bool.minimumShouldMatch(1);
            return bool;
        }

        BoolQueryBuilder bool = QueryBuilders.boolQuery();
        bool.should(QueryBuilders.matchQuery("name", trimmed).operator(Operator.AND));
        bool.should(QueryBuilders.matchPhrasePrefixQuery("name", trimmed));
        if (!trimmed.contains("*")) {
            bool.should(QueryBuilders.wildcardQuery("name.keyword", "*" + escapeWildcard(trimmed) + "*"));
        }
        bool.minimumShouldMatch(1);
        return bool;
    }

    private BasicNode toBasicNode(SearchHit hit) {
        Map<String, Object> source = hit.getSourceAsMap();
        BasicNode node = new BasicNode();
        node.setId(toStableLongId(hit.getId()));
        node.setLabels(extractLabels(source.get("labels"), hit.getIndex()));

        Map<String, Object> properties = new HashMap<>();
        Object propertyObj = source.get("properties");
        if (propertyObj instanceof Map) {
            properties.putAll((Map<String, Object>) propertyObj);
        }
        mergeTopLevelFields(properties, source);
        properties.put("es_doc_id", hit.getId());
        if (!properties.containsKey("name") || properties.get("name") == null || String.valueOf(properties.get("name")).trim().isEmpty()) {
            properties.put("name", resolveDisplayName(source, hit.getId()));
        }
        node.setProperties(properties);
        return node;
    }

    private String resolveDisplayName(Map<String, Object> source, String docId) {
        String name = firstNonBlank(
                asString(source.get("name")),
                asString(source.get("id")),
                asString(source.get("pattern")),
                asString(source.get("sequence_id"))
        );
        if (name != null) {
            return name;
        }

        String templateContent = asString(source.get("template_content"));
        if (templateContent != null && !templateContent.trim().isEmpty()) {
            return truncate(templateContent.trim(), 80);
        }

        String description = asString(source.get("description"));
        if (description != null && !description.trim().isEmpty()) {
            return truncate(description.trim(), 80);
        }

        return docId == null ? "unknown" : docId;
    }

    private String truncate(String text, int maxLen) {
        if (text == null) {
            return null;
        }
        if (text.length() <= maxLen) {
            return text;
        }
        return text.substring(0, maxLen) + "...";
    }

    private void mergeTopLevelFields(Map<String, Object> properties, Map<String, Object> source) {
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            String key = entry.getKey();
            if ("properties".equals(key) || "labels".equals(key)) {
                continue;
            }
            if (!properties.containsKey(key)) {
                properties.put(key, entry.getValue());
            }
        }
    }

    private List<String> extractLabels(Object labelsObj, String indexName) {
        List<String> labels = new ArrayList<>();
        if (labelsObj instanceof List) {
            for (Object o : (List<?>) labelsObj) {
                if (o != null) {
                    labels.add(String.valueOf(o));
                }
            }
        } else if (labelsObj instanceof String) {
            labels.add((String) labelsObj);
        }
        if (labels.isEmpty()) {
            labels.add("entity");
            String lower = indexName == null ? "" : indexName.toLowerCase(Locale.ROOT);
            if (!lower.isEmpty() && !DEFAULT_NODE_LABELS.contains(lower)) {
                labels.add(lower);
            }
        }
        return labels;
    }

    private long toStableLongId(String id) {
        if (id == null) {
            return 0L;
        }
        try {
            return Long.parseLong(id);
        } catch (NumberFormatException ignored) {
            int hash = id.hashCode();
            if (hash == Integer.MIN_VALUE) {
                return (long) Integer.MAX_VALUE;
            }
            return Math.abs((long) hash);
        }
    }

    private String[] parseEsIndices(String esIndex) {
        if (esIndex == null || esIndex.trim().isEmpty()) {
            return new String[]{"kb_nodes_2"};
        }
        String[] parts = esIndex.split(",");
        List<String> indices = new ArrayList<>();
        for (String part : parts) {
            String trimmed = part == null ? "" : part.trim();
            if (!trimmed.isEmpty()) {
                indices.add(trimmed);
            }
        }
        if (indices.isEmpty()) {
            indices.add("kb_nodes_2");
        }
        return indices.toArray(new String[0]);
    }

    private boolean matchCategory(BasicNode node, String categoryMain, String categoryDetail) {
        if (categoryMain != null && !categoryMain.trim().isEmpty() && !hasLabel(node, categoryMain)) {
            return false;
        }
        if (categoryDetail != null && !categoryDetail.trim().isEmpty() && !hasLabel(node, categoryDetail)) {
            return false;
        }
        return true;
    }

    private boolean hasLabel(BasicNode node, String label) {
        if (node == null || node.getLabels() == null || label == null) {
            return false;
        }
        for (String item : node.getLabels()) {
            if (label.equalsIgnoreCase(item)) {
                return true;
            }
        }
        return false;
    }

    private String firstNonBaseLabel(BasicNode node, String baseLabel) {
        if (node == null || node.getLabels() == null) {
            return null;
        }
        for (String label : node.getLabels()) {
            if (label == null) {
                continue;
            }
            if (!label.equalsIgnoreCase(baseLabel)) {
                return label;
            }
        }
        return null;
    }

    private long countNodesWithLabel(List<BasicNode> nodes, String label) {
        long count = 0;
        for (BasicNode node : nodes) {
            if (hasLabel(node, label)) {
                count++;
            }
        }
        return count;
    }

    private boolean matchName(String nodeName, String query, Boolean exactMatch) {
        if (nodeName == null || query == null) {
            return false;
        }
        if (Boolean.TRUE.equals(exactMatch)) {
            return nodeName.equalsIgnoreCase(query.trim());
        }
        return nodeName.toLowerCase(Locale.ROOT).contains(query.trim().toLowerCase(Locale.ROOT));
    }

    private String getNodeName(BasicNode node) {
        if (node == null || node.getProperties() == null) {
            return null;
        }
        Object name = node.getProperties().get("name");
        return name == null ? null : String.valueOf(name);
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value;
            }
        }
        return null;
    }

    private String escapeWildcard(String value) {
        return value.replace("\\", "\\\\")
                .replace("*", "\\*")
                .replace("?", "\\?");
    }
}
