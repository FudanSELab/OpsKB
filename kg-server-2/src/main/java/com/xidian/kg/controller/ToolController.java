package com.xidian.kg.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xidian.kg.entity.BasicNode;
import com.xidian.kg.entity.FusionRelationVO;
import com.xidian.kg.service.ToolWithFile;
import com.xidian.kg.util.DifyApiConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Slf4j
@RequestMapping("/tool")
@ResponseBody
@Controller
@CrossOrigin
public class ToolController {

    // ==================== 工作流Token常量 ====================
    /** 提取工作流Token（用于文本、图片、文件的知识提取和预览） */
    private static final String DEFAULT_EXTRACTION_WORKFLOW_TOKEN = "app-rUiLLkZKhl9Uj7yijSM4PzIz";
    /** 冲突消解工作流Token */
    private static final String DEFAULT_RESOLUTION_WORKFLOW_TOKEN = "app-EdIWsMctP1FfQeNjR9XwCMja";
    /** Fusion工作流Token */
    private static final String DEFAULT_FUSION_WORKFLOW_TOKEN = "app-jLNrn9QwdPzw6BAsP4115fha";

    // ==================== 文档索引配置常量 ====================
    /** 索引数据集ID */
    private static final String DEFAULT_INDEX_DATASET_ID = "03423696-afd8-408a-b025-96de45b2f4f4";
    /** 索引文档ID */
    private static final String DEFAULT_INDEX_DOCUMENT_ID = "d43e60f8-a27b-477f-ab15-18f5a4ad2f58";
    /** 索引文件路径 */
    private static final String DEFAULT_INDEX_FILE_PATH = "/opt/neo4j-community-5/import/data.txt";
    // private static final String DEFAULT_INDEX_FILE_PATH = "D:\\Tools\\Package\\neo4j-community-3.5.31\\import\\data.txt";
    /** 索引数据集授权Token */
    private static final String DEFAULT_INDEX_DATASET_TOKEN = "dataset-mXYtj8eLQW8gfWpTw6q5t6a9";

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ToolWithFile toolWithFile;

//    /**
//     * 获取数据集列表
//     * @param page 页码
//     * @param limit 每页数量
//     * @param token Authorization Bearer token
//     * @return 数据集列表
//     */
//    @RequestMapping("/getDatasets")
//    public String getDatasets(
//            @RequestParam(defaultValue = "1") int page,
//            @RequestParam(defaultValue = "20") int limit,
//            @RequestParam(defaultValue = "dataset-rq4IF05IL7l4qY7aqaxCUMUL") String token) {
//
//        // 构建URL
//        String url = String.format("https://api.dify.ai/v1/datasets?page=%d&limit=%d", page, limit);
//        log.info("url={}", url);
//        // 设置请求头
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("Authorization", "Bearer " + token);
//
//        // 创建请求实体
//        HttpEntity<String> entity = new HttpEntity<>(headers);
//
//        // 发送GET请求
//        ResponseEntity<String> response = restTemplate.exchange(
//                url,
//                HttpMethod.GET,
//                entity,
//                String.class
//        );
//
//        return "测试";
//    }

    /**
     * 更新文档并检查索引状态
     * @param datasetId 数据集ID
     * @param documentId 文档ID
     * @param filePath 本地文件路径
     * @param token Authorization Bearer token
     * @return 状态码和消息
     */
    @RequestMapping("/updateDocument")
    public Map<String, Object> updateDocument(
            @RequestParam(defaultValue = DEFAULT_INDEX_DATASET_ID) String datasetId,
            @RequestParam(defaultValue = DEFAULT_INDEX_DOCUMENT_ID) String documentId,
            @RequestParam(defaultValue = DEFAULT_INDEX_FILE_PATH) String filePath,
            @RequestParam(defaultValue = DEFAULT_INDEX_DATASET_TOKEN) String token) {
        
        log.info("开始更新文档: datasetId={}, documentId={}, filePath={}", datasetId, documentId, filePath);
        return toolWithFile.updateDocumentAndCheckStatus(datasetId, documentId, filePath, token);
    }

   /**
    * 通过图片提取节点信息（仅提取，不插入数据库）
    * @param file 上传的图片文件
    * @param type 类型参数
    * @param token Authorization Bearer token
    * @return 提取的节点列表
    */
   @RequestMapping("/extractNodesFromImage")
   public List<BasicNode> extractNodesFromImage(
           @RequestParam("file") MultipartFile file,
           @RequestParam("type") String type,
           @RequestParam(defaultValue = DEFAULT_EXTRACTION_WORKFLOW_TOKEN) String token) {
       List<BasicNode> nodeList = new ArrayList<>();
       ObjectMapper objectMapper = new ObjectMapper();

       try {
           // 步骤1: 上传图片文件
           log.info("开始上传图片，文件名: {}, 大小: {}, 类型: {}", file.getOriginalFilename(), file.getSize(), type);

           // 1.1 构建上传URL
           String uploadUrl = DifyApiConstants.FILES_UPLOAD_URL;
           log.info("上传URL: {}", uploadUrl);

           // 1.2 设置请求头
           HttpHeaders uploadHeaders = new HttpHeaders();
           uploadHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
           uploadHeaders.set("Authorization", "Bearer " + token);

           // 1.3 构建 multipart/form-data 请求
           MultiValueMap<String, Object> uploadBody = new LinkedMultiValueMap<>();
           uploadBody.add("file", file.getResource());
           uploadBody.add("user", "user-123");

           HttpEntity<MultiValueMap<String, Object>> uploadRequest = new HttpEntity<>(uploadBody, uploadHeaders);

           // 1.4 发送上传请求
           ResponseEntity<String> uploadResponse = restTemplate.exchange(
                   uploadUrl,
                   HttpMethod.POST,
                   uploadRequest,
                   String.class
           );

           log.info("上传响应: {}", uploadResponse.getBody());

           // 1.5 解析上传响应，获取文件ID
           JsonNode uploadResponseNode = objectMapper.readTree(uploadResponse.getBody());
           String fileId = uploadResponseNode.get("id").asText();
           log.info("获取到文件ID: {}", fileId);

           // 步骤2: 使用文件ID调用工作流
           // 2.1 构建工作流URL
           String workflowUrl = DifyApiConstants.WORKFLOWS_RUN_URL;
           log.info("工作流URL: {}", workflowUrl);

           // 2.2 设置请求头
           HttpHeaders workflowHeaders = new HttpHeaders();
           workflowHeaders.setContentType(MediaType.APPLICATION_JSON);
           workflowHeaders.set("Authorization", "Bearer " + token);

           // 2.3 构建请求体
           Map<String, Object> requestBody = new HashMap<>();

           // 构建 inputs.image 对象
           Map<String, Object> inputs = new HashMap<>();
           Map<String, Object> imageObj = new HashMap<>();
           imageObj.put("transfer_method", "local_file");
           imageObj.put("upload_file_id", fileId);
           imageObj.put("type", "image");
           inputs.put("image", imageObj);
           

           inputs.put("type", type);
           requestBody.put("inputs", inputs);
           requestBody.put("response_mode", "streaming");
           requestBody.put("user", "abc-123");

           String requestJson = objectMapper.writeValueAsString(requestBody);
           log.info("工作流请求体: {}", requestJson);

           // 2.4 创建请求实体
           HttpEntity<String> workflowRequest = new HttpEntity<>(requestJson, workflowHeaders);

           // 2.5 发送POST请求到工作流（streaming模式）
           ResponseEntity<String> workflowResponse = restTemplate.exchange(
                   workflowUrl,
                   HttpMethod.POST,
                   workflowRequest,
                   String.class
           );

           String responseBody = workflowResponse.getBody();
           log.info("工作流SSE响应长度: {}", responseBody != null ? responseBody.length() : 0);

           // 2.6 解析SSE流式响应，获取通用outputs数据
           Map<String, Object> outputs = parseSseResponse(responseBody, objectMapper);
           
           // 2.7 从outputs中提取output.data数据
           Object outputObj = outputs.get("output");
           if (outputObj != null && outputObj instanceof Map) {
               @SuppressWarnings("unchecked")
               Map<String, Object> outputMap = (Map<String, Object>) outputObj;
               Object dataArrayObj = outputMap.get("data");
               
               if (dataArrayObj != null && dataArrayObj instanceof List) {
                   @SuppressWarnings("unchecked")
                   List<Map<String, Object>> dataArray = (List<Map<String, Object>>) dataArrayObj;
                   
                   // 2.8 转换为 BasicNode 列表
                   for (Map<String, Object> nodeData : dataArray) {
                       BasicNode basicNode = new BasicNode();

                       // 设置labels
                       Object labelsObj = nodeData.get("labels");
                       if (labelsObj != null && labelsObj instanceof List) {
                           @SuppressWarnings("unchecked")
                           List<String> labels = (List<String>) labelsObj;
                           basicNode.setLabels(labels);
                       }

                       // 设置properties
                       Object propertiesObj = nodeData.get("properties");
                       if (propertiesObj != null && propertiesObj instanceof Map) {
                           @SuppressWarnings("unchecked")
                           Map<String, Object> properties = (Map<String, Object>) propertiesObj;
                           basicNode.setProperties(properties);
                       }

                       // 设置id为null
                       basicNode.setId(null);

                       nodeList.add(basicNode);
                       log.info("转换节点: labels={}, properties={}",
                               basicNode.getLabels(), basicNode.getProperties());
                   }
               }
           }

           log.info("从图片成功提取 {} 个节点", nodeList.size());

       } catch (Exception e) {
           log.error("从图片提取节点失败", e);
       }

       return nodeList;
   }

   /**
    * 通过文本提取知识（节点），但不插入数据库
    * @param inputParams 输入参数，包含text字段
    * @param type 类型参数
    * @param token Authorization Bearer token
    * @return 提取结果，包含节点列表
    */
   @RequestMapping("/extractFromText")
   public Map<String, Object> extractFromText(
           @RequestBody Map<String, Object> inputParams,
           @RequestParam(defaultValue = DEFAULT_EXTRACTION_WORKFLOW_TOKEN) String token) {
       Map<String, Object> result = new HashMap<>();
       List<BasicNode> nodeList = new ArrayList<>();
       ObjectMapper objectMapper = new ObjectMapper();

       try {
           // 步骤1: 从输入参数中提取text
           String text = (String) inputParams.get("text");
           String kgType = (String) inputParams.get("type");
           log.info("开始从文本提取知识，文本长度: {}, 类型: {}", text != null ? text.length() : 0, kgType);

           // 步骤2: 构建工作流URL
           String workflowUrl = DifyApiConstants.WORKFLOWS_RUN_URL;
           log.info("工作流URL: {}", workflowUrl);

           // 步骤3: 设置请求头
           HttpHeaders headers = new HttpHeaders();
           headers.setContentType(MediaType.APPLICATION_JSON);
           headers.set("Authorization", "Bearer " + token);

           // 步骤4: 构建请求体
           Map<String, Object> requestBody = new HashMap<>();
           requestBody.put("inputs", inputParams);
           requestBody.put("response_mode", "streaming");
           requestBody.put("user", "abc-123");

           String requestJson = objectMapper.writeValueAsString(requestBody);
           log.info("请求体: {}", requestJson);

           // 步骤5: 创建请求实体
           HttpEntity<String> requestEntity = new HttpEntity<>(requestJson, headers);

           // 步骤6: 发送POST请求（streaming模式）
           ResponseEntity<String> response = restTemplate.exchange(
                   workflowUrl,
                   HttpMethod.POST,
                   requestEntity,
                   String.class
           );

           String responseBody = response.getBody();
           log.info("工作流SSE响应长度: {}", responseBody != null ? responseBody.length() : 0);

           // 步骤7: 解析SSE流式响应，获取通用outputs数据
           Map<String, Object> outputs = parseSseResponse(responseBody, objectMapper);
           
           // 步骤8: 从outputs中提取output.data数据
           Object outputObj = outputs.get("output");
           if (outputObj != null && outputObj instanceof Map) {
               @SuppressWarnings("unchecked")
               Map<String, Object> outputMap = (Map<String, Object>) outputObj;
               Object dataArrayObj = outputMap.get("data");
               
               if (dataArrayObj != null && dataArrayObj instanceof List) {
                   @SuppressWarnings("unchecked")
                   List<Map<String, Object>> dataArray = (List<Map<String, Object>>) dataArrayObj;
                   
                   // 步骤9: 分离节点
                   for (Map<String, Object> item : dataArray) {
                       String itemType = (String) item.get("type");
                       
                       if ("node".equals(itemType)) {
                           // 解析节点
                           BasicNode basicNode = new BasicNode();
                           
                           // 设置labels
                           Object labelsObj = item.get("labels");
                           if (labelsObj != null && labelsObj instanceof List) {
                               @SuppressWarnings("unchecked")
                               List<String> labels = (List<String>) labelsObj;
                               basicNode.setLabels(labels);
                           }
                           
                           // 设置properties
                           Object propertiesObj = item.get("properties");
                           if (propertiesObj != null && propertiesObj instanceof Map) {
                               @SuppressWarnings("unchecked")
                               Map<String, Object> properties = (Map<String, Object>) propertiesObj;
                               basicNode.setProperties(properties);
                           }
                           
                           // 设置id为null
                           basicNode.setId(null);
                           
                           nodeList.add(basicNode);
                           log.info("提取节点: labels={}, properties={}", 
                                   basicNode.getLabels(), basicNode.getProperties());
                        }   
                    //    } else if ("relationship".equals(type)) {
                    //        // 解析关系
                    //        Map<String, Object> relation = new HashMap<>();
                           
                    //        String label = (String) item.get("label");
                    //        relation.put("type", label);
                           
                    //        // 获取起始节点和结束节点信息
                    //        @SuppressWarnings("unchecked")
                    //        Map<String, Object> startNode = (Map<String, Object>) item.get("start");
                    //        @SuppressWarnings("unchecked")
                    //        Map<String, Object> endNode = (Map<String, Object>) item.get("end");
                           
                    //        Map<String, Object> start = new HashMap<>();
                    //        start.put("name", startNode.get("name"));
                    //        if (startNode.get("labels") != null && startNode.get("labels") instanceof List) {
                    //            start.put("labels", startNode.get("labels"));
                    //        }
                           
                    //        Map<String, Object> end = new HashMap<>();
                    //        end.put("name", endNode.get("name"));
                    //        if (endNode.get("labels") != null && endNode.get("labels") instanceof List) {
                    //            end.put("labels", endNode.get("labels"));
                    //        }
                           
                    //        relation.put("start", start);
                    //        relation.put("end", end);
                           
                    //        // 设置关系属性
                    //        Object propertiesObj = item.get("properties");
                    //        if (propertiesObj != null && propertiesObj instanceof Map) {
                    //            @SuppressWarnings("unchecked")
                    //            Map<String, Object> properties = (Map<String, Object>) propertiesObj;
                    //            relation.put("properties", properties);
                    //        }
                           
                    //        relationList.add(relation);
                    //        log.info("提取关系: {} -> {} ({})", 
                    //                start.get("name"), end.get("name"), label);
                    //    }
                   }
               }
           }

           log.info("从文本成功提取 {} 个节点", nodeList.size());

           result.put("code", 200);
           result.put("message", "提取成功");
           result.put("nodeCount", nodeList.size());
           result.put("nodes", nodeList);

       } catch (Exception e) {
           log.error("从文本提取知识失败", e);
           result.put("code", 500);
           result.put("message", "提取失败: " + e.getMessage());
           result.put("nodeCount", 0);
           result.put("nodes", nodeList);
       }

       return result;
   }

   /**
    * 根据JSON格式的节点数据进行冲突消解
    * @param inputParams 输入参数，包含target字段（JSON字符串格式的节点信息）
    * @param token Authorization Bearer token (冲突消解工作流的token)
    * @return 冲突消解结果，包含target、similar_target、judge、judge_reason、finalKg等信息
    */
   @RequestMapping("/resolveConflict")
   public Map<String, Object> resolveConflict(
           @RequestBody Map<String, Object> inputParams,
           @RequestParam(defaultValue = DEFAULT_RESOLUTION_WORKFLOW_TOKEN) String token) {
       Map<String, Object> result = new HashMap<>();
       ObjectMapper objectMapper = new ObjectMapper();

       try {
           // 步骤1: 从输入参数中提取target JSON字符串
           String targetJson = (String) inputParams.get("target");
           log.info("开始冲突消解，target: {}", targetJson);

           // 步骤2: 构建工作流URL
           String workflowUrl = DifyApiConstants.WORKFLOWS_RUN_URL;
           log.info("冲突消解工作流URL: {}", workflowUrl);

           // 步骤3: 设置请求头
           HttpHeaders headers = new HttpHeaders();
           headers.setContentType(MediaType.APPLICATION_JSON);
           headers.set("Authorization", "Bearer " + token);

           // 步骤4: 构建请求体
           Map<String, Object> inputs = new HashMap<>();
           inputs.put("target", targetJson);
           
           Map<String, Object> requestBody = new HashMap<>();
           requestBody.put("inputs", inputs);
           requestBody.put("response_mode", "streaming");
           requestBody.put("user", "abc-123");

           String requestJson = objectMapper.writeValueAsString(requestBody);
           log.info("冲突消解请求体: {}", requestJson);

           // 步骤5: 创建请求实体
           HttpEntity<String> requestEntity = new HttpEntity<>(requestJson, headers);

           // 步骤6: 发送POST请求（streaming模式）
           ResponseEntity<String> response = restTemplate.exchange(
                   workflowUrl,
                   HttpMethod.POST,
                   requestEntity,
                   String.class
           );

           String responseBody = response.getBody();
           log.info("冲突消解工作流SSE响应长度: {}", responseBody != null ? responseBody.length() : 0);

           // 步骤7: 解析SSE流式响应，获取通用outputs数据
           Map<String, Object> outputs = parseSseResponse(responseBody, objectMapper);

           // 步骤8: 从outputs中提取result字段
           Object resultObj = outputs.get("result");
           if (resultObj == null) {
               result.put("code", 400);
               result.put("message", "outputs中没有result字段");
               return result;
           }

           if (!(resultObj instanceof Map)) {
               result.put("code", 400);
               result.put("message", "result字段格式错误");
               return result;
           }

           @SuppressWarnings("unchecked")
           Map<String, Object> resultMap = (Map<String, Object>) resultObj;
           
           // 提取data字段作为最终返回结果
           Object dataObj = resultMap.get("data");
           if (dataObj == null) {
               result.put("code", 400);
               result.put("message", "result中没有data字段");
               return result;
           }
           
           if (!(dataObj instanceof Map)) {
               result.put("code", 400);
               result.put("message", "result.data格式错误");
               return result;
           }
           
           @SuppressWarnings("unchecked")
           Map<String, Object> dataMap = (Map<String, Object>) dataObj;
           
           // 处理target字段：如果是JSON字符串，解析为对象
           Object targetObj = dataMap.get("target");
           if (targetObj != null && targetObj instanceof String) {
               String targetStr = (String) targetObj;
               try {
                   Object parsedTarget = objectMapper.readValue(targetStr, Object.class);
                   dataMap.put("target", parsedTarget);
               } catch (Exception ex) {
                   log.warn("无法解析target JSON字符串，保持原值: {}", targetStr);
               }
           }
           
           // 处理finalKg字段：如果是字符串"null"，转换为null对象；否则解析为对象
           Object finalKgObj = dataMap.get("finalKg");
           if (finalKgObj != null && finalKgObj instanceof String) {
               String finalKgStr = (String) finalKgObj;
               if ("null".equals(finalKgStr)) {
                   dataMap.put("finalKg", null);
               } else {
                   // 尝试将JSON字符串解析为对象
                   try {
                       Object parsedFinalKg = objectMapper.readValue(finalKgStr, Object.class);
                       dataMap.put("finalKg", parsedFinalKg);
                   } catch (Exception ex) {
                       log.warn("无法解析finalKg JSON字符串: {}", finalKgStr);
                   }
               }
           }
           
           result = dataMap;
           log.info("冲突消解成功，judge: {}", result.get("judge"));

       } catch (Exception e) {
           log.error("冲突消解失败", e);
           result.put("code", 500);
           result.put("message", "冲突消解失败: " + e.getMessage());
       }

       return result;
   }

   /**
    * 从文件提取知识，但不插入数据库
    * @param file 上传的文件
    * @param type 类型参数
    * @param token Authorization Bearer token
    * @return 提取结果，包含节点和关系等统计信息
    */
   @RequestMapping("/extractFromFile")
   public Map<String, Object> extractFromFile(
           @RequestParam("file") MultipartFile file,
           @RequestParam("type") String type,
           @RequestParam(defaultValue = DEFAULT_EXTRACTION_WORKFLOW_TOKEN) String token) {
       Map<String, Object> result = new HashMap<>();
       ObjectMapper objectMapper = new ObjectMapper();
       
       List<BasicNode> allNodes = new ArrayList<>();
       
       try {
           log.info("开始从文件提取知识（不插入数据库）: {}, 大小: {} bytes", 
                   file.getOriginalFilename(), file.getSize());
           
           // 步骤1: 上传文件到dify
           log.info("开始上传文件到dify，文件名: {}, 大小: {}", file.getOriginalFilename(), file.getSize());
           
           // 1.1 构建上传URL
           String uploadUrl = DifyApiConstants.FILES_UPLOAD_URL;
           log.info("上传URL: {}", uploadUrl);
           
           // 1.2 设置请求头
           HttpHeaders uploadHeaders = new HttpHeaders();
           uploadHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
           uploadHeaders.set("Authorization", "Bearer " + token);
           
           // 1.3 构建 multipart/form-data 请求
           MultiValueMap<String, Object> uploadBody = new LinkedMultiValueMap<>();
           uploadBody.add("file", file.getResource());
           uploadBody.add("user", "user-123");
           
           HttpEntity<MultiValueMap<String, Object>> uploadRequest = new HttpEntity<>(uploadBody, uploadHeaders);
           
           // 1.4 发送上传请求
           ResponseEntity<String> uploadResponse = restTemplate.exchange(
                   uploadUrl,
                   HttpMethod.POST,
                   uploadRequest,
                   String.class
           );
           
           log.info("上传响应: {}", uploadResponse.getBody());
           
           // 1.5 解析上传响应，获取文件ID
           JsonNode uploadResponseNode = objectMapper.readTree(uploadResponse.getBody());
           String fileId = uploadResponseNode.get("id").asText();
           log.info("获取到文件ID: {}", fileId);
           
           // 步骤2: 使用文件ID调用工作流
           // 2.1 构建工作流URL
           String workflowUrl = DifyApiConstants.WORKFLOWS_RUN_URL;
           log.info("工作流URL: {}", workflowUrl);
           
           // 2.2 设置请求头
           HttpHeaders workflowHeaders = new HttpHeaders();
           workflowHeaders.setContentType(MediaType.APPLICATION_JSON);
           workflowHeaders.set("Authorization", "Bearer " + token);
           
           // 2.3 构建请求体
           Map<String, Object> requestBody = new HashMap<>();
           
           // 构建 inputs.file 对象（document类型）
           Map<String, Object> inputs = new HashMap<>();
           Map<String, Object> fileObj = new HashMap<>();
           fileObj.put("transfer_method", "local_file");
           fileObj.put("upload_file_id", fileId);
           fileObj.put("type", "document");
           inputs.put("file", fileObj);
           
           inputs.put("type", type);
           
           requestBody.put("inputs", inputs);
           requestBody.put("response_mode", "streaming");
           requestBody.put("user", "abc-123");
           
           String requestJson = objectMapper.writeValueAsString(requestBody);
           log.info("工作流请求体: {}", requestJson);
           
           // 2.4 创建请求实体
           HttpEntity<String> workflowRequest = new HttpEntity<>(requestJson, workflowHeaders);
           
           // 2.5 发送POST请求到工作流（streaming模式）
           ResponseEntity<String> workflowResponse = restTemplate.exchange(
                   workflowUrl,
                   HttpMethod.POST,
                   workflowRequest,
                   String.class
           );
           
           String responseBody = workflowResponse.getBody();
           log.info("工作流SSE响应长度: {}", responseBody != null ? responseBody.length() : 0);
           
           // 2.6 解析SSE流式响应，获取通用outputs数据
           Map<String, Object> outputs = parseSseResponse(responseBody, objectMapper);
           
           // 2.7 从outputs中提取output.data数据
           Object outputObj = outputs.get("output");
           if (outputObj != null && outputObj instanceof Map) {
               @SuppressWarnings("unchecked")
               Map<String, Object> outputMap = (Map<String, Object>) outputObj;
               Object dataArrayObj = outputMap.get("data");
               
               if (dataArrayObj != null && dataArrayObj instanceof List) {
                   @SuppressWarnings("unchecked")
                   List<Map<String, Object>> dataArray = (List<Map<String, Object>>) dataArrayObj;
                   
                   // 2.8 解析节点和关系
                   for (Map<String, Object> item : dataArray) {
                       String itemType = (String) item.get("type");
                       
                       if ("node".equals(itemType)) {
                           BasicNode basicNode = new BasicNode();
                           
                           // 设置labels
                           Object labelsObj = item.get("labels");
                           if (labelsObj != null && labelsObj instanceof List) {
                               @SuppressWarnings("unchecked")
                               List<String> labels = (List<String>) labelsObj;
                               basicNode.setLabels(labels);
                           }
                           
                           // 设置properties
                           Object propertiesObj = item.get("properties");
                           if (propertiesObj != null && propertiesObj instanceof Map) {
                               @SuppressWarnings("unchecked")
                               Map<String, Object> properties = (Map<String, Object>) propertiesObj;
                               basicNode.setProperties(properties);
                           }
                           
                           basicNode.setId(null);
                           allNodes.add(basicNode);
                           
                       } 
                   }
               }
           }
           
           // 3. 构建返回结果
           result.put("code", 200);
           result.put("message", "文件处理完成");
           result.put("fileName", file.getOriginalFilename());
           result.put("totalNodeCount", allNodes.size());
           result.put("nodes", allNodes);
           
           log.info("文件处理完成 - 总节点: {}", allNodes.size());
           
       } catch (Exception e) {
           log.error("文件处理失败", e);
           result.put("code", 500);
           result.put("message", "文件处理失败: " + e.getMessage());
           result.put("totalNodeCount", allNodes.size());
           result.put("nodes", allNodes);
       }
       
       return result;
   }

   /**
    * Fusion工作流接口 - 根据events和categories进行融合处理，返回关系数组
    * @param inputParams 输入参数，包含events和categories字段
    * @param token Authorization Bearer token (Fusion工作流的token)
    * @return 包含关系列表的响应数据
    */
   @RequestMapping("/fusion")
   public Map<String, Object> fusion(
           @RequestBody Map<String, Object> inputParams,
           @RequestParam(defaultValue = DEFAULT_FUSION_WORKFLOW_TOKEN) String token) {
       Map<String, Object> result = new HashMap<>();
       List<FusionRelationVO> relationList = new ArrayList<>();
       ObjectMapper objectMapper = new ObjectMapper();

       try {
           // 步骤1: 从输入参数中提取events和categories
           String events = (String) inputParams.get("events");
           String categories = (String) inputParams.get("categories");
           log.info("开始Fusion工作流，events: {}, categories: {}", events, categories);

           // 步骤2: 构建工作流URL
           String workflowUrl = DifyApiConstants.WORKFLOWS_RUN_URL;
           log.info("Fusion工作流URL: {}", workflowUrl);

           // 步骤3: 设置请求头
           HttpHeaders headers = new HttpHeaders();
           headers.setContentType(MediaType.APPLICATION_JSON);
           headers.set("Authorization", "Bearer " + token);

           // 步骤4: 构建请求体
           Map<String, Object> inputs = new HashMap<>();
           inputs.put("events", events);
           inputs.put("categories", categories);
           
           Map<String, Object> requestBody = new HashMap<>();
           requestBody.put("inputs", inputs);
           requestBody.put("response_mode", "streaming");
           requestBody.put("user", "abc-123");

           String requestJson = objectMapper.writeValueAsString(requestBody);
           log.info("Fusion工作流请求体: {}", requestJson);

           // 步骤5: 创建请求实体
           HttpEntity<String> requestEntity = new HttpEntity<>(requestJson, headers);

           // 步骤6: 发送POST请求（streaming模式）
           ResponseEntity<String> response = restTemplate.exchange(
                   workflowUrl,
                   HttpMethod.POST,
                   requestEntity,
                   String.class
           );

           String responseBody = response.getBody();
           log.info("Fusion工作流SSE响应长度: {}", responseBody != null ? responseBody.length() : 0);

           // 步骤7: 解析SSE流式响应，获取通用outputs数据
           Map<String, Object> outputs = parseSseResponse(responseBody, objectMapper);
           
           // 步骤8: 从outputs中提取output数组
           Object outputObj = outputs.get("output");
           if (outputObj == null) {
               throw new Exception("outputs中没有output字段");
           }
           
           if (!(outputObj instanceof List)) {
               throw new Exception("output字段不是数组格式");
           }
           
           @SuppressWarnings("unchecked")
           List<Map<String, Object>> outputArray = (List<Map<String, Object>>) outputObj;
           
           // 步骤9: 转换为FusionRelationVO列表
           for (Map<String, Object> relationData : outputArray) {
               FusionRelationVO relation = new FusionRelationVO();
               
               // 设置type
               Object typeObj = relationData.get("type");
               if (typeObj != null) {
                   relation.setType(typeObj.toString());
               }
               
               // 设置properties
               Object propertiesObj = relationData.get("properties");
               if (propertiesObj != null && propertiesObj instanceof Map) {
                   @SuppressWarnings("unchecked")
                   Map<String, Object> properties = (Map<String, Object>) propertiesObj;
                   relation.setProperties(properties);
               }
               
               // 设置start
               Object startObj = relationData.get("start");
               if (startObj != null) {
                   relation.setStart(startObj.toString());
               }
               
               // 设置end
               Object endObj = relationData.get("end");
               if (endObj != null) {
                   relation.setEnd(endObj.toString());
               }
               
               relationList.add(relation);
               log.info("提取关系: {} -> {} ({})", relation.getStart(), relation.getEnd(), relation.getType());
           }
           
           result.put("code", 200);
           result.put("message", "Fusion工作流执行成功");
           result.put("relationCount", relationList.size());
           result.put("relations", relationList);
           
           log.info("Fusion工作流执行成功，提取 {} 个关系", relationList.size());

       } catch (Exception e) {
           log.error("Fusion工作流执行失败", e);
           result.put("code", 500);
           result.put("message", "Fusion工作流执行失败: " + e.getMessage());
           result.put("relationCount", 0);
           result.put("relations", relationList);
       }

       return result;
   }

   /**
    * 解析SSE（Server-Sent Events）流式响应，提取outputs数据（通用方法）
    * @param sseResponse SSE格式的响应字符串
    * @param objectMapper JSON解析器
    * @return outputs数据对象（Map格式），各接口可以从中提取自己需要的字段
    * @throws Exception 解析失败时抛出异常
    */
   private Map<String, Object> parseSseResponse(String sseResponse, ObjectMapper objectMapper) throws Exception {
       if (sseResponse == null || sseResponse.isEmpty()) {
           throw new Exception("SSE响应为空");
       }
       
       String[] lines = sseResponse.split("\n");
       JsonNode workflowFinishedNode = null;
       
       // 遍历所有行，每行格式为 "data: {...}"
       for (String line : lines) {
           line = line.trim();
           
           // 跳过空行
           if (line.isEmpty()) {
               continue;
           }
           
           // SSE格式：每行以 "data: " 开头
           if (line.startsWith("data: ")) {
               // 去掉 "data: " 前缀，获取JSON内容
               String jsonContent = line.substring(6);
               
               try {
                   // 解析JSON对象
                   JsonNode jsonNode = objectMapper.readTree(jsonContent);
                   
                   // 检查是否有event字段
                   if (jsonNode.has("event")) {
                       String eventType = jsonNode.get("event").asText();
                       
                       // 找到workflow_finished事件
                       if ("workflow_finished".equals(eventType)) {
                           workflowFinishedNode = jsonNode;
                           log.info("找到workflow_finished事件");
                           break;  // 找到后直接退出循环
                       }
                   }
               } catch (Exception e) {
                   // 忽略无法解析的行，继续处理下一行
                   log.debug("跳过无法解析的行: {}", e.getMessage());
               }
           }
       }
       
       if (workflowFinishedNode == null) {
           throw new Exception("无法从SSE响应中找到workflow_finished事件");
       }
       
       // 从workflow_finished事件的JSON对象中提取data.outputs（通用）
       if (!workflowFinishedNode.has("data")) {
           throw new Exception("workflow_finished事件中没有data字段");
       }
       
       JsonNode dataNode = workflowFinishedNode.get("data");
       if (!dataNode.has("outputs")) {
           throw new Exception("workflow_finished事件的data中没有outputs字段");
       }
       
       JsonNode outputsNode = dataNode.get("outputs");
       log.info("成功从workflow_finished事件中提取outputs数据");
       
       // 将outputs转换为Map返回，让各个接口自己提取需要的字段
       @SuppressWarnings("unchecked")
       Map<String, Object> outputsMap = objectMapper.convertValue(outputsNode, Map.class);
       return outputsMap;
   }
}
