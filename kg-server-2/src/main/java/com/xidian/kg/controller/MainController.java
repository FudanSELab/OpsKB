package com.xidian.kg.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xidian.kg.controller.util.Result;
import com.xidian.kg.service.ExportService;
import com.xidian.kg.service.MainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Controller
@ResponseBody
@RequestMapping("/main")
@CrossOrigin
public class MainController {

    @Autowired
    private MainService mainService;
    @Autowired
    private ExportService exportService;
    private ObjectMapper mapper = new ObjectMapper();


    /**
     * 向前端返回所有节点信息和关系信息
     */
    @RequestMapping("/getAll")
    public String getAllNodesAndRelations() throws JsonProcessingException {
        log.info("获取所有节点和关系信息");
        Result allNodesAndRelations = mainService.getAllNodesAndRelations();
        log.info("获取所有节点和关系信息完成，结果: {}", allNodesAndRelations.isFlag() ? "成功" : "失败");
        return mapper.writeValueAsString(allNodesAndRelations);
    }

    /**
     * 加载前端传来的文件，并将文件内容写入知识库
     * @param file 前端传来的文件
     * @param type 文件中的内容是节点信息还是关系信息，node表示节点信息，relation表示关系信息
     * @return
     * @throws Exception
     */
    @RequestMapping("/loadFromCSV")
    public String loadNodeFromCSV(@RequestParam MultipartFile file, @RequestParam String type,
                                  @RequestParam(required = false) String kbId) throws Exception {
        log.info("从CSV文件加载数据，文件名: {}, 类型: {}, kbId: {}", file != null ? file.getOriginalFilename() : "null", type, kbId);

        if (file == null || ObjectUtils.isEmpty(file) || file.getSize() <= 0) {
            log.warn("文件为空，加载失败");
            return mapper.writeValueAsString(new Result(false,"文件为空，请重新上传"));
        }

        String fileName = file.getOriginalFilename();
        if(fileName == null || !fileName.endsWith(".csv")){
            log.warn("文件类型错误，文件名: {}", fileName);
            return mapper.writeValueAsString(new Result(false,"文件类型错误，请上传csv文件"));
        }
        if(!type.equals("node") && !type.equals("relation")){
            log.warn("数据类型错误: {}", type);
            return mapper.writeValueAsString(new Result(false,"数据访问错误"));
        }
        Result result = mainService.loadFromCSV(file,type,kbId);
        // 新增节点后执行数据导出
        if (result.isFlag()) {
            log.info("CSV数据加载成功，开始导出数据");
            exportService.exportData();
        } else {
            log.warn("CSV数据加载失败");
        }
        return mapper.writeValueAsString(result);
    }

}
