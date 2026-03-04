package com.xidian.kg.util;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

// CSV 转 json
// 使用csvToJSon对象的.ConvertToJson方法 带入 csv文件路径及导出路径。
public class NodeCsvToJson {
    public static void NodeConvertToJson(InputStream filePath, OutputStream outPutPath) throws Exception {
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
                
                for (int i = 0; i < headers.length; i++) {
                    if (i == 0) {
                        // 处理 labels 字段
                        String labelsHead = "\"labels\":";
                        sBuilder.append(labelsHead);
                        String sLabel = lineDataList.get(i);
                        
                        if (sLabel.indexOf(";") > 0) {
                            // labels 包含分号，分割成多个标签
                            String[] s = sLabel.split(";");
                            String labels = "[";
                            for (int n = 0; n < s.length; n++) {
                                labels += "\"" + s[n].trim() + "\"";
                                if (n < s.length - 1) {
                                    labels += ",";
                                }
                            }
                            labels += "],";
                            sBuilder.append(labels);
                        } else {
                            String labels = String.format("[\"%s\"],", sLabel);
                            sBuilder.append(labels);
                        }
                    } else {
                        // 处理 properties 字段
                        if (i == 1) {
                            sBuilder.append("\"properties\":{");
                        }
                        
                        String headerName = headers[i];
                        String value = lineDataList.get(i);
                        
                        // 跳过空值
                        if (value.equals("")) {
                            if (i == headers.length - 1) {
                                // 最后一个字段为空，删除多余的逗号和空格
                                if (sBuilder.charAt(sBuilder.length() - 1) == ' ') {
                                    sBuilder.deleteCharAt(sBuilder.length() - 1);
                                }
                                if (sBuilder.charAt(sBuilder.length() - 1) == ',') {
                                    sBuilder.deleteCharAt(sBuilder.length() - 1);
                                }
                                sBuilder.append("}");
                            }
                            continue;
                        }
                        
                        // 转义值中的双引号和反斜杠
                        String escapedValue = value.replace("\\", "\\\\").replace("\"", "\\\"");
                        String property = String.format("\"%s\": \"%s\"", headerName, escapedValue);
                        sBuilder.append(property);
                        
                        if (i != headers.length - 1) {
                            sBuilder.append(", ");
                        } else {
                            sBuilder.append("}");
                        }
                    }
                }
                
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
//        InputStream filePath = new FileInputStream("C:\\Users\\Daaaa\\Desktop\\111.csv");
//        OutputStream outPutPath = new FileOutputStream("C:\\Users\\Daaaa\\Desktop\\111.json");
//        NodeCsvToJson csvToJSon = new NodeCsvToJson();
//        csvToJSon.NodeConvertToJson(filePath, outPutPath);
//        System.out.println("转换完成");
//    }

}
