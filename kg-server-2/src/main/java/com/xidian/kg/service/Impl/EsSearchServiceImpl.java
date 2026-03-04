package com.xidian.kg.service.Impl;

import com.xidian.kg.controller.util.Result;
import com.xidian.kg.dao.EsNodeRepository;
import com.xidian.kg.entity.EsNodeDocument;
import com.xidian.kg.service.EsSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Elasticsearch 搜索服务实现
 */
@Service
public class EsSearchServiceImpl implements EsSearchService {

    private static final Logger logger = LoggerFactory.getLogger(EsSearchServiceImpl.class);

    @Resource
    private EsNodeRepository esNodeRepository;

    @Override
    public Result searchByKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return new Result(false, "关键字不能为空");
        }
        try {
            List<EsNodeDocument> docs = esNodeRepository.findByNameContaining(keyword.trim());

            // 为了与前端当前使用的 BasicNode 数据结构大致兼容，这里简单封装一下返回结构
            Map<String, Object> data = new HashMap<>();
            data.put("total", docs.size());
            data.put("items", docs);

            return new Result(true, data);
        } catch (Exception e) {
            logger.error("Elasticsearch 搜索失败, keyword={}", keyword, e);
            return new Result(false, "Elasticsearch 搜索失败: " + e.getMessage());
        }
    }
}

