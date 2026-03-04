package com.xidian.kg.service.Impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xidian.kg.controller.util.Result;
import com.xidian.kg.entity.BasicNode;
import com.xidian.kg.entity.BasicRelationReturnVO;
import com.xidian.kg.entity.QueryRelation;
import com.xidian.kg.service.KnowledgeBaseAdminService;
import com.xidian.kg.service.NodeService;
import com.xidian.kg.service.RelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class KnowledgeBaseAdminServiceImpl implements KnowledgeBaseAdminService {

    private static final Map<String, String> CATEGORY_LABEL_MAP = new HashMap<>();
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    static {
        CATEGORY_LABEL_MAP.put("symptoms", "Symptom");
        CATEGORY_LABEL_MAP.put("check_steps", "CheckStep");
        CATEGORY_LABEL_MAP.put("observations", "Observation");
        CATEGORY_LABEL_MAP.put("root_causes", "RootCause");
        CATEGORY_LABEL_MAP.put("recoveries", "Recovery");
    }

    @Value("${kb.fault.dropin.dir:/Users/ethanyuan/works/kb_tools/kb_inputs/fault-dropin}")
    private String faultDropinDir;

    @Value("${kb.fault.processed.dir:/Users/ethanyuan/works/kb_tools/kb_inputs/fault-dropin-processed}")
    private String faultProcessedDir;

    @Autowired
    private NodeService nodeService;

    @Autowired
    private RelationService relationService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Result importFaultKnowledgeFromDropinDir() {
        try {
            Path inDir = Paths.get(faultDropinDir);
            Path doneDir = Paths.get(faultProcessedDir);
            Files.createDirectories(inDir);
            Files.createDirectories(doneDir);

            List<Path> files = new ArrayList<>();
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(inDir, "*.json")) {
                for (Path p : stream) {
                    if (Files.isRegularFile(p)) {
                        files.add(p);
                    }
                }
            }
            files.sort(Comparator.comparing(Path::getFileName));

            Map<String, Object> summary = new LinkedHashMap<>();
            List<Map<String, Object>> perFile = new ArrayList<>();
            int totalNodes = 0;
            int totalRelations = 0;

            for (Path file : files) {
                Map<String, Object> one = importSingleFaultJson(file, doneDir);
                perFile.add(one);
                totalNodes += ((Number) one.getOrDefault("nodesImported", 0)).intValue();
                totalRelations += ((Number) one.getOrDefault("relationsImported", 0)).intValue();
            }

            summary.put("dropinDir", inDir.toString());
            summary.put("processedDir", doneDir.toString());
            summary.put("filesFound", files.size());
            summary.put("nodesImported", totalNodes);
            summary.put("relationsImported", totalRelations);
            summary.put("files", perFile);
            return new Result(true, summary);
        } catch (Exception e) {
            return new Result(false, "故障知识库目录导入失败: " + e.getMessage());
        }
    }

    private Map<String, Object> importSingleFaultJson(Path file, Path doneDir) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("file", file.getFileName().toString());
        int nodesImported = 0;
        int relationsImported = 0;

        try {
            JsonNode root = objectMapper.readTree(file.toFile());
            Map<String, String> idToName = new HashMap<>();

            JsonNode nodesNode = root.path("nodes");
            if (nodesNode.isObject()) {
                Iterator<String> groups = nodesNode.fieldNames();
                while (groups.hasNext()) {
                    String group = groups.next();
                    JsonNode arr = nodesNode.path(group);
                    if (!arr.isArray()) continue;

                    for (JsonNode n : arr) {
                        String kgId = text(n, "id");
                        String name = text(n, "name");
                        if (name == null || name.trim().isEmpty()) {
                            continue;
                        }
                        if (kgId != null && !kgId.isEmpty()) {
                            idToName.put(kgId, name);
                        }

                        if (existsNodeByName(name)) {
                            continue;
                        }

                        BasicNode node = new BasicNode();
                        List<String> labels = new ArrayList<>();
                        labels.add("entity");
                        labels.add(CATEGORY_LABEL_MAP.getOrDefault(group, group));
                        node.setLabels(labels);

                        Map<String, Object> props = objectMapper.convertValue(n, Map.class);
                        props.put("kg_id", kgId);
                        props.put("category", group);
                        props.put("type", CATEGORY_LABEL_MAP.getOrDefault(group, group));
                        node.setProperties(props);

                        Result create = nodeService.createNode(node);
                        if (create.isFlag()) {
                            nodesImported++;
                        }
                    }
                }
            }

            JsonNode rels = root.path("relationships");
            if (rels.isArray()) {
                for (JsonNode r : rels) {
                    String fromId = text(r, "from_id");
                    String toId = text(r, "to_id");
                    String fromName = idToName.get(fromId);
                    String toName = idToName.get(toId);
                    if (fromName == null || toName == null) {
                        continue;
                    }

                    BasicRelationReturnVO vo = new BasicRelationReturnVO();
                    BasicNode start = nodeByName(fromName);
                    BasicNode end = nodeByName(toName);
                    vo.setStart(start);
                    vo.setEnd(end);

                    QueryRelation queryRelation = new QueryRelation();
                    queryRelation.setType(text(r, "type"));
                    JsonNode propNode = r.path("properties");
                    if (propNode.isObject()) {
                        queryRelation.setProperties(objectMapper.convertValue(propNode, Map.class));
                    } else {
                        queryRelation.setProperties(new HashMap<>());
                    }
                    vo.setRelation(queryRelation);

                    Result relResult = relationService.createRelation(vo);
                    if (relResult.isFlag()) {
                        relationsImported++;
                    }
                }
            }

            Path target = doneDir.resolve(file.getFileName().toString().replace(".json", "") + "-" + TS.format(LocalDateTime.now()) + ".json");
            Files.move(file, target, StandardCopyOption.REPLACE_EXISTING);
            result.put("status", "imported");
            result.put("nodesImported", nodesImported);
            result.put("relationsImported", relationsImported);
            result.put("movedTo", target.toString());
        } catch (Exception e) {
            result.put("status", "failed");
            result.put("error", e.getMessage());
            result.put("nodesImported", nodesImported);
            result.put("relationsImported", relationsImported);
        }
        return result;
    }

    private boolean existsNodeByName(String name) {
        Result query = nodeService.queryNodeByName(name, true);
        if (!query.isFlag() || query.getData() == null) return false;
        if (!(query.getData() instanceof List)) return false;
        return !((List<?>) query.getData()).isEmpty();
    }

    private BasicNode nodeByName(String name) {
        BasicNode n = new BasicNode();
        Map<String, Object> p = new HashMap<>();
        p.put("name", name);
        n.setProperties(p);
        return n;
    }

    private String text(JsonNode node, String field) {
        JsonNode child = node.path(field);
        return child.isMissingNode() || child.isNull() ? null : child.asText();
    }
}
