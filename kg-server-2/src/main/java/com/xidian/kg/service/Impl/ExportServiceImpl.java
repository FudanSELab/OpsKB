package com.xidian.kg.service.Impl;

import com.xidian.kg.service.ExportService;
import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.session.Session;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.HashMap;

/**
 * Neo4j数据导出服务实现类
 */
@Service
public class ExportServiceImpl implements ExportService {
    
    private static final Logger logger = LoggerFactory.getLogger(ExportServiceImpl.class);
    
    @Resource
    private Session session;
    
    /**
     * 执行Neo4j数据导出
     * 调用APOC插件的export.json.all方法导出所有数据到data.txt文件
     */
    @Override
    public void exportData() {
        try {
            // 执行APOC导出命令
            String cypher = "CALL apoc.export.json.all(\"data.txt\", {})";
            Result result = session.query(cypher, new HashMap<>());
            
            logger.info("Neo4j数据导出命令执行完成");
            
        } catch (Exception e) {
            logger.error("Neo4j数据导出失败: {}", e.getMessage(), e);
        }
    }
}
