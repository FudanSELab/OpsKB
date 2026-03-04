package com.xidian.kg.service.Impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xidian.kg.service.ToolWithFile;
import com.xidian.kg.util.DifyApiConstants;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Service
public class ToolWithFileImpl implements ToolWithFile {
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Override
    public Map<String, Object> updateDocumentAndCheckStatus(String datasetId, String documentId, String filePath, String token) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 1. 读取文件并获取修改时间
            File file = new File(filePath);
            if (!file.exists()) {
                log.error("文件不存在: {}", filePath);
                result.put("code", 404);
                result.put("message", "文件不存在");
                return result;
            }
            
            // 检查文件大小
            long fileSize = file.length();
            double fileSizeMB = fileSize / (1024.0 * 1024.0);
            log.info("文件大小: {} bytes ({} MB)", fileSize, String.format("%.2f", fileSizeMB));
            
            if (fileSize == 0) {
                log.error("文件为空: {}", filePath);
                result.put("code", 400);
                result.put("message", "文件为空");
                return result;
            }
            
            // 获取当前文件修改时间作为 name
            long lastModified = file.lastModified();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            String currentFileName = sdf.format(new Date(lastModified));
            
            log.info("当前文件修改时间: {}, 生成文件名: {}", lastModified, currentFileName);
            
            // 2. 检查文件是否有更新：查询文档当前的name字段
            String existingFileName = getDocumentName(datasetId, documentId, token);
            log.info("文档当前name: {}", existingFileName);
            
            if (currentFileName.equals(existingFileName)) {
                log.info("文件未更新，直接返回索引完成");
                result.put("code", 200);
                result.put("message", "文件未更新，索引已完成");
                result.put("status", "completed");
                result.put("fileModified", false);
                return result;
            }
            
            log.info("检测到文件已更新，开始上传新文件");
            
            // 3. 构建 data JSON 字符串
            String dataJson = buildDataJson(currentFileName, objectMapper);
            log.info("data JSON: {}", dataJson);
            
            // 4. 上传文件并获取 batch
            String batch = uploadFile(datasetId, documentId, file, dataJson, token);
            log.info("获取到 batch: {}", batch);
            
            // 5. 轮询检查索引状态
            Map<String, Object> indexResult = checkIndexingStatus(datasetId, batch, token);
            indexResult.put("fileModified", true);
            return indexResult;
            
        } catch (Exception e) {
            log.error("更新文档失败", e);
            result.put("code", 500);
            result.put("message", "更新文档失败: " + e.getMessage());
            return result;
        }
    }
    
    /**
     * 获取文档的name字段（上次上传的文件修改时间）
     */
    private String getDocumentName(String datasetId, String documentId, String token) throws Exception {
        try {
            // 构建获取文档列表的URL
            String url = String.format(DifyApiConstants.DIFY_API_BASE_URL + "/datasets/%s/documents", datasetId);
            log.info("获取文档列表URL: {}", url);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);
            
            // 发送GET请求
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    String.class
            );
            
            log.info("文档列表响应: {}", response.getBody());
            
            // 解析响应，查找指定documentId的文档
            JsonNode responseNode = objectMapper.readTree(response.getBody());
            JsonNode dataArray = responseNode.get("data");
            
            if (dataArray != null && dataArray.isArray()) {
                for (JsonNode doc : dataArray) {
                    String id = doc.get("id").asText();
                    if (documentId.equals(id)) {
                        String name = doc.get("name").asText();
                        log.info("找到文档 {}, name: {}", documentId, name);
                        return name;
                    }
                }
            }
            
            log.warn("未找到文档ID: {}", documentId);
            return null;
            
        } catch (Exception e) {
            log.error("获取文档name失败: {}", e.getMessage(), e);
            // 如果获取失败，返回null，这样会触发上传
            return null;
        }
    }
    
    /**
     * 构建请求数据JSON
     */
    private String buildDataJson(String fileName, ObjectMapper objectMapper) throws Exception {
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("name", fileName);
        dataMap.put("indexing_technique", "high_quality");
        
        Map<String, Object> processRule = new HashMap<>();
        Map<String, Object> rules = new HashMap<>();
        
        // 预处理规则
        List<Map<String, Object>> preProcessingRules = new ArrayList<>();
        Map<String, Object> rule1 = new HashMap<>();
        rule1.put("id", "remove_extra_spaces");
        rule1.put("enabled", false);
        preProcessingRules.add(rule1);
        
        Map<String, Object> rule2 = new HashMap<>();
        rule2.put("id", "remove_urls_emails");
        rule2.put("enabled", false);
        preProcessingRules.add(rule2);
        
        rules.put("pre_processing_rules", preProcessingRules);
        
        // 分段规则
        Map<String, Object> segmentation = new HashMap<>();
        segmentation.put("separator", "\\n");
        segmentation.put("max_tokens", 1000);
        rules.put("segmentation", segmentation);
        
        processRule.put("rules", rules);
        processRule.put("mode", "custom");
        dataMap.put("process_rule", processRule);
        
        return objectMapper.writeValueAsString(dataMap);
    }
    
    /**
     * 上传文件到服务器
     */
    private String uploadFile(String datasetId, String documentId, File file, String dataJson, String token) throws Exception {
        try {
            // 构建 multipart/form-data 请求
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.set("Authorization", "Bearer " + token);
            
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("data", dataJson);
            body.add("file", new FileSystemResource(file));
            
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            
            // 发送 POST 请求上传文件
            String uploadUrl = String.format(DifyApiConstants.DIFY_API_BASE_URL + "/datasets/%s/documents/%s/update-by-file",
                    datasetId, documentId);
            log.info("上传URL: {}", uploadUrl);
            log.info("开始上传文件，大小: {} bytes", file.length());
            
            ResponseEntity<String> uploadResponse = restTemplate.exchange(
                    uploadUrl,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );
            
            log.info("上传响应状态码: {}", uploadResponse.getStatusCodeValue());
            log.info("上传响应: {}", uploadResponse.getBody());
            
            // 解析响应获取 batch
            JsonNode responseNode = objectMapper.readTree(uploadResponse.getBody());
            return responseNode.get("batch").asText();
            
        } catch (org.springframework.web.client.ResourceAccessException e) {
            // 连接相关的错误
            if (e.getMessage().contains("Connection reset")) {
                log.error("连接被服务器重置，可能的原因：");
                log.error("1. 文件太大，超过服务器限制");
                log.error("2. 服务器端超时");
                log.error("3. 网络不稳定");
                log.error("文件大小: {} bytes", file.length());
                throw new Exception("文件上传失败：连接被服务器重置。可能是文件太大或服务器限制。文件大小: " + file.length() + " bytes", e);
            } else if (e.getMessage().contains("timeout")) {
                log.error("上传超时，文件大小: {} bytes", file.length());
                throw new Exception("文件上传超时，请检查网络连接或减小文件大小", e);
            } else {
                log.error("网络错误: {}", e.getMessage());
                throw new Exception("文件上传失败：" + e.getMessage(), e);
            }
        } catch (Exception e) {
            log.error("上传文件时发生未知错误: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 轮询检查索引状态
     */
    private Map<String, Object> checkIndexingStatus(String datasetId, String batch, String token) throws Exception {
        Map<String, Object> result = new HashMap<>();
        
        String statusUrl = String.format(DifyApiConstants.DIFY_API_BASE_URL + "/datasets/%s/documents/%s/indexing-status",
                datasetId, batch);
        log.info("状态检查URL: {}", statusUrl);
        
        HttpHeaders statusHeaders = new HttpHeaders();
        statusHeaders.set("Authorization", "Bearer " + token);
        HttpEntity<String> statusEntity = new HttpEntity<>(statusHeaders);
        
        long startTime = System.currentTimeMillis();
        long timeout = 60000; // 60秒超时
        
        while (true) {
            // 检查是否超时
            if (System.currentTimeMillis() - startTime > timeout) {
                log.warn("索引状态检查超时");
                result.put("code", 301);
                result.put("message", "索引状态检查超时");
                result.put("batch", batch);
                return result;
            }
            
            // 发送状态检查请求
            ResponseEntity<String> statusResponse = restTemplate.exchange(
                    statusUrl,
                    HttpMethod.GET,
                    statusEntity,
                    String.class
            );
            
            log.info("状态响应: {}", statusResponse.getBody());
            
            JsonNode statusNode = objectMapper.readTree(statusResponse.getBody());
            JsonNode dataArray = statusNode.get("data");
            
            if (dataArray != null && dataArray.isArray() && dataArray.size() > 0) {
                String indexingStatus = dataArray.get(0).get("indexing_status").asText();
                log.info("当前索引状态: {}", indexingStatus);
                
                if ("completed".equals(indexingStatus)) {
                    log.info("索引完成");
                    result.put("code", 200);
                    result.put("message", "索引完成");
                    result.put("batch", batch);
                    result.put("status", indexingStatus);
                    return result;
                }
            }
            
            // 等待 1 秒后继续检查
            Thread.sleep(1000);
        }
    }
}
