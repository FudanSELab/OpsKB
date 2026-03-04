package com.xidian.kg.util;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class RelationCsvToJson {
    public static void RelationConvertToJson(InputStream filePath, OutputStream outPutPath) throws Exception {
        //创建使用BufferedWriter 来写文件
        OutputStreamWriter osw = new OutputStreamWriter(outPutPath, "utf-8");
        BufferedWriter writer = new BufferedWriter(osw);
        
        //使用 OpenCSV 来读取 CSV 文件
        InputStreamReader isr = new InputStreamReader(filePath, "utf-8");
        CSVReader csvReader = new CSVReaderBuilder(isr).build();
        
        try {
            //读取所有行
            List<String[]> allRows = csvReader.readAll();
            
            if (allRows.isEmpty()) {
                System.out.println("CSV文件为空");
                return;
            }
            
            //获取表头
            String[] headers = allRows.get(0);
            if (headers.length == 0) {
                System.out.println("表格头不能为空");
                return;
            }
            
            //Json格式拼接
            StringBuilder sBuilder = new StringBuilder();
            sBuilder.append("[");
            
            //处理每一行数据（跳过表头）
            for (int rowIndex = 1; rowIndex < allRows.size(); rowIndex++) {
                String[] lineData = allRows.get(rowIndex);
                
                //如果行数据长度小于表头长度，补充空字符串
                List<String> lineDataList = new ArrayList<>();
                for (int i = 0; i < headers.length; i++) {
                    if (i < lineData.length) {
                        lineDataList.add(lineData[i]);
                    } else {
                        lineDataList.add("");
                    }
                }
                
                sBuilder.append("{");
                
                boolean hasProperties = false;
                
                for (int i = 0; i < headers.length; i++) {
                    String headerName = headers[i];
                    String value = lineDataList.get(i);
                    
                    if (i == 0) {
                        // 处理 type 字段
                        String escapedValue = value.replace("\\", "\\\\").replace("\"", "\\\"");
                        sBuilder.append(String.format("\"type\":\"%s\"", escapedValue));
                    } else if (i == 1) {
                        // 处理 start 字段
                        String escapedValue = value.replace("\\", "\\\\").replace("\"", "\\\"");
                        sBuilder.append(String.format(", \"start\": \"%s\"", escapedValue));
                    } else if (i == 2) {
                        // 处理 end 字段
                        String escapedValue = value.replace("\\", "\\\\").replace("\"", "\\\"");
                        sBuilder.append(String.format(", \"end\": \"%s\"", escapedValue));
                    } else {
                        // 从第4列开始处理 properties 字段
                        if (value.equals("")) {
                            continue; // 跳过空值
                        }
                        
                        if (!hasProperties) {
                            sBuilder.append(", \"properties\":{");
                            hasProperties = true;
                        } else {
                            sBuilder.append(", ");
                        }
                        
                        // 转义值中的双引号和反斜杠
                        String escapedValue = value.replace("\\", "\\\\").replace("\"", "\\\"");
                        sBuilder.append(String.format("\"%s\": \"%s\"", headerName, escapedValue));
                    }
                }
                
                // 关闭 properties（如果有的话）
                if (hasProperties) {
                    sBuilder.append("}");
                }
                
                // 关闭整个对象
                sBuilder.append("}");
                
                if (rowIndex < allRows.size() - 1) {
                    sBuilder.append(",");
                }
            }
            
            sBuilder.append("]");
            String jsonStr = sBuilder.toString();
            writer.write(jsonStr);
            writer.write("\r\n");
            
        } catch (CsvException e) {
            throw new Exception("CSV解析错误: " + e.getMessage(), e);
        } finally {
            if (csvReader != null) {
                csvReader.close();
            }
            if (writer != null) {
                writer.close();
            }
        }
    }

//    public static void main(String[] args) throws Exception {
//        //使用Java IO流操作打开/导出文件
//        InputStream filePath = new FileInputStream("C:\\Users\\Daaaa\\Desktop\\relation.csv");
//        OutputStream outPutPath = new FileOutputStream("C:\\Users\\Daaaa\\Desktop\\relation.json");
//        RelationCsvToJson csvToJSon = new RelationCsvToJson();
//        csvToJSon.RelationConvertToJson(filePath, outPutPath);
//        System.out.println("转换完成");
//    }
}
