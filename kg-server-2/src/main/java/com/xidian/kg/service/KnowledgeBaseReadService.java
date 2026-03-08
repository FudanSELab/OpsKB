package com.xidian.kg.service;

import com.xidian.kg.controller.util.Result;
import com.xidian.kg.entity.BasicNode;

public interface KnowledgeBaseReadService {

    Result listKnowledgeBases();

    Result getAll(String kbId, String storageType, Integer limit);

    Result queryNodeByName(String kbId, String storageType, String name, Boolean exactMatch);

    Result queryNodeByCategory(String kbId, String storageType, String categoryMain, String categoryDetail, Integer page, Integer size);

    Result queryGraph(String kbId, String storageType, String name, Integer type);

    Result getNodeRelation(String kbId, String storageType, BasicNode node);

    Result getEntityCategories(String kbId, String storageType);

    Result getEventCategories(String kbId, String storageType);

    Result getModelCategories(String kbId, String storageType);

    Result getTreeCategories(String kbId, String storageType);

    Result getEntityCountByCategory(String kbId, String storageType);

    Result getEventCountByCategory(String kbId, String storageType);

    Result getModelCountByCategory(String kbId, String storageType);

    Result getTreeCountByCategory(String kbId, String storageType);

    Result getNodeCountByCategory(String kbId, String storageType);

    Result getNodeTotal(String kbId, String storageType);
}
