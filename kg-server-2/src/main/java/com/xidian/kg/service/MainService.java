package com.xidian.kg.service;

import com.xidian.kg.controller.util.Result;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

public interface MainService {

    /**
     * 获取所有节点信息和关系信息
     * @return
     */
    public Result getAllNodesAndRelations();

    /**
     * 读取前端上传的文件内容，并将文件中的节点或关系添加到知识库
     * @param file 前端上传的文件
     * @return
     */
    public Result loadFromCSV(MultipartFile file, String type, String kbId) throws Exception;
}
