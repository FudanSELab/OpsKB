package com.xidian.kg.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xidian.kg.controller.util.Result;
import com.xidian.kg.service.EsSearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 基于 Elasticsearch 的搜索控制器
 * 与现有基于 Neo4j 的 NodeController 并存，前端可以分别从两种后端检索数据。
 */
@Slf4j
@RequestMapping("/es")
@ResponseBody
@Controller
@CrossOrigin
public class EsSearchController {

    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private EsSearchService esSearchService;

    /**
     * 按关键字搜索节点
     * 示例：GET /es/search?keyword=雷达
     */
    @RequestMapping("/search")
    public String search(@RequestParam("keyword") String keyword) throws JsonProcessingException {
        log.info("Elasticsearch 搜索，关键字: {}", keyword);
        Result result = esSearchService.searchByKeyword(keyword);
        log.info("Elasticsearch 搜索完成，结果: {}", result.isFlag() ? "成功" : "失败");
        return mapper.writeValueAsString(result);
    }
}

