package com.xidian.kg.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xidian.kg.controller.util.Result;
import com.xidian.kg.entity.BasicNode;
import com.xidian.kg.service.KnowledgeBaseAdminService;
import com.xidian.kg.service.KnowledgeBaseReadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@ResponseBody
@CrossOrigin
@RequestMapping("/knowledgebase")
public class KnowledgeBaseController {

    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private KnowledgeBaseReadService knowledgeBaseReadService;

    @Autowired
    private KnowledgeBaseAdminService knowledgeBaseAdminService;

    @RequestMapping("/list")
    public String listKnowledgeBases() throws JsonProcessingException {
        return mapper.writeValueAsString(knowledgeBaseReadService.listKnowledgeBases());
    }

    @RequestMapping("/getAll")
    public String getAll(@RequestParam(required = false) String kbId,
                         @RequestParam(required = false) String storage,
                         @RequestParam(required = false) Integer limit) throws JsonProcessingException {
        return mapper.writeValueAsString(knowledgeBaseReadService.getAll(kbId, storage, limit));
    }

    @RequestMapping("/queryByName")
    public String queryByName(@RequestParam String name,
                              @RequestParam(required = false, defaultValue = "false") Boolean exactMatch,
                              @RequestParam(required = false) String kbId,
                              @RequestParam(required = false) String storage) throws JsonProcessingException {
        return mapper.writeValueAsString(knowledgeBaseReadService.queryNodeByName(kbId, storage, name, exactMatch));
    }

    @RequestMapping("/queryNodeByCategory")
    public String queryNodeByCategory(@RequestParam(name = "category_main", required = false) String categoryMain,
                                      @RequestParam(name = "category_detail", required = false) String categoryDetail,
                                      @RequestParam(required = false) String kbId,
                                      @RequestParam(required = false) String storage,
                                      @RequestParam(required = false) Integer page,
                                      @RequestParam(required = false) Integer size) throws JsonProcessingException {
        return mapper.writeValueAsString(knowledgeBaseReadService.queryNodeByCategory(kbId, storage, categoryMain, categoryDetail, page, size));
    }

    @RequestMapping("/queryGraph")
    public String queryGraph(@RequestParam String name,
                             @RequestParam Integer type,
                             @RequestParam(required = false) String kbId,
                             @RequestParam(required = false) String storage) throws JsonProcessingException {
        return mapper.writeValueAsString(knowledgeBaseReadService.queryGraph(kbId, storage, name, type));
    }

    @RequestMapping("/getNodeRelation")
    public String getNodeRelation(@RequestBody BasicNode node,
                                  @RequestParam(required = false) String kbId,
                                  @RequestParam(required = false) String storage) throws JsonProcessingException {
        return mapper.writeValueAsString(knowledgeBaseReadService.getNodeRelation(kbId, storage, node));
    }

    @RequestMapping("/getEntityCategories")
    public String getEntityCategories(@RequestParam(required = false) String kbId,
                                      @RequestParam(required = false) String storage) throws JsonProcessingException {
        return mapper.writeValueAsString(knowledgeBaseReadService.getEntityCategories(kbId, storage));
    }

    @RequestMapping("/getEventCategories")
    public String getEventCategories(@RequestParam(required = false) String kbId,
                                     @RequestParam(required = false) String storage) throws JsonProcessingException {
        return mapper.writeValueAsString(knowledgeBaseReadService.getEventCategories(kbId, storage));
    }

    @RequestMapping("/getModelCategories")
    public String getModelCategories(@RequestParam(required = false) String kbId,
                                     @RequestParam(required = false) String storage) throws JsonProcessingException {
        return mapper.writeValueAsString(knowledgeBaseReadService.getModelCategories(kbId, storage));
    }

    @RequestMapping("/getTreeCategories")
    public String getTreeCategories(@RequestParam(required = false) String kbId,
                                    @RequestParam(required = false) String storage) throws JsonProcessingException {
        return mapper.writeValueAsString(knowledgeBaseReadService.getTreeCategories(kbId, storage));
    }

    @RequestMapping("/getEntityCountByCategory")
    public String getEntityCountByCategory(@RequestParam(required = false) String kbId,
                                           @RequestParam(required = false) String storage) throws JsonProcessingException {
        return mapper.writeValueAsString(knowledgeBaseReadService.getEntityCountByCategory(kbId, storage));
    }

    @RequestMapping("/getEventCountByCategory")
    public String getEventCountByCategory(@RequestParam(required = false) String kbId,
                                          @RequestParam(required = false) String storage) throws JsonProcessingException {
        return mapper.writeValueAsString(knowledgeBaseReadService.getEventCountByCategory(kbId, storage));
    }

    @RequestMapping("/getModelCountByCategory")
    public String getModelCountByCategory(@RequestParam(required = false) String kbId,
                                          @RequestParam(required = false) String storage) throws JsonProcessingException {
        return mapper.writeValueAsString(knowledgeBaseReadService.getModelCountByCategory(kbId, storage));
    }

    @RequestMapping("/getTreeCountByCategory")
    public String getTreeCountByCategory(@RequestParam(required = false) String kbId,
                                         @RequestParam(required = false) String storage) throws JsonProcessingException {
        return mapper.writeValueAsString(knowledgeBaseReadService.getTreeCountByCategory(kbId, storage));
    }

    @RequestMapping("/getNodeCountByCategory")
    public String getNodeCountByCategory(@RequestParam(required = false) String kbId,
                                         @RequestParam(required = false) String storage) throws JsonProcessingException {
        return mapper.writeValueAsString(knowledgeBaseReadService.getNodeCountByCategory(kbId, storage));
    }

    @RequestMapping("/getNodeTotal")
    public String getNodeTotal(@RequestParam(required = false) String kbId,
                               @RequestParam(required = false) String storage) throws JsonProcessingException {
        return mapper.writeValueAsString(knowledgeBaseReadService.getNodeTotal(kbId, storage));
    }

    @PostMapping("/admin/fault/importDropin")
    public String importFaultDropin() throws JsonProcessingException {
        Result result = knowledgeBaseAdminService.importFaultKnowledgeFromDropinDir();
        return mapper.writeValueAsString(result);
    }
}
