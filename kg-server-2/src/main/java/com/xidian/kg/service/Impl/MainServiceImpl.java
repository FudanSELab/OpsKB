package com.xidian.kg.service.Impl;

import com.xidian.kg.controller.util.Result;
import com.xidian.kg.dao.NodeDao;
import com.xidian.kg.dao.RelationDao;
import com.xidian.kg.entity.BasicNode;
import com.xidian.kg.entity.BasicRelationReturnVO;
import com.xidian.kg.service.MainService;
import com.xidian.kg.util.DeleteFile;
import com.xidian.kg.util.NodeCsvToJson;
import com.xidian.kg.util.RelationCsvToJson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Service
public class

MainServiceImpl implements MainService {

    @Autowired
    private NodeDao nodeDao;

    @Autowired
    private RelationDao relationDao;

    @Value("${neo4j.import.csv-dir:/Users/ethanyuan/tools/neo4j-community-5/import/csv/}")
    private String csvFolder;

    @Value("${neo4j.import.json-dir:/Users/ethanyuan/tools/neo4j-community-5/import/json/}")
    private String jsonFolder;
    /**
     * 获取所有节点信息和关系信息
     * @return
     */
    @Override
    public Result getAllNodesAndRelations() {
        List<List> result = new ArrayList<>();
        try{
            List<BasicNode> allNodes = nodeDao.getAllNodes();
            System.out.println("allNodes.size:" + allNodes.size());
            HashSet<String> nodeName = new HashSet<>();
            for (int i = 0; i < allNodes.size(); i++) {
                nodeName.add((String) allNodes.get(i).getProperties().get("name"));
            }
            List<BasicRelationReturnVO> allRelations = relationDao.getAllRelations();
            for (int i = 0; i < allRelations.size(); i++) {
                BasicRelationReturnVO relationReturnVO = allRelations.get(i);
                if (!nodeName.contains(relationReturnVO.getStart().getProperties().get("name"))){
                    allRelations.remove(i);
                    i--;
                    continue;
                }
                if (!nodeName.contains(relationReturnVO.getEnd().getProperties().get("name"))){
                    allRelations.remove(i);
                    i--;
                }
            }
            result.add(allNodes);
            result.add(allRelations);
        }catch (Exception e){
            e.printStackTrace();
            return new Result(false,"发生错误，请稍后查询");
        }
        return new Result(true,result);
    }

    /**
     * 加载前端传来的文件，并将文件内容写入知识库
     * @param file 前端传来的文件
     * @param type 文件中的内容是节点信息还是关系信息，node表示节点信息，relation表示关系信息
     * @return
     * @throws Exception
     */
    public Result loadFromCSV(MultipartFile file, String type) throws Exception {
        // 存放上传及转化后文件的位置，设置在neo4j安装目录下的import/csv和import/json中
//        String csvFolder = "D:\\Tools\\Package\\neo4j-community-3.5.31\\import\\csv\\";
//        String jsonFolder = "D:\\Tools\\Package\\neo4j-community-3.5.31\\import\\json\\";
//        String csvFolder = "/home/benin/neo4j/import/csv/";
//        String jsonFolder = "/home/benin/neo4j/import/json/";
        // 清空csv和json文件夹
        DeleteFile.deleteFile(new File(csvFolder));
        DeleteFile.deleteFile(new File(jsonFolder));
        // 首先将文件写入到D:\neo4j\neo4j-community-3.5.31\import\csv下
        String fileName = file.getOriginalFilename();
        String csvPath = csvFolder+fileName;
        File dest = new File(csvPath);
        file.transferTo(dest);
        System.out.println("csv文件已保存到:"+dest.getAbsolutePath());
        // 将此文件转化为json, 并存储到D:\neo4j\neo4j-community-3.5.31\import\json下
        String jsonPath = jsonFolder+fileName+".json";
        if(type.equals("node")){
            NodeCsvToJson.NodeConvertToJson(new FileInputStream(csvPath),new FileOutputStream(jsonPath));
        }else if(type.equals("relation")){
            RelationCsvToJson.RelationConvertToJson(new FileInputStream(csvPath),new FileOutputStream(jsonPath));
        }else{
            return new Result(false,"数据错误");
        }
        System.out.println("转化json文件已保存到:"+jsonPath);
        // 读取json文件内容并写入知识库
        Long infoCount = 0L;
        String result = "";
        if(type.equals("node")){
            infoCount = nodeDao.loadNodeFromJson("file:///json/"+fileName+".json");
            result = "导入成功，共导入了"+infoCount+"个节点";
        }else if(type.equals("relation")){
            infoCount = relationDao.loadRelationFromJson("file:///json/"+fileName+".json");
            result = "导入成功，共导入了"+infoCount+"条关系";
        }else{
            return new Result(false,"数据错误");
        }
        return new Result(true,result);
    }
}
