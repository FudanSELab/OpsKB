package com.xidian.kg.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xidian.kg.controller.util.Result;
import com.xidian.kg.entity.BasicNode;
import com.xidian.kg.service.NodeService;
import com.xidian.kg.service.ExportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequestMapping("/node")
@ResponseBody
@Controller
@CrossOrigin
public class NodeController {

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private NodeService nodeService;
    
    @Autowired
    private ExportService exportService;

    /**
     * 获取模型类节点信息(包含所建流程图节点信息)
     * @param name 要查询节点名称
     * @return 查询的节点信息和流程图节点信息
     * @throws JsonProcessingException
     */
    @RequestMapping("/getModelNode")
    public String getModelNode(String name) throws JsonProcessingException {
        log.info("获取模型类节点信息，节点名称: {}", name);
        Result result = nodeService.getModelNode(name);
        log.info("获取模型类节点信息完成，结果: {}", result.isFlag() ? "成功" : "失败");
        return mapper.writeValueAsString(result);
    }

    /**
     * 根据类别查询节点
     * @param category_main： 主类别，即传感探测类资源知识，情报处理类资源知识等等
     * @param category_detail: 细分类别，即陆地传感探测装置，水下传感探测装置等等
     * @return 该类别下的所有节点信息（json字符串格式）
     */
    @RequestMapping("/queryNodeByCategory")
    public String queryNodeByCategory(String category_main,String category_detail) throws JsonProcessingException {
        log.info("根据类别查询节点，主类别: {}, 细分类别: {}", category_main, category_detail);
        Result result = nodeService.queryNodeByCategory(category_main, category_detail);
        log.info("根据类别查询节点完成，结果: {}", result.isFlag() ? "成功" : "失败");
        return mapper.writeValueAsString(result);
    }

    /**
     * 新建一个节点
     * @param node 节点
     * @return
     */
    @RequestMapping("/create")
    public String createNode(@RequestBody BasicNode node) throws JsonProcessingException {
        log.info("创建节点，节点信息: {}", node != null && node.getProperties() != null ? node.getProperties().get("name") : "null");
        Result result = nodeService.createNode(node);
        // 新增节点后执行数据导出
        if (result.isFlag()) {
            log.info("节点创建成功，开始导出数据");
            exportService.exportData();
        } else {
            log.warn("节点创建失败");
        }
        return mapper.writeValueAsString(result);
    }

    /**
     * 得到实体类所有的大类别和小类别，以及之间的对应关系
     * @return
     * @throws JsonProcessingException
     */
    @RequestMapping("/getEntityCategories")
    public String queryEntityCategories() throws JsonProcessingException {
        log.info("获取实体类所有类别");
        Result result = nodeService.queryEntityCategories();
        log.info("获取实体类所有类别完成，结果: {}", result.isFlag() ? "成功" : "失败");
        return mapper.writeValueAsString(result);
    }

    /**
     * 得到事件类所有的大类别和小类别，以及之间的对应关系
     * @return
     * @throws JsonProcessingException
     */
    @RequestMapping("/getEventCategories")
    public String queryEventCategories() throws JsonProcessingException {
        log.info("获取事件类所有类别");
        Result result = nodeService.queryEventCategories();
        log.info("获取事件类所有类别完成，结果: {}", result.isFlag() ? "成功" : "失败");
        return mapper.writeValueAsString(result);
    }

    /**
     * 得到模型类所有的大类别和小类别，以及之间的对应关系
     * @return
     * @throws JsonProcessingException
     */
    @RequestMapping("/getModelCategories")
    public String queryModelCategories() throws JsonProcessingException {
        log.info("获取模型类所有类别");
        Result result = nodeService.queryModelCategories();
        log.info("获取模型类所有类别完成，结果: {}", result.isFlag() ? "成功" : "失败");
        return mapper.writeValueAsString(result);
    }

    /**
     * 得到策略树类所有的大类别和小类别，以及之间的对应关系
     * @return
     * @throws JsonProcessingException
     */
    @RequestMapping("/getTreeCategories")
    public String queryTreeCategories() throws JsonProcessingException {
        log.info("获取策略树类所有类别");
        Result result = nodeService.queryTreeCategories();
        log.info("获取策略树类所有类别完成，结果: {}", result.isFlag() ? "成功" : "失败");
        return mapper.writeValueAsString(result);
    }

    /**
     * 根据节点名称查询节点信息
     * @param name: 节点名称
     * @param exactMatch: 是否精确匹配（可选参数，默认false表示模糊匹配，true表示全字符匹配）
     * @return
     */
    @RequestMapping("/queryByName")
    public String queryNodeByName(@RequestParam String name, 
                                  @RequestParam(required = false, defaultValue = "false") Boolean exactMatch) throws JsonProcessingException {
        log.info("根据节点名称查询节点信息，节点名称: {}, 精确匹配: {}", name, exactMatch);
        Result result = nodeService.queryNodeByName(name, exactMatch);
        log.info("根据节点名称查询节点信息完成，结果: {}", result.isFlag() ? "成功" : "失败");
        return mapper.writeValueAsString(result);
    }

    /**
     * 给知识库中的节点添加属性
     * @param map 前端传来的json格式数据，包含nodeName和properties两个键值对，nodeName表示要添加属性的节点名称
     *            properties表示要添加的属性键值对（可以有多个）
     * @return 是否添加成功封装的Result对象字符串
     * @throws JsonProcessingException
     */
    @RequestMapping("/addProperties")
    public String addPropertiesToNode(@RequestBody Map<String,Object> map) throws JsonProcessingException {
        if(map.get("nodeName") == null || map.get("properties") == null){
            log.warn("添加节点属性失败，请求数据格式错误");
            return mapper.writeValueAsString(new Result(false,"请求数据格式错误"));
        }
        String nodeName = map.get("nodeName").toString();
        Map<String,String> properties = (HashMap)map.get("properties");
        log.info("给节点添加属性，节点名称: {}, 属性数量: {}", nodeName, properties.size());
        Result result = nodeService.addPropertiesToNode(nodeName, properties);
        // 修改节点属性后执行数据导出
        if (result.isFlag()) {
            log.info("节点属性添加成功，开始导出数据");
            exportService.exportData();
        } else {
            log.warn("节点属性添加失败");
        }
        return mapper.writeValueAsString(result);
    }

    /**
     * 删除知识库中的节点属性
     * @param map 前端传来的json格式数据，包含nodeName和propName两个键值对，nodeName表示要删除属性的节点名称
     *            propName表示要删除的属性字段列表(可以有多个)
     * @return 是否删除成功封装的Result对象字符串
     * @throws JsonProcessingException
     */
    @RequestMapping("/removeProperties")
    public String removePropertiesFromNode(@RequestBody Map<String, Object> map) throws JsonProcessingException {
        if(map.get("nodeName") == null || map.get("propName") == null){
            log.warn("删除节点属性失败，请求数据格式错误");
            return mapper.writeValueAsString(new Result(false,"请求数据格式错误"));
        }
        String nodeName = map.get("nodeName").toString();
        List<String> propName = (ArrayList<String>)map.get("propName");
        log.info("删除节点属性，节点名称: {}, 属性数量: {}", nodeName, propName.size());
        Result result = nodeService.removePropertiesFromNode(nodeName, propName);
        // 删除节点属性后执行数据导出
        if (result.isFlag()) {
            log.info("节点属性删除成功，开始导出数据");
            exportService.exportData();
        } else {
            log.warn("节点属性删除失败");
        }
        return mapper.writeValueAsString(result);
    }

    /**
     * 设置节点的属性值
     * 注意：如果属性不存在则自动创建，如果存在则修改其值
     * @param map 前端传来的json格式数据，包含nodeName和properties两个键值对，nodeName表示要设置属性的节点名称
     *            properties表示要设置的属性键值对
     * @return 是否设置成功封装的Result对象字符串
     * @throws JsonProcessingException
     */
    @RequestMapping("/setNodeProperty")
    public String setNodeProerties(@RequestBody Map<String, Object> map) throws JsonProcessingException {
        if(map.get("nodeName") == null || map.get("properties") == null){
            log.warn("设置节点属性失败，请求数据格式错误");
            return mapper.writeValueAsString(new Result(false,"请求数据格式错误"));
        }
        String nodeName = map.get("nodeName").toString();
        Map<String,Object> properties = (HashMap)map.get("properties");
        log.info("设置节点属性，节点名称: {}, 属性数量: {}", nodeName, properties.size());
        Result result = nodeService.setNodeProperty(nodeName, properties);
        // 修改节点属性后执行数据导出
        if (result.isFlag()) {
            log.info("节点属性设置成功，开始导出数据");
            exportService.exportData();
        } else {
            log.warn("节点属性设置失败");
        }
        return mapper.writeValueAsString(result);
    }

    /**
     * 获得实体类下所有类别所对应的节点个数
     * @return 实体类下所有类别和节点个数键值对组成的map集合，封装后的Result对象字符串
     * @throws JsonProcessingException
     */
    @RequestMapping("/getEntityCountByCategory")
    public String getEntityCountByCategory() throws JsonProcessingException {
        log.info("获取实体类节点个数统计");
        Result entityCountByCategory = nodeService.getEntityCountByCategory();
        log.info("获取实体类节点个数统计完成，结果: {}", entityCountByCategory.isFlag() ? "成功" : "失败");
        return mapper.writeValueAsString(entityCountByCategory);
    }

    /**
     * 获得事件类下所有类别所对应的节点个数
     * @return 事件类下所有类别和节点个数键值对组成的map集合，封装后的Result对象字符串
     * @throws JsonProcessingException
     */
    @RequestMapping("/getEventCountByCategory")
    public String getEventCountByCategory() throws JsonProcessingException {
        log.info("获取事件类节点个数统计");
        Result eventCountByCategory = nodeService.getEventCountByCategory();
        log.info("获取事件类节点个数统计完成，结果: {}", eventCountByCategory.isFlag() ? "成功" : "失败");
        return mapper.writeValueAsString(eventCountByCategory);
    }

    /**
     * 获得模型类下所有类别所对应的节点个数
     * @return 模型类下所有类别和节点个数键值对组成的map集合，封装后的Result对象字符串
     * @throws JsonProcessingException
     */
    @RequestMapping("/getModelCountByCategory")
    public String getModelCountByCategory() throws JsonProcessingException {
        log.info("获取模型类节点个数统计");
        Result modelCountByCategory = nodeService.getModelCountByCategory();
        log.info("获取模型类节点个数统计完成，结果: {}", modelCountByCategory.isFlag() ? "成功" : "失败");
        return mapper.writeValueAsString(modelCountByCategory);
    }

    /**
     * 获得策略树类下所有类别所对应的节点个数
     * @return 策略树类下所有类别和节点个数键值对组成的map集合，封装后的Result对象字符串
     * @throws JsonProcessingException
     */
    @RequestMapping("/getTreeCountByCategory")
    public String getTreeCountByCategory() throws JsonProcessingException {
        log.info("获取策略树类节点个数统计");
        Result treeCountByCategory = nodeService.getTreeCountByCategory();
        log.info("获取策略树类节点个数统计完成，结果: {}", treeCountByCategory.isFlag() ? "成功" : "失败");
        return mapper.writeValueAsString(treeCountByCategory);
    }

    /**
     * 删除一个节点
     * @param node
     * @return
     * @throws JsonProcessingException
     */
    @RequestMapping("/delete")
    public String deleteNode(@RequestBody BasicNode node) throws JsonProcessingException {
        log.info("删除节点，节点信息: {}", node != null && node.getProperties() != null ? node.getProperties().get("name") : "null");
        Result result = nodeService.deleteNode(node);
        // 删除节点后执行数据导出
        if (result.isFlag()) {
            log.info("节点删除成功，开始导出数据");
            exportService.exportData();
        } else {
            log.warn("节点删除失败");
        }
        return mapper.writeValueAsString(result);
    }

    /**
     * 获取所有节点总数
     * @return
     * @throws JsonProcessingException
     */
    @RequestMapping("/getNodeTotal")
    public String getNodeTotal() throws JsonProcessingException {
        log.info("获取所有节点总数");
        Result result = nodeService.getNodeTotal();
        log.info("获取所有节点总数完成，结果: {}", result.isFlag() ? "成功" : "失败");
        return mapper.writeValueAsString(result);
    }

    @RequestMapping("/getNodeCountByCategory")
    public String getNodeCountByCategory() throws JsonProcessingException {
        log.info("获取各类别节点个数统计");
        Result nodeCountByCategory = nodeService.getNodeCountByCategory();
        log.info("获取各类别节点个数统计完成，结果: {}", nodeCountByCategory.isFlag() ? "成功" : "失败");
        return mapper.writeValueAsString(nodeCountByCategory);
    }

    /**
     * 根据节点ID更新节点属性（完全替换模式）
     * 注意：此方法会完全替换节点的所有属性，未提供的属性将被删除
     * @param map 前端传来的json格式数据，包含nodeId和properties两个键值对
     *            nodeId表示要更新的节点ID，properties表示要设置的属性键值对（必须包含name属性）
     * @return 是否更新成功封装的Result对象字符串
     * @throws JsonProcessingException
     */
    @RequestMapping("/updatePropertiesById")
    public String updateNodePropertiesById(@RequestBody Map<String, Object> map) throws JsonProcessingException {
        if (map.get("nodeId") == null || map.get("properties") == null) {
            log.warn("根据ID更新节点属性失败，请求数据格式错误");
            return mapper.writeValueAsString(new Result(false, "请求数据格式错误，需要包含nodeId和properties字段"));
        }
        
        Long nodeId;
        try {
            // 处理nodeId可能是String或Number类型的情况
            Object nodeIdObj = map.get("nodeId");
            if (nodeIdObj instanceof Number) {
                nodeId = ((Number) nodeIdObj).longValue();
            } else if (nodeIdObj instanceof String) {
                nodeId = Long.parseLong((String) nodeIdObj);
            } else {
                log.warn("根据ID更新节点属性失败，nodeId格式错误");
                return mapper.writeValueAsString(new Result(false, "nodeId格式错误，必须是数字"));
            }
        } catch (NumberFormatException e) {
            log.warn("根据ID更新节点属性失败，nodeId格式错误: {}", e.getMessage());
            return mapper.writeValueAsString(new Result(false, "nodeId格式错误，必须是有效的数字"));
        }
        
        Map<String, Object> properties = (HashMap<String, Object>) map.get("properties");
        List<String> labels = (ArrayList<String>) map.get("labels");
        log.info("根据ID更新节点属性，节点ID: {}, 属性数量: {}", nodeId, properties.size());
        Result result = nodeService.updateNodePropertiesById(nodeId, properties,labels);
        
        // 更新成功后执行数据导出
        if (result.isFlag()) {
            log.info("节点属性更新成功，开始导出数据");
            exportService.exportData();
        } else {
            log.warn("节点属性更新失败");
        }
        
        return mapper.writeValueAsString(result);
    }

    /**
     * 根据节点名称和类型查询图数据
     * @param name 节点名称
     * @param type 查询类型：1-一跳关系，2-两跳关系，3-全部关系
     * @return 包含节点列表和关系列表的Result对象字符串
     * @throws JsonProcessingException
     */
    @RequestMapping("/queryGraph")
    public String queryGraphByNameAndType(@RequestParam String name, @RequestParam Integer type) throws JsonProcessingException {
        log.info("查询图数据，节点名称: {}, 查询类型: {}", name, type);
        Result result = nodeService.queryGraphByNameAndType(name, type);
        log.info("查询图数据完成，结果: {}", result.isFlag() ? "成功" : "失败");
        return mapper.writeValueAsString(result);
    }

}
