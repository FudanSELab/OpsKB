package com.xidian.kg.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xidian.kg.controller.util.Result;
import com.xidian.kg.entity.BasicNode;
import com.xidian.kg.entity.BasicRelationReturnVO;
import com.xidian.kg.service.RelationService;
import com.xidian.kg.service.ExportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@Controller
@ResponseBody
@RequestMapping("/relation")
@CrossOrigin
public class RelationController {
    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private RelationService relationService;
    
    @Autowired
    private ExportService exportService;

    /**
     * 新建关系
     * @param basicRelationReturnVO
     * @return
     * @throws JsonProcessingException
     */
    @RequestMapping("/create")
    public String createRelation(@RequestBody BasicRelationReturnVO basicRelationReturnVO) throws JsonProcessingException {
        String startNodeName = null;
        String endNodeName = null;
        String relationType = null;
        if (basicRelationReturnVO != null) {
            if (basicRelationReturnVO.getStart() != null && basicRelationReturnVO.getStart().getProperties() != null) {
                startNodeName = (String) basicRelationReturnVO.getStart().getProperties().get("name");
            }
            if (basicRelationReturnVO.getEnd() != null && basicRelationReturnVO.getEnd().getProperties() != null) {
                endNodeName = (String) basicRelationReturnVO.getEnd().getProperties().get("name");
            }
            if (basicRelationReturnVO.getRelation() != null) {
                relationType = basicRelationReturnVO.getRelation().getType();
            }
        }
        log.info("创建关系，起始节点: {}, 关系类型: {}, 目标节点: {}", startNodeName, relationType, endNodeName);
        Result result = relationService.createRelation(basicRelationReturnVO);
        // 新增关系后执行数据导出
        if (result.isFlag()) {
            log.info("关系创建成功，开始导出数据");
            exportService.exportData();
        } else {
            log.warn("关系创建失败");
        }
        return mapper.writeValueAsString(result);
    }

    /**
     * 删除关系
     * @param basicRelationReturnVO
     * @return
     * @throws JsonProcessingException
     */
    @RequestMapping("/delete")
    public String deleteRelation(@RequestBody BasicRelationReturnVO basicRelationReturnVO) throws JsonProcessingException {
        String startNodeName = null;
        String endNodeName = null;
        String relationType = null;
        if (basicRelationReturnVO != null) {
            if (basicRelationReturnVO.getStart() != null && basicRelationReturnVO.getStart().getProperties() != null) {
                startNodeName = (String) basicRelationReturnVO.getStart().getProperties().get("name");
            }
            if (basicRelationReturnVO.getEnd() != null && basicRelationReturnVO.getEnd().getProperties() != null) {
                endNodeName = (String) basicRelationReturnVO.getEnd().getProperties().get("name");
            }
            if (basicRelationReturnVO.getRelation() != null) {
                relationType = basicRelationReturnVO.getRelation().getType();
            }
        }
        log.info("删除关系，起始节点: {}, 关系类型: {}, 目标节点: {}", startNodeName, relationType, endNodeName);
        Result result = relationService.deleteRelation(basicRelationReturnVO);
        // 删除关系后执行数据导出
        if (result.isFlag()) {
            log.info("关系删除成功，开始导出数据");
            exportService.exportData();
        } else {
            log.warn("关系删除失败");
        }
        return mapper.writeValueAsString(result);
    }

    /**
     * 获取某一节点所有关系
     * @param basicNode
     * @return
     * @throws JsonProcessingException
     */
    @RequestMapping("/getNodeRelation")
    public String getNodeRelation(@RequestBody BasicNode basicNode) throws JsonProcessingException {
        log.info("获取节点所有关系，节点信息: {}", basicNode != null && basicNode.getProperties() != null ? basicNode.getProperties().get("name") : "null");
        Result result = relationService.getNodeRelation(basicNode);
        log.info("获取节点所有关系完成，结果: {}", result.isFlag() ? "成功" : "失败");
        return mapper.writeValueAsString(result);
    }


    /**
     * 修改关系属性值
     * @param basicRelationReturnVO
     * @return
     * @throws JsonProcessingException
     */
    @RequestMapping("/updateRelationProperties")
    public String updateRelationProperties(@RequestBody BasicRelationReturnVO basicRelationReturnVO) throws JsonProcessingException {
        String startNodeName = null;
        String endNodeName = null;
        String relationType = null;
        if (basicRelationReturnVO != null) {
            if (basicRelationReturnVO.getStart() != null && basicRelationReturnVO.getStart().getProperties() != null) {
                startNodeName = (String) basicRelationReturnVO.getStart().getProperties().get("name");
            }
            if (basicRelationReturnVO.getEnd() != null && basicRelationReturnVO.getEnd().getProperties() != null) {
                endNodeName = (String) basicRelationReturnVO.getEnd().getProperties().get("name");
            }
            if (basicRelationReturnVO.getRelation() != null) {
                relationType = basicRelationReturnVO.getRelation().getType();
            }
        }
        log.info("修改关系属性，起始节点: {}, 关系类型: {}, 目标节点: {}", startNodeName, relationType, endNodeName);
        Result result = relationService.updateRelationProperties(basicRelationReturnVO);
        // 修改关系属性后执行数据导出
        if (result.isFlag()) {
            log.info("关系属性修改成功，开始导出数据");
            exportService.exportData();
        } else {
            log.warn("关系属性修改失败");
        }
        return mapper.writeValueAsString(result);
    }
}
