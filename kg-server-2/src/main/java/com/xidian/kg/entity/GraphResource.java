package com.xidian.kg.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GraphResource {

    private static ObjectMapper mapper = new ObjectMapper();

    public static Map<String, Object> graphData;

    static {
        graphData = new HashMap<>();
        Object curveness = 0;
        // 经验预案类图1
        Map<String, Object> graph1_experience = new HashMap<>();
        List<Map> nodes1_experience = new ArrayList<>(5);
        nodes1_experience.add(0,new HashMap(){
            {
                put("name","中继无人机c1失效");
                put("x",400);
                put("y",100);
            }
        });
        nodes1_experience.add(1,new HashMap(){
            {
                put("name","c1=f1(S1, E1, S2, E2)");
                put("x",400);
                put("y",200);
            }
        });
        nodes1_experience.add(2,new HashMap(){
            {
                put("name","c1 > 0.9");
                put("x",400);
                put("y",300);
            }
        });
        nodes1_experience.add(3,new HashMap(){
            {
                put("name","用卫星通信c2进行代替通信，利用短波电台c3进行备份通信");
                put("x",400);
                put("y",400);
            }
        });
        nodes1_experience.add(4,new HashMap(){
            {
                put("name","结束");
                put("x",400);
                put("y",500);
            }
        });
        List<Map<String,Object>> links1_experience = new ArrayList<>(5);
        links1_experience.add(0,new HashMap(){
            {
                put("source","中继无人机c1失效");
                put("target","c1=f1(S1, E1, S2, E2)");
            }
        });
        links1_experience.add(1,new HashMap(){
            {
                put("source","c1=f1(S1, E1, S2, E2)");
                put("target","c1 > 0.9");
            }
        });
        links1_experience.add(2,new HashMap(){
            {
                put("source","c1 > 0.9");
                put("target","用卫星通信c2进行代替通信，利用短波电台c3进行备份通信");
                put("formatter","是");
            }
        });
        //
        links1_experience.add(3,new HashMap(){
            {
                put("source","c1 > 0.9");
                put("target","结束");
                put("formatter","否");
                put("curveness",1);
            }
        });
        links1_experience.add(4,new HashMap(){
            {
                put("source","用卫星通信c2进行代替通信，利用短波电台c3进行备份通信");
                put("target","结束");
            }
        });
        graph1_experience.put("nodes",nodes1_experience);
        graph1_experience.put("links",links1_experience);
        graphData.put("通信类资源失效预案1",graph1_experience);
        // 经验预案类图2
        Map<String, Object> graph2_experience = new HashMap<>();
        List<Map> nodes2_experience = new ArrayList<>(5);
        nodes2_experience.add(0,new HashMap(){
            {
                put("name","侦察无人机O1失效");
                put("x",400);
                put("y",100);
            }
        });
        nodes2_experience.add(1,new HashMap(){
            {
                put("name","c2=f2(S1, E1, S2, E2)");
                put("x",400);
                put("y",200);
            }
        });
        nodes2_experience.add(2,new HashMap(){
            {
                put("name","c2 > 0.95");
                put("x",400);
                put("y",300);
            }
        });
        nodes2_experience.add(3,new HashMap(){
            {
                put("name","采用预警机O2进行补盲");
                put("x",400);
                put("y",400);
            }
        });
        nodes2_experience.add(4,new HashMap(){
            {
                put("name","结束");
                put("x",400);
                put("y",500);
            }
        });
        List<Map<String,Object>> links2_experience = new ArrayList<>(5);
        links2_experience.add(0,new HashMap(){
            {
                put("source","侦察无人机O1失效");
                put("target","c2=f2(S1, E1, S2, E2)");
            }
        });
        links2_experience.add(1,new HashMap(){
            {
                put("source","c2=f2(S1, E1, S2, E2)");
                put("target","c2 > 0.95");
            }
        });
        links2_experience.add(2,new HashMap(){
            {
                put("source","c2 > 0.95");
                put("target","采用预警机O2进行补盲");
                put("formatter","是");
            }
        });
        links2_experience.add(3,new HashMap(){
            {
                put("source","c2 > 0.95");
                put("target","结束");
                put("formatter","否");
                put("curveness",1);
            }
        });
        links2_experience.add(4,new HashMap(){
            {
                put("source","采用预警机O2进行补盲");
                put("target","结束");
            }
        });
        graph2_experience.put("nodes",nodes2_experience);
        graph2_experience.put("links",links2_experience);
        graphData.put("传感探测类资源失效预案",graph2_experience);
        // 经验预案类图3
        Map<String, Object> graph3_experience = new HashMap<>();
        List<Map> nodes3_experience = new ArrayList<>(5);
        nodes3_experience.add(0,new HashMap(){
            {
                put("name","情报处理P1中心失效");
                put("x",400);
                put("y",100);
            }
        });
        nodes3_experience.add(1,new HashMap(){
            {
                put("name","c3=f3(S1, E1, S2, E2)");
                put("x",400);
                put("y",200);
            }
        });
        nodes3_experience.add(2,new HashMap(){
            {
                put("name","c3 > 0.8");
                put("x",400);
                put("y",300);
            }
        });
        nodes3_experience.add(3,new HashMap(){
            {
                put("name","采用情报处理P2进行接替");
                put("x",400);
                put("y",400);
            }
        });
        nodes3_experience.add(4,new HashMap(){
            {
                put("name","结束");
                put("x",400);
                put("y",500);
            }
        });
        List<Map<String,Object>> links3_experience = new ArrayList<>(5);
        links3_experience.add(0,new HashMap(){
            {
                put("source","情报处理P1中心失效");
                put("target","c3=f3(S1, E1, S2, E2)");
            }
        });
        links3_experience.add(1,new HashMap(){
            {
                put("source","c3=f3(S1, E1, S2, E2)");
                put("target","c3 > 0.8");
            }
        });
        links3_experience.add(2,new HashMap(){
            {
                put("source","c3 > 0.8");
                put("target","采用情报处理P2进行接替");
                put("formatter","是");
            }
        });
        links3_experience.add(3,new HashMap(){
            {
                put("source","c3 > 0.8");
                put("target","结束");
                put("formatter","否");
                put("curveness",1);
            }
        });
        links3_experience.add(4,new HashMap(){
            {
                put("source","采用情报处理P2进行接替");
                put("target","结束");
            }
        });
        graph3_experience.put("nodes",nodes3_experience);
        graph3_experience.put("links",links3_experience);
        graphData.put("情报处理类资源失效预案",graph3_experience);
        // 经验预案类图4
        Map<String, Object> graph4_experience = new HashMap<>();
        List<Map> nodes4_experience = new ArrayList<>(5);
        nodes4_experience.add(0,new HashMap(){
            {
                put("name","航空兵指挥所D1失效");
                put("x",400);
                put("y",100);
            }
        });
        nodes4_experience.add(1,new HashMap(){
            {
                put("name","c4=f4(S1, E1, S2, E2)");
                put("x",400);
                put("y",200);
            }
        });
        nodes4_experience.add(2,new HashMap(){
            {
                put("name","c4 > 0.95");
                put("x",400);
                put("y",300);
            }
        });
        nodes4_experience.add(3,new HashMap(){
            {
                put("name","采用空防作战基地指挥所D2进行接替");
                put("x",400);
                put("y",400);
            }
        });
        nodes4_experience.add(4,new HashMap(){
            {
                put("name","结束");
                put("x",400);
                put("y",500);
            }
        });
        List<Map<String,Object>> links4_experience = new ArrayList<>(5);
        links4_experience.add(0,new HashMap(){
            {
                put("source","航空兵指挥所D1失效");
                put("target","c4=f4(S1, E1, S2, E2)");
            }
        });
        links4_experience.add(1,new HashMap(){
            {
                put("source","c4=f4(S1, E1, S2, E2)");
                put("target","c4 > 0.95");
            }
        });
        links4_experience.add(2,new HashMap(){
            {
                put("source","c4 > 0.95");
                put("target","采用空防作战基地指挥所D2进行接替");
                put("formatter","是");
            }
        });
        links4_experience.add(3,new HashMap(){
            {
                put("source","c4 > 0.95");
                put("target","结束");
                put("formatter","否");
                put("curveness",1);
            }
        });
        links4_experience.add(4,new HashMap(){
            {
                put("source","采用空防作战基地指挥所D2进行接替");
                put("target","结束");
            }
        });
        graph4_experience.put("nodes",nodes4_experience);
        graph4_experience.put("links",links4_experience);
        graphData.put("决策控制类资源失效预案1",graph4_experience);
        // 经验预案类图5
        Map<String, Object> graph5_experience = new HashMap<>();
        List<Map> nodes5_experience = new ArrayList<>(5);
        nodes5_experience.add(0,new HashMap(){
            {
                put("name","空防作战基地指挥所D2失效");
                put("x",400);
                put("y",100);
            }
        });
        nodes5_experience.add(1,new HashMap(){
            {
                put("name","c5=f5(S1, E1, S2, E2)");
                put("x",400);
                put("y",200);
            }
        });
        nodes5_experience.add(2,new HashMap(){
            {
                put("name","c5 > 0.95");
                put("x",400);
                put("y",300);
            }
        });
        nodes5_experience.add(3,new HashMap(){
            {
                put("name","采用机动合成指挥所系统D3进行接替");
                put("x",400);
                put("y",400);
            }
        });
        nodes5_experience.add(4,new HashMap(){
            {
                put("name","结束");
                put("x",400);
                put("y",500);
            }
        });
        List<Map<String,Object>> links5_experience = new ArrayList<>(5);
        links5_experience.add(0,new HashMap(){
            {
                put("source","空防作战基地指挥所D2失效");
                put("target","c5=f5(S1, E1, S2, E2)");
            }
        });
        links5_experience.add(1,new HashMap(){
            {
                put("source","c5=f5(S1, E1, S2, E2)");
                put("target","c5 > 0.95");
            }
        });
        links5_experience.add(2,new HashMap(){
            {
                put("source","c5 > 0.95");
                put("target","采用机动合成指挥所系统D3进行接替");
                put("formatter","是");
            }
        });
        links5_experience.add(3,new HashMap(){
            {
                put("source","c5 > 0.95");
                put("target","结束");
                put("formatter","否");
                put("curveness",1);
            }
        });
        links5_experience.add(4,new HashMap(){
            {
                put("source","采用机动合成指挥所系统D3进行接替");
                put("target","结束");
            }
        });
        graph5_experience.put("nodes",nodes5_experience);
        graph5_experience.put("links",links5_experience);
        graphData.put("决策控制类资源失效预案2",graph5_experience);
        // 经验预案类图6
        Map<String, Object> graph6_experience = new HashMap<>();
        List<Map> nodes6_experience = new ArrayList<>(5);
        nodes6_experience.add(0,new HashMap(){
            {
                put("name","对敌歼击机A1失效");
                put("x",400);
                put("y",100);
            }
        });
        nodes6_experience.add(1,new HashMap(){
            {
                put("name","c6=f6(S1, E1, S2, E2)");
                put("x",400);
                put("y",200);
            }
        });
        nodes6_experience.add(2,new HashMap(){
            {
                put("name","c6 > 0.8");
                put("x",400);
                put("y",300);
            }
        });
        nodes6_experience.add(3,new HashMap(){
            {
                put("name","采用护卫舰A2代替打击工作");
                put("x",400);
                put("y",400);
            }
        });
        nodes6_experience.add(4,new HashMap(){
            {
                put("name","结束");
                put("x",400);
                put("y",500);
            }
        });
        List<Map<String,Object>> links6_experience = new ArrayList<>(5);
        links6_experience.add(0,new HashMap(){
            {
                put("source","对敌歼击机A1失效");
                put("target","c6=f6(S1, E1, S2, E2)");
            }
        });
        links6_experience.add(1,new HashMap(){
            {
                put("source","c6=f6(S1, E1, S2, E2)");
                put("target","c6 > 0.8");
            }
        });
        links6_experience.add(2,new HashMap(){
            {
                put("source","c6 > 0.8");
                put("target","采用护卫舰A2代替打击工作");
                put("formatter","是");
            }
        });
        links6_experience.add(3,new HashMap(){
            {
                put("source","c6 > 0.8");
                put("target","结束");
                put("formatter","否");
                put("curveness",1);
            }
        });
        links6_experience.add(4,new HashMap(){
            {
                put("source","采用护卫舰A2代替打击工作");
                put("target","结束");
            }
        });
        graph6_experience.put("nodes",nodes6_experience);
        graph6_experience.put("links",links6_experience);
        graphData.put("兵力火力类资源失效预案",graph6_experience);
        // 经验预案类图7
        Map<String, Object> graph7_experience = new HashMap<>();
        List<Map> nodes7_experience = new ArrayList<>(5);
        nodes7_experience.add(0,new HashMap(){
            {
                put("name","新增打击任务目标M1");
                put("x",400);
                put("y",100);
            }
        });
        nodes7_experience.add(1,new HashMap(){
            {
                put("name","c7=f7(S1, E1, S2, E2)");
                put("x",400);
                put("y",200);
            }
        });
        nodes7_experience.add(2,new HashMap(){
            {
                put("name","c7 > 0.8");
                put("x",400);
                put("y",300);
            }
        });
        nodes7_experience.add(3,new HashMap(){
            {
                put("name","用对地歼击机A3执行打击工作");
                put("x",400);
                put("y",400);
            }
        });
        nodes7_experience.add(4,new HashMap(){
            {
                put("name","结束");
                put("x",400);
                put("y",500);
            }
        });
        List<Map<String,Object>> links7_experience = new ArrayList<>(5);
        links7_experience.add(0,new HashMap(){
            {
                put("source","新增打击任务目标M1");
                put("target","c7=f7(S1, E1, S2, E2)");
            }
        });
        links7_experience.add(1,new HashMap(){
            {
                put("source","c7=f7(S1, E1, S2, E2)");
                put("target","c7 > 0.8");
            }
        });
        links7_experience.add(2,new HashMap(){
            {
                put("source","c7 > 0.8");
                put("target","用对地歼击机A3执行打击工作");
                put("formatter","是");
            }
        });
        links7_experience.add(3,new HashMap(){
            {
                put("source","c7 > 0.8");
                put("target","结束");
                put("formatter","否");
                put("curveness",1);
            }
        });
        links7_experience.add(4,new HashMap(){
            {
                put("source","用对地歼击机A3执行打击工作");
                put("target","结束");
            }
        });
        graph7_experience.put("nodes",nodes7_experience);
        graph7_experience.put("links",links7_experience);
        graphData.put("新增打击目标预案",graph7_experience);
        // 经验预案类图8
        Map<String, Object> graph8_experience = new HashMap<>();
        List<Map> nodes8_experience = new ArrayList<>(5);
        nodes8_experience.add(0,new HashMap(){
            {
                put("name","新增探测任务目标M2");
                put("x",400);
                put("y",100);
            }
        });
        nodes8_experience.add(1,new HashMap(){
            {
                put("name","c8=f8(S1, E1, S2, E2)");
                put("x",400);
                put("y",200);
            }
        });
        nodes8_experience.add(2,new HashMap(){
            {
                put("name","c8 > 0.9");
                put("x",400);
                put("y",300);
            }
        });
        nodes8_experience.add(3,new HashMap(){
            {
                put("name","采用侦察无人机O3执行探测工作");
                put("x",400);
                put("y",400);
            }
        });
        nodes8_experience.add(4,new HashMap(){
            {
                put("name","结束");
                put("x",400);
                put("y",500);
            }
        });
        List<Map<String,Object>> links8_experience = new ArrayList<>(5);
        links8_experience.add(0,new HashMap(){
            {
                put("source","新增探测任务目标M2");
                put("target","c8=f8(S1, E1, S2, E2)");
            }
        });
        links8_experience.add(1,new HashMap(){
            {
                put("source","c8=f8(S1, E1, S2, E2)");
                put("target","c8 > 0.9");
            }
        });
        links8_experience.add(2,new HashMap(){
            {
                put("source","c8 > 0.9");
                put("target","采用侦察无人机O3执行探测工作");
                put("formatter","是");
            }
        });
        links8_experience.add(3,new HashMap(){
            {
                put("source","c8 > 0.9");
                put("target","结束");
                put("formatter","否");
                put("curveness",1);
            }
        });
        links8_experience.add(4,new HashMap(){
            {
                put("source","采用侦察无人机O3执行探测工作");
                put("target","结束");
            }
        });
        graph8_experience.put("nodes",nodes8_experience);
        graph8_experience.put("links",links8_experience);
        graphData.put("新增探测目标预案",graph8_experience);
        // 经验预案类图9
        Map<String, Object> graph9_experience = new HashMap<>();
        List<Map> nodes9_experience = new ArrayList<>(5);
        nodes9_experience.add(0,new HashMap(){
            {
                put("name","区域K产生电磁干扰");
                put("x",400);
                put("y",100);
            }
        });
        nodes9_experience.add(1,new HashMap(){
            {
                put("name","c9=f9(S1, E1, S2, E2)");
                put("x",400);
                put("y",200);
            }
        });
        nodes9_experience.add(2,new HashMap(){
            {
                put("name","c9 > 0.9");
                put("x",400);
                put("y",300);
            }
        });
        nodes9_experience.add(3,new HashMap(){
            {
                put("name","采用卫星通信C2进行代替通信，采用护卫舰A3进行电磁抗干扰");
                put("x",400);
                put("y",400);
            }
        });
        nodes9_experience.add(4,new HashMap(){
            {
                put("name","结束");
                put("x",400);
                put("y",500);
            }
        });
        List<Map<String,Object>> links9_experience = new ArrayList<>(5);
        links9_experience.add(0,new HashMap(){
            {
                put("source","区域K产生电磁干扰");
                put("target","c9=f9(S1, E1, S2, E2)");
            }
        });
        links9_experience.add(1,new HashMap(){
            {
                put("source","c9=f9(S1, E1, S2, E2)");
                put("target","c9 > 0.9");
            }
        });
        links9_experience.add(2,new HashMap(){
            {
                put("source","c9 > 0.9");
                put("target","采用卫星通信C2进行代替通信，采用护卫舰A3进行电磁抗干扰");
                put("formatter","是");
            }
        });
        links9_experience.add(3,new HashMap(){
            {
                put("source","c9 > 0.9");
                put("target","结束");
                put("formatter","否");
                put("curveness",1);
            }
        });
        links9_experience.add(4,new HashMap(){
            {
                put("source","采用卫星通信C2进行代替通信，采用护卫舰A3进行电磁抗干扰");
                put("target","结束");
            }
        });
        graph9_experience.put("nodes",nodes9_experience);
        graph9_experience.put("links",links9_experience);
        graphData.put("通信类资源失效预案2",graph9_experience);
        // 策略规则类图1
        Map<String, Object> graph1_rule = new HashMap<>();
        List<Map> nodes1_rule = new ArrayList<>(7);
        nodes1_rule.add(0,new HashMap(){
            {
                put("name","首选策略");
                put("x",400);
                put("y",100);
            }
        });
        nodes1_rule.add(1,new HashMap(){
            {
                put("name","资源能力参数相似度计算");
                put("x",400);
                put("y",200);
            }
        });
        nodes1_rule.add(2,new HashMap(){
            {
                put("name","按相似度排序的资源列表：\n相似同类资源1：97%\n相似同类资源2：95%\n相似同类资源3：90%\n相似同类资源4：88%\n......");
                put("x",400);
                put("y",300);
            }
        });
        nodes1_rule.add(3,new HashMap(){
            {
                put("name","判定阈值：>=90%");
                put("x",400);
                put("y",400);
            }
        });
        nodes1_rule.add(3,new HashMap(){
            {
                put("name","可选资源池");
                put("x",350);
                put("y",500);
            }
        });
        nodes1_rule.add(4,new HashMap(){
            {
                put("name","扩展策略");
                put("x",450);
                put("y",500);
            }
        });
        nodes1_rule.add(5,new HashMap(){
            {
                put("name","遍历空闲资源池");
                put("x",450);
                put("y",600);
            }
        });
        nodes1_rule.add(6,new HashMap(){
            {
                put("name","策略选择");
                put("x",350);
                put("y",600);
            }
        });
        List<Map<String,Object>> links1_rule = new ArrayList<>(8);
        links1_rule.add(0,new HashMap(){
            {
                put("source","首选策略");
                put("target","资源能力参数相似度计算");
            }
        });
        links1_rule.add(1,new HashMap(){
            {
                put("source","资源能力参数相似度计算");
                put("target","按相似度排序的资源列表：\n相似同类资源1：97%\n相似同类资源2：95%\n相似同类资源3：90%\n相似同类资源4：88%\n......");
            }
        });
        links1_rule.add(2,new HashMap(){
            {
                put("source","按相似度排序的资源列表：\n相似同类资源1：97%\n相似同类资源2：95%\n相似同类资源3：90%\n相似同类资源4：88%\n......");
                put("target","判定阈值：>=90%");
            }
        });
        links1_rule.add(3,new HashMap(){
            {
                put("source","判定阈值：>=90%");
                put("target","可选资源池");
                put("formatter","是");
            }
        });
        links1_rule.add(4,new HashMap(){
            {
                put("source","判定阈值：>=90%");
                put("target","扩展策略");
                put("formatter","否");
            }
        });
        links1_rule.add(5,new HashMap(){
            {
                put("source","可选资源池");
                put("target","策略选择");
            }
        });
        links1_rule.add(6,new HashMap(){
            {
                put("source","扩展策略");
                put("target","遍历空闲资源池");
            }
        });
        links1_rule.add(7,new HashMap(){
            {
                put("source","遍历空闲资源池");
                put("target","可选资源池");
            }
        });
        graph1_rule.put("nodes",nodes1_rule);
        graph1_rule.put("links",links1_rule);
        graphData.put("传感探测类策略规则1",graph1_rule);
        // 策略规则类图2
        Map<String, Object> graph2_rule = new HashMap<>();
        List<Map> nodes2_rule = new ArrayList<>(7);
        nodes2_rule.add(0,new HashMap(){
            {
                put("name","首选策略");
                put("x",400);
                put("y",100);
            }
        });
        nodes2_rule.add(1,new HashMap(){
            {
                put("name","集群探测能力相似度计算");
                put("x",400);
                put("y",200);
            }
        });
        nodes2_rule.add(2,new HashMap(){
            {
                put("name","按相似度排序的集群列表：\n组合资源集群1：97%\n组合资源集群2：95%\n组合资源集群3：90%\n组合资源集群4：88%\n......");
                put("x",400);
                put("y",300);
            }
        });
        nodes2_rule.add(3,new HashMap(){
            {
                put("name","判定阈值：>=95%");
                put("x",400);
                put("y",400);
            }
        });
        nodes2_rule.add(3,new HashMap(){
            {
                put("name","可选资源池");
                put("x",350);
                put("y",500);
            }
        });
        nodes2_rule.add(4,new HashMap(){
            {
                put("name","扩展策略");
                put("x",450);
                put("y",500);
            }
        });
        nodes2_rule.add(5,new HashMap(){
            {
                put("name","遍历空闲资源池");
                put("x",450);
                put("y",600);
            }
        });
        nodes2_rule.add(6,new HashMap(){
            {
                put("name","策略选择");
                put("x",350);
                put("y",600);
            }
        });
        List<Map<String,Object>> links2_rule = new ArrayList<>(8);
        links2_rule.add(0,new HashMap(){
            {
                put("source","首选策略");
                put("target","集群探测能力相似度计算");
            }
        });
        links2_rule.add(1,new HashMap(){
            {
                put("source","集群探测能力相似度计算");
                put("target","按相似度排序的集群列表：\n组合资源集群1：97%\n组合资源集群2：95%\n组合资源集群3：90%\n组合资源集群4：88%\n......");
            }
        });
        links2_rule.add(2,new HashMap(){
            {
                put("source","按相似度排序的集群列表：\n组合资源集群1：97%\n组合资源集群2：95%\n组合资源集群3：90%\n组合资源集群4：88%\n......");
                put("target","判定阈值：>=95%");
            }
        });
        links2_rule.add(3,new HashMap(){
            {
                put("source","判定阈值：>=95%");
                put("target","可选资源池");
                put("formatter","是");
            }
        });
        links2_rule.add(4,new HashMap(){
            {
                put("source","判定阈值：>=95%");
                put("target","扩展策略");
                put("formatter","否");
            }
        });
        links2_rule.add(5,new HashMap(){
            {
                put("source","可选资源池");
                put("target","策略选择");
            }
        });
        links2_rule.add(6,new HashMap(){
            {
                put("source","扩展策略");
                put("target","遍历空闲资源池");
            }
        });
        links2_rule.add(7,new HashMap(){
            {
                put("source","遍历空闲资源池");
                put("target","可选资源池");
            }
        });
        graph2_rule.put("nodes",nodes2_rule);
        graph2_rule.put("links",links2_rule);
        graphData.put("传感探测类策略规则2",graph2_rule);

        // 策略规则类图3
        Map<String, Object> graph3_rule = new HashMap<>();
        List<Map> nodes3_rule = new ArrayList<>(7);
        nodes3_rule.add(0,new HashMap(){
            {
                put("name","首选策略");
                put("x",400);
                put("y",100);
            }
        });
        nodes3_rule.add(1,new HashMap(){
            {
                put("name","资源能力参数相似度计算");
                put("x",400);
                put("y",200);
            }
        });
        nodes3_rule.add(2,new HashMap(){
            {
                put("name","按相似度排序的资源列表：\n相似同类资源1：97%\n相似同类资源2：95%\n相似同类资源3：85%\n相似同类资源4：82%\n......");
                put("x",400);
                put("y",300);
            }
        });
        nodes3_rule.add(3,new HashMap(){
            {
                put("name","判定阈值：>=85%");
                put("x",400);
                put("y",400);
            }
        });
        nodes3_rule.add(3,new HashMap(){
            {
                put("name","可选资源池");
                put("x",350);
                put("y",500);
            }
        });
        nodes3_rule.add(4,new HashMap(){
            {
                put("name","扩展策略");
                put("x",450);
                put("y",500);
            }
        });
        nodes3_rule.add(5,new HashMap(){
            {
                put("name","遍历空闲资源池");
                put("x",450);
                put("y",600);
            }
        });
        nodes3_rule.add(6,new HashMap(){
            {
                put("name","策略选择");
                put("x",350);
                put("y",600);
            }
        });
        List<Map<String,Object>> links3_rule = new ArrayList<>(8);
        links3_rule.add(0,new HashMap(){
            {
                put("source","首选策略");
                put("target","资源能力参数相似度计算");
            }
        });
        links3_rule.add(1,new HashMap(){
            {
                put("source","资源能力参数相似度计算");
                put("target","按相似度排序的资源列表：\n相似同类资源1：97%\n相似同类资源2：95%\n相似同类资源3：85%\n相似同类资源4：82%\n......");
            }
        });
        links3_rule.add(2,new HashMap(){
            {
                put("source","按相似度排序的资源列表：\n相似同类资源1：97%\n相似同类资源2：95%\n相似同类资源3：85%\n相似同类资源4：82%\n......");
                put("target","判定阈值：>=85%");
            }
        });
        links3_rule.add(3,new HashMap(){
            {
                put("source","判定阈值：>=85%");
                put("target","可选资源池");
                put("formatter","是");
            }
        });
        links3_rule.add(4,new HashMap(){
            {
                put("source","判定阈值：>=85%");
                put("target","扩展策略");
                put("formatter","否");
            }
        });
        links3_rule.add(5,new HashMap(){
            {
                put("source","可选资源池");
                put("target","策略选择");
            }
        });
        links3_rule.add(6,new HashMap(){
            {
                put("source","扩展策略");
                put("target","遍历空闲资源池");
            }
        });
        links3_rule.add(7,new HashMap(){
            {
                put("source","遍历空闲资源池");
                put("target","可选资源池");
            }
        });
        graph3_rule.put("nodes",nodes3_rule);
        graph3_rule.put("links",links3_rule);
        graphData.put("情报处理类策略规则1",graph3_rule);

        // 策略规则类图4
        Map<String, Object> graph4_rule = new HashMap<>();
        List<Map> nodes4_rule = new ArrayList<>(7);
        nodes4_rule.add(0,new HashMap(){
            {
                put("name","首选策略");
                put("x",400);
                put("y",100);
            }
        });
        nodes4_rule.add(1,new HashMap(){
            {
                put("name","集群处理能力相似度计算");
                put("x",400);
                put("y",200);
            }
        });
        nodes4_rule.add(2,new HashMap(){
            {
                put("name","按相似度排序的集群列表：\n组合资源集群1：97%\n组合资源集群2：95%\n组合资源集群3：90%\n组合资源集群4：88%\n......");
                put("x",400);
                put("y",300);
            }
        });
        nodes4_rule.add(3,new HashMap(){
            {
                put("name","判定阈值：>=90%");
                put("x",400);
                put("y",400);
            }
        });
        nodes4_rule.add(3,new HashMap(){
            {
                put("name","可选资源池");
                put("x",350);
                put("y",500);
            }
        });
        nodes4_rule.add(4,new HashMap(){
            {
                put("name","扩展策略");
                put("x",450);
                put("y",500);
            }
        });
        nodes4_rule.add(5,new HashMap(){
            {
                put("name","遍历空闲资源池");
                put("x",450);
                put("y",600);
            }
        });
        nodes4_rule.add(6,new HashMap(){
            {
                put("name","策略选择");
                put("x",350);
                put("y",600);
            }
        });
        List<Map<String,Object>> links4_rule = new ArrayList<>(8);
        links4_rule.add(0,new HashMap(){
            {
                put("source","首选策略");
                put("target","集群处理能力相似度计算");
            }
        });
        links4_rule.add(1,new HashMap(){
            {
                put("source","集群处理能力相似度计算");
                put("target","按相似度排序的集群列表：\n组合资源集群1：97%\n组合资源集群2：95%\n组合资源集群3：90%\n组合资源集群4：88%\n......");
            }
        });
        links4_rule.add(2,new HashMap(){
            {
                put("source","按相似度排序的集群列表：\n组合资源集群1：97%\n组合资源集群2：95%\n组合资源集群3：90%\n组合资源集群4：88%\n......");
                put("target","判定阈值：>=90%");
            }
        });
        links4_rule.add(3,new HashMap(){
            {
                put("source","判定阈值：>=90%");
                put("target","可选资源池");
                put("formatter","是");
            }
        });
        links4_rule.add(4,new HashMap(){
            {
                put("source","判定阈值：>=90%");
                put("target","扩展策略");
                put("formatter","否");
            }
        });
        links4_rule.add(5,new HashMap(){
            {
                put("source","可选资源池");
                put("target","策略选择");
            }
        });
        links4_rule.add(6,new HashMap(){
            {
                put("source","扩展策略");
                put("target","遍历空闲资源池");
            }
        });
        links4_rule.add(7,new HashMap(){
            {
                put("source","遍历空闲资源池");
                put("target","可选资源池");
            }
        });
        graph4_rule.put("nodes",nodes4_rule);
        graph4_rule.put("links",links4_rule);
        graphData.put("情报处理类策略规则2",graph4_rule);

        // 策略规则类图5
        Map<String, Object> graph5_rule = new HashMap<>();
        List<Map> nodes5_rule = new ArrayList<>(6);
        nodes5_rule.add(0,new HashMap(){
            {
                put("name","首选策略");
                put("x",350);
                put("y",100);
            }
        });
        nodes5_rule.add(1,new HashMap(){
            {
                put("name","扩展策略");
                put("x",450);
                put("y",100);
            }
        });
        nodes5_rule.add(2,new HashMap(){
            {
                put("name","检索可用上级指挥所");
                put("x",350);
                put("y",200);
            }
        });
        nodes5_rule.add(3,new HashMap(){
            {
                put("name","检索可用同级指挥所");
                put("x",450);
                put("y",200);
            }
        });
        nodes5_rule.add(4,new HashMap(){
            {
                put("name","可选资源池");
                put("x",400);
                put("y",300);
            }
        });
        nodes5_rule.add(5,new HashMap(){
            {
                put("name","策略选择");
                put("x",400);
                put("y",400);
            }
        });
        List<Map<String,Object>> links5_rule = new ArrayList<>(8);
        links5_rule.add(0,new HashMap(){
            {
                put("source","首选策略");
                put("target","检索可用上级指挥所");
            }
        });
        links5_rule.add(1,new HashMap(){
            {
                put("source","扩展策略");
                put("target","检索可用同级指挥所");
            }
        });
        links5_rule.add(2,new HashMap(){
            {
                put("source","检索可用上级指挥所");
                put("target","可选资源池");
            }
        });
        links5_rule.add(3,new HashMap(){
            {
                put("source","检索可用同级指挥所");
                put("target","可选资源池");
            }
        });
        links5_rule.add(4,new HashMap(){
            {
                put("source","可选资源池");
                put("target","可选资源池");
            }
        });
        graph5_rule.put("nodes",nodes5_rule);
        graph5_rule.put("links",links5_rule);
        graphData.put("决策控制类策略规则1",graph5_rule);

        // 策略规则类图6
        Map<String, Object> graph6_rule = new HashMap<>();
        List<Map> nodes6_rule = new ArrayList<>(7);
        nodes6_rule.add(0,new HashMap(){
            {
                put("name","首选策略");
                put("x",400);
                put("y",100);
            }
        });
        nodes6_rule.add(1,new HashMap(){
            {
                put("name","集群决策能力相似度计算");
                put("x",400);
                put("y",200);
            }
        });
        nodes6_rule.add(2,new HashMap(){
            {
                put("name","按相似度排序的集群列表：\n组合资源集群1：97%\n组合资源集群2：95%\n组合资源集群3：90%\n组合资源集群4：88%\n......");
                put("x",400);
                put("y",300);
            }
        });
        nodes6_rule.add(3,new HashMap(){
            {
                put("name","判定阈值：>=85%");
                put("x",400);
                put("y",400);
            }
        });
        nodes6_rule.add(3,new HashMap(){
            {
                put("name","可选资源池");
                put("x",350);
                put("y",500);
            }
        });
        nodes6_rule.add(4,new HashMap(){
            {
                put("name","扩展策略");
                put("x",450);
                put("y",500);
            }
        });
        nodes6_rule.add(5,new HashMap(){
            {
                put("name","遍历空闲资源池");
                put("x",450);
                put("y",600);
            }
        });
        nodes6_rule.add(6,new HashMap(){
            {
                put("name","策略选择");
                put("x",350);
                put("y",600);
            }
        });
        List<Map<String,Object>> links6_rule = new ArrayList<>(8);
        links6_rule.add(0,new HashMap(){
            {
                put("source","首选策略");
                put("target","集群决策能力相似度计算");
            }
        });
        links6_rule.add(1,new HashMap(){
            {
                put("source","集群决策能力相似度计算");
                put("target","按相似度排序的集群列表：\n组合资源集群1：97%\n组合资源集群2：95%\n组合资源集群3：90%\n组合资源集群4：88%\n......");
            }
        });
        links6_rule.add(2,new HashMap(){
            {
                put("source","按相似度排序的集群列表：\n组合资源集群1：97%\n组合资源集群2：95%\n组合资源集群3：90%\n组合资源集群4：88%\n......");
                put("target","判定阈值：>=85%");
            }
        });
        links6_rule.add(3,new HashMap(){
            {
                put("source","判定阈值：>=85%");
                put("target","可选资源池");
                put("formatter","是");
            }
        });
        links6_rule.add(4,new HashMap(){
            {
                put("source","判定阈值：>=85%");
                put("target","扩展策略");
                put("formatter","否");
            }
        });
        links6_rule.add(5,new HashMap(){
            {
                put("source","可选资源池");
                put("target","策略选择");
            }
        });
        links6_rule.add(6,new HashMap(){
            {
                put("source","扩展策略");
                put("target","遍历空闲资源池");
            }
        });
        links6_rule.add(7,new HashMap(){
            {
                put("source","遍历空闲资源池");
                put("target","可选资源池");
            }
        });
        graph6_rule.put("nodes",nodes6_rule);
        graph6_rule.put("links",links6_rule);
        graphData.put("决策控制类策略规则2",graph6_rule);

        //策略规则类图7
        Map<String, Object> graph7_rule = new HashMap<>();
        List<Map> nodes7_rule = new ArrayList<>(7);
        nodes7_rule.add(0,new HashMap(){
            {
                put("name","首选策略");
                put("x",400);
                put("y",100);
            }
        });
        nodes7_rule.add(1,new HashMap(){
            {
                put("name","资源能力参数相似度计算");
                put("x",400);
                put("y",200);
            }
        });
        nodes7_rule.add(2,new HashMap(){
            {
                put("name","按相似度排序的资源列表：\n相似同类资源1：97%\n相似同类资源2：95%\n相似同类资源3：90%\n相似同类资源4：88%\n......");
                put("x",400);
                put("y",300);
            }
        });
        nodes7_rule.add(3,new HashMap(){
            {
                put("name","判定阈值：>=85%");
                put("x",400);
                put("y",400);
            }
        });
        nodes7_rule.add(3,new HashMap(){
            {
                put("name","可选资源池");
                put("x",350);
                put("y",500);
            }
        });
        nodes7_rule.add(4,new HashMap(){
            {
                put("name","扩展策略");
                put("x",450);
                put("y",500);
            }
        });
        nodes7_rule.add(5,new HashMap(){
            {
                put("name","遍历空闲资源池");
                put("x",450);
                put("y",600);
            }
        });
        nodes7_rule.add(6,new HashMap(){
            {
                put("name","策略选择");
                put("x",350);
                put("y",600);
            }
        });
        List<Map<String,Object>> links7_rule = new ArrayList<>(8);
        links7_rule.add(0,new HashMap(){
            {
                put("source","首选策略");
                put("target","资源能力参数相似度计算");
            }
        });
        links7_rule.add(1,new HashMap(){
            {
                put("source","资源能力参数相似度计算");
                put("target","按相似度排序的资源列表：\n相似同类资源1：97%\n相似同类资源2：95%\n相似同类资源3：90%\n相似同类资源4：88%\n......");
            }
        });
        links7_rule.add(2,new HashMap(){
            {
                put("source","按相似度排序的资源列表：\n相似同类资源1：97%\n相似同类资源2：95%\n相似同类资源3：90%\n相似同类资源4：88%\n......");
                put("target","判定阈值：>=90%");
            }
        });
        links7_rule.add(3,new HashMap(){
            {
                put("source","判定阈值：>=85%");
                put("target","可选资源池");
                put("formatter","是");
            }
        });
        links7_rule.add(4,new HashMap(){
            {
                put("source","判定阈值：>=85%");
                put("target","扩展策略");
                put("formatter","否");
            }
        });
        links7_rule.add(5,new HashMap(){
            {
                put("source","可选资源池");
                put("target","策略选择");
            }
        });
        links7_rule.add(6,new HashMap(){
            {
                put("source","扩展策略");
                put("target","遍历空闲资源池");
            }
        });
        links7_rule.add(7,new HashMap(){
            {
                put("source","遍历空闲资源池");
                put("target","可选资源池");
            }
        });
        graph7_rule.put("nodes",nodes7_rule);
        graph7_rule.put("links",links7_rule);
        graphData.put("兵力火力类策略规则1",graph7_rule);

        // 策略规则类图8
        Map<String, Object> graph8_rule = new HashMap<>();
        List<Map> nodes8_rule = new ArrayList<>(7);
        nodes8_rule.add(0,new HashMap(){
            {
                put("name","首选策略");
                put("x",400);
                put("y",100);
            }
        });
        nodes8_rule.add(1,new HashMap(){
            {
                put("name","集群打击能力相似度计算");
                put("x",400);
                put("y",200);
            }
        });
        nodes8_rule.add(2,new HashMap(){
            {
                put("name","按相似度排序的集群列表：\n组合资源集群1：97%\n组合资源集群2：95%\n组合资源集群3：90%\n组合资源集群4：88%\n......");
                put("x",400);
                put("y",300);
            }
        });
        nodes8_rule.add(3,new HashMap(){
            {
                put("name","判定阈值：>=90%");
                put("x",400);
                put("y",400);
            }
        });
        nodes8_rule.add(3,new HashMap(){
            {
                put("name","可选资源池");
                put("x",350);
                put("y",500);
            }
        });
        nodes8_rule.add(4,new HashMap(){
            {
                put("name","扩展策略");
                put("x",450);
                put("y",500);
            }
        });
        nodes8_rule.add(5,new HashMap(){
            {
                put("name","遍历空闲资源池");
                put("x",450);
                put("y",600);
            }
        });
        nodes8_rule.add(6,new HashMap(){
            {
                put("name","策略选择");
                put("x",350);
                put("y",600);
            }
        });
        List<Map<String,Object>> links8_rule = new ArrayList<>(8);
        links8_rule.add(0,new HashMap(){
            {
                put("source","首选策略");
                put("target","集群打击能力相似度计算");
            }
        });
        links8_rule.add(1,new HashMap(){
            {
                put("source","集群打击能力相似度计算");
                put("target","按相似度排序的集群列表：\n组合资源集群1：97%\n组合资源集群2：95%\n组合资源集群3：90%\n组合资源集群4：88%\n......");
            }
        });
        links8_rule.add(2,new HashMap(){
            {
                put("source","按相似度排序的集群列表：\n组合资源集群1：97%\n组合资源集群2：95%\n组合资源集群3：90%\n组合资源集群4：88%\n......");
                put("target","判定阈值：>=90%");
            }
        });
        links8_rule.add(3,new HashMap(){
            {
                put("source","判定阈值：>=90%");
                put("target","可选资源池");
                put("formatter","是");
            }
        });
        links8_rule.add(4,new HashMap(){
            {
                put("source","判定阈值：>=90%");
                put("target","扩展策略");
                put("formatter","否");
            }
        });
        links8_rule.add(5,new HashMap(){
            {
                put("source","可选资源池");
                put("target","策略选择");
            }
        });
        links8_rule.add(6,new HashMap(){
            {
                put("source","扩展策略");
                put("target","遍历空闲资源池");
            }
        });
        links8_rule.add(7,new HashMap(){
            {
                put("source","遍历空闲资源池");
                put("target","可选资源池");
            }
        });
        graph8_rule.put("nodes",nodes8_rule);
        graph8_rule.put("links",links8_rule);
        graphData.put("兵力火力类策略规则2",graph8_rule);

        // 策略规则类图9
        Map<String, Object> graph9_rule = new HashMap<>();
        List<Map> nodes9_rule = new ArrayList<>(4);
        nodes9_rule.add(0,new HashMap(){
            {
                put("name","首选策略");
                put("x",400);
                put("y",100);
            }
        });
        nodes9_rule.add(1,new HashMap(){
            {
                put("name","目前资源能力百分比评估");
                put("x",400);
                put("y",200);
            }
        });
        nodes9_rule.add(2,new HashMap(){
            {
                put("name","当前资源仍能支撑当前工作，不予处理。");
                put("x",400);
                put("y",300);
            }
        });
        nodes1_rule.add(3,new HashMap(){
            {
                put("name","策略应用");
                put("x",400);
                put("y",400);
            }
        });
        List<Map<String,Object>> links9_rule = new ArrayList<>(8);
        links9_rule.add(0,new HashMap(){
            {
                put("source","首选策略");
                put("target","目前资源能力百分比评估");
            }
        });
        links9_rule.add(1,new HashMap(){
            {
                put("source","目前资源能力百分比评估");
                put("target","当前资源仍能支撑当前工作，不予处理。");
            }
        });
        links9_rule.add(2,new HashMap(){
            {
                put("source","当前资源仍能支撑当前工作，不予处理。");
                put("target","策略应用");
            }
        });
        graph9_rule.put("nodes",nodes9_rule);
        graph9_rule.put("links",links9_rule);
        graphData.put("通信类策略规则1",graph9_rule);

        // 策略规则类图10
        Map<String, Object> graph10_rule = new HashMap<>();
        List<Map> nodes10_rule = new ArrayList<>(7);
        nodes10_rule.add(0,new HashMap(){
            {
                put("name","首选策略");
                put("x",400);
                put("y",100);
            }
        });
        nodes10_rule.add(1,new HashMap(){
            {
                put("name","集群通信能力相似度计算");
                put("x",400);
                put("y",200);
            }
        });
        nodes10_rule.add(2,new HashMap(){
            {
                put("name","按相似度排序的集群列表：\n组合资源集群1：97%\n组合资源集群2：95%\n组合资源集群3：90%\n组合资源集群4：88%\n......");
                put("x",400);
                put("y",300);
            }
        });
        nodes10_rule.add(3,new HashMap(){
            {
                put("name","判定阈值：>=90%");
                put("x",400);
                put("y",400);
            }
        });
        nodes10_rule.add(3,new HashMap(){
            {
                put("name","可选资源池");
                put("x",350);
                put("y",500);
            }
        });
        nodes10_rule.add(4,new HashMap(){
            {
                put("name","扩展策略");
                put("x",450);
                put("y",500);
            }
        });
        nodes10_rule.add(5,new HashMap(){
            {
                put("name","遍历空闲资源池");
                put("x",450);
                put("y",600);
            }
        });
        nodes10_rule.add(6,new HashMap(){
            {
                put("name","策略选择");
                put("x",350);
                put("y",600);
            }
        });
        List<Map<String,Object>> links10_rule = new ArrayList<>(8);
        links10_rule.add(0,new HashMap(){
            {
                put("source","首选策略");
                put("target","集群通信能力相似度计算");
            }
        });
        links10_rule.add(1,new HashMap(){
            {
                put("source","集群通信能力相似度计算");
                put("target","按相似度排序的集群列表：\n组合资源集群1：97%\n组合资源集群2：95%\n组合资源集群3：90%\n组合资源集群4：88%\n......");
            }
        });
        links10_rule.add(2,new HashMap(){
            {
                put("source","按相似度排序的集群列表：\n组合资源集群1：97%\n组合资源集群2：95%\n组合资源集群3：90%\n组合资源集群4：88%\n......");
                put("target","判定阈值：>=90%");
            }
        });
        links10_rule.add(3,new HashMap(){
            {
                put("source","判定阈值：>=90%");
                put("target","可选资源池");
                put("formatter","是");
            }
        });
        links10_rule.add(4,new HashMap(){
            {
                put("source","判定阈值：>=90%");
                put("target","扩展策略");
                put("formatter","否");
            }
        });
        links10_rule.add(5,new HashMap(){
            {
                put("source","可选资源池");
                put("target","策略选择");
            }
        });
        links10_rule.add(6,new HashMap(){
            {
                put("source","扩展策略");
                put("target","遍历空闲资源池");
            }
        });
        links10_rule.add(7,new HashMap(){
            {
                put("source","遍历空闲资源池");
                put("target","可选资源池");
            }
        });
        graph10_rule.put("nodes",nodes10_rule);
        graph10_rule.put("links",links10_rule);
        graphData.put("通信类策略规则2",graph10_rule);

        // 策略规则类图11
        Map<String, Object> graph11_rule = new HashMap<>();
        List<Map> nodes11_rule = new ArrayList<>(7);
        nodes11_rule.add(0,new HashMap(){
            {
                put("name","首选策略");
                put("x",400);
                put("y",100);
            }
        });
        nodes11_rule.add(1,new HashMap(){
            {
                put("name","资源能力参数相似度计算");
                put("x",400);
                put("y",200);
            }
        });
        nodes11_rule.add(2,new HashMap(){
            {
                put("name","按相似度排序的资源列表：\n相似同类资源1：97%\n相似同类资源2：95%\n相似同类资源3：90%\n相似同类资源4：88%\n......");
                put("x",400);
                put("y",300);
            }
        });
        nodes11_rule.add(3,new HashMap(){
            {
                put("name","判定阈值：>=90%");
                put("x",400);
                put("y",400);
            }
        });
        nodes11_rule.add(3,new HashMap(){
            {
                put("name","可选资源池");
                put("x",350);
                put("y",500);
            }
        });
        nodes11_rule.add(4,new HashMap(){
            {
                put("name","扩展策略");
                put("x",450);
                put("y",500);
            }
        });
        nodes11_rule.add(5,new HashMap(){
            {
                put("name","遍历空闲资源池");
                put("x",450);
                put("y",600);
            }
        });
        nodes11_rule.add(6,new HashMap(){
            {
                put("name","策略选择");
                put("x",350);
                put("y",600);
            }
        });
        List<Map<String,Object>> links11_rule = new ArrayList<>(8);
        links11_rule.add(0,new HashMap(){
            {
                put("source","首选策略");
                put("target","资源能力参数相似度计算");
            }
        });
        links11_rule.add(1,new HashMap(){
            {
                put("source","资源能力参数相似度计算");
                put("target","按相似度排序的资源列表：\n相似同类资源1：97%\n相似同类资源2：95%\n相似同类资源3：90%\n相似同类资源4：88%\n......");
            }
        });
        links11_rule.add(2,new HashMap(){
            {
                put("source","按相似度排序的资源列表：\n相似同类资源1：97%\n相似同类资源2：95%\n相似同类资源3：90%\n相似同类资源4：88%\n......");
                put("target","判定阈值：>=90%");
            }
        });
        links11_rule.add(3,new HashMap(){
            {
                put("source","判定阈值：>=90%");
                put("target","可选资源池");
                put("formatter","是");
            }
        });
        links11_rule.add(4,new HashMap(){
            {
                put("source","判定阈值：>=90%");
                put("target","扩展策略");
                put("formatter","否");
            }
        });
        links11_rule.add(5,new HashMap(){
            {
                put("source","可选资源池");
                put("target","策略选择");
            }
        });
        links11_rule.add(6,new HashMap(){
            {
                put("source","扩展策略");
                put("target","遍历空闲资源池");
            }
        });
        links11_rule.add(7,new HashMap(){
            {
                put("source","遍历空闲资源池");
                put("target","可选资源池");
            }
        });
        graph11_rule.put("nodes",nodes11_rule);
        graph11_rule.put("links",links11_rule);
        graphData.put("传感探测类资源优选策略1",graph11_rule);

        // 策略规则类图12
        Map<String, Object> graph12_rule = new HashMap<>();
        List<Map> nodes12_rule = new ArrayList<>(7);
        nodes12_rule.add(0,new HashMap(){
            {
                put("name","首选策略");
                put("x",400);
                put("y",100);
            }
        });
        nodes12_rule.add(1,new HashMap(){
            {
                put("name","集群探测能力相似度计算");
                put("x",400);
                put("y",200);
            }
        });
        nodes12_rule.add(2,new HashMap(){
            {
                put("name","按相似度排序的集群列表：\n组合资源集群1：97%\n组合资源集群2：95%\n组合资源集群3：90%\n组合资源集群4：88%\n......");
                put("x",400);
                put("y",300);
            }
        });
        nodes12_rule.add(3,new HashMap(){
            {
                put("name","判定阈值：>=95%");
                put("x",400);
                put("y",400);
            }
        });
        nodes12_rule.add(3,new HashMap(){
            {
                put("name","可选资源池");
                put("x",350);
                put("y",500);
            }
        });
        nodes12_rule.add(4,new HashMap(){
            {
                put("name","扩展策略");
                put("x",450);
                put("y",500);
            }
        });
        nodes12_rule.add(5,new HashMap(){
            {
                put("name","遍历空闲资源池");
                put("x",450);
                put("y",600);
            }
        });
        nodes12_rule.add(6,new HashMap(){
            {
                put("name","策略选择");
                put("x",350);
                put("y",600);
            }
        });
        List<Map<String,Object>> links12_rule = new ArrayList<>(8);
        links12_rule.add(0,new HashMap(){
            {
                put("source","首选策略");
                put("target","集群探测能力相似度计算");
            }
        });
        links12_rule.add(1,new HashMap(){
            {
                put("source","集群探测能力相似度计算");
                put("target","按相似度排序的集群列表：\n组合资源集群1：97%\n组合资源集群2：95%\n组合资源集群3：90%\n组合资源集群4：88%\n......");
            }
        });
        links12_rule.add(2,new HashMap(){
            {
                put("source","按相似度排序的集群列表：\n组合资源集群1：97%\n组合资源集群2：95%\n组合资源集群3：90%\n组合资源集群4：88%\n......");
                put("target","判定阈值：>=95%");
            }
        });
        links12_rule.add(3,new HashMap(){
            {
                put("source","判定阈值：>=95%");
                put("target","可选资源池");
                put("formatter","是");
            }
        });
        links12_rule.add(4,new HashMap(){
            {
                put("source","判定阈值：>=95%");
                put("target","扩展策略");
                put("formatter","否");
            }
        });
        links12_rule.add(5,new HashMap(){
            {
                put("source","可选资源池");
                put("target","策略选择");
            }
        });
        links12_rule.add(6,new HashMap(){
            {
                put("source","扩展策略");
                put("target","遍历空闲资源池");
            }
        });
        links12_rule.add(7,new HashMap(){
            {
                put("source","遍历空闲资源池");
                put("target","可选资源池");
            }
        });
        graph12_rule.put("nodes",nodes12_rule);
        graph12_rule.put("links",links12_rule);
        graphData.put("传感探测类资源优选策略2",graph12_rule);

        // 策略规则类图13
        Map<String, Object> graph13_rule = new HashMap<>();
        List<Map> nodes13_rule = new ArrayList<>(7);
        nodes13_rule.add(0,new HashMap(){
            {
                put("name","首选策略");
                put("x",400);
                put("y",100);
            }
        });
        nodes13_rule.add(1,new HashMap(){
            {
                put("name","集群探测能力相似度计算\n以更继资源位置为次要考量指标");
                put("x",400);
                put("y",200);
            }
        });
        nodes13_rule.add(2,new HashMap(){
            {
                put("name","按相似度排序的集群列表：\n组合资源集群1：97%\n组合资源集群2：95%\n组合资源集群3：90%\n组合资源集群4：88%\n......");
                put("x",400);
                put("y",300);
            }
        });
        nodes13_rule.add(3,new HashMap(){
            {
                put("name","判定阈值：>=95%");
                put("x",400);
                put("y",400);
            }
        });
        nodes13_rule.add(3,new HashMap(){
            {
                put("name","可选资源池");
                put("x",350);
                put("y",500);
            }
        });
        nodes13_rule.add(4,new HashMap(){
            {
                put("name","扩展策略");
                put("x",450);
                put("y",500);
            }
        });
        nodes13_rule.add(5,new HashMap(){
            {
                put("name","遍历空闲资源池");
                put("x",450);
                put("y",600);
            }
        });
        nodes13_rule.add(6,new HashMap(){
            {
                put("name","策略选择");
                put("x",350);
                put("y",600);
            }
        });
        List<Map<String,Object>> links13_rule = new ArrayList<>(8);
        links13_rule.add(0,new HashMap(){
            {
                put("source","首选策略");
                put("target","集群探测能力相似度计算\n以更继资源位置为次要考量指标");
            }
        });
        links13_rule.add(1,new HashMap(){
            {
                put("source","集群探测能力相似度计算\n以更继资源位置为次要考量指标");
                put("target","按相似度排序的集群列表：\n组合资源集群1：97%\n组合资源集群2：95%\n组合资源集群3：90%\n组合资源集群4：88%\n......");
            }
        });
        links13_rule.add(2,new HashMap(){
            {
                put("source","按相似度排序的集群列表：\n组合资源集群1：97%\n组合资源集群2：95%\n组合资源集群3：90%\n组合资源集群4：88%\n......");
                put("target","判定阈值：>=95%");
            }
        });
        links13_rule.add(3,new HashMap(){
            {
                put("source","判定阈值：>=95%");
                put("target","可选资源池");
                put("formatter","是");
            }
        });
        links13_rule.add(4,new HashMap(){
            {
                put("source","判定阈值：>=95%");
                put("target","扩展策略");
                put("formatter","否");
            }
        });
        links13_rule.add(5,new HashMap(){
            {
                put("source","可选资源池");
                put("target","策略选择");
            }
        });
        links13_rule.add(6,new HashMap(){
            {
                put("source","扩展策略");
                put("target","遍历空闲资源池");
            }
        });
        links13_rule.add(7,new HashMap(){
            {
                put("source","遍历空闲资源池");
                put("target","可选资源池");
            }
        });
        graph13_rule.put("nodes",nodes13_rule);
        graph13_rule.put("links",links13_rule);
        graphData.put("传感探测类资源优选策略3",graph13_rule);

        // 策略规则类图14
        Map<String, Object> graph14_rule = new HashMap<>();
        List<Map> nodes14_rule = new ArrayList<>(7);
        nodes14_rule.add(0,new HashMap(){
            {
                put("name","首选策略");
                put("x",400);
                put("y",100);
            }
        });
        nodes14_rule.add(1,new HashMap(){
            {
                put("name","资源能力参数相似度计算");
                put("x",400);
                put("y",200);
            }
        });
        nodes14_rule.add(2,new HashMap(){
            {
                put("name","按相似度排序的资源列表：\n相似同类资源1：97%\n相似同类资源2：95%\n相似同类资源3：90%\n相似同类资源4：88%\n......");
                put("x",400);
                put("y",300);
            }
        });
        nodes14_rule.add(3,new HashMap(){
            {
                put("name","判定阈值：>=85%");
                put("x",400);
                put("y",400);
            }
        });
        nodes14_rule.add(3,new HashMap(){
            {
                put("name","可选资源池");
                put("x",350);
                put("y",500);
            }
        });
        nodes14_rule.add(4,new HashMap(){
            {
                put("name","扩展策略");
                put("x",450);
                put("y",500);
            }
        });
        nodes14_rule.add(5,new HashMap(){
            {
                put("name","遍历空闲资源池");
                put("x",450);
                put("y",600);
            }
        });
        nodes14_rule.add(6,new HashMap(){
            {
                put("name","策略选择");
                put("x",350);
                put("y",600);
            }
        });
        List<Map<String,Object>> links14_rule = new ArrayList<>(8);
        links14_rule.add(0,new HashMap(){
            {
                put("source","首选策略");
                put("target","资源能力参数相似度计算");
            }
        });
        links14_rule.add(1,new HashMap(){
            {
                put("source","资源能力参数相似度计算");
                put("target","按相似度排序的资源列表：\n相似同类资源1：97%\n相似同类资源2：95%\n相似同类资源3：90%\n相似同类资源4：88%\n......");
            }
        });
        links14_rule.add(2,new HashMap(){
            {
                put("source","按相似度排序的资源列表：\n相似同类资源1：97%\n相似同类资源2：95%\n相似同类资源3：90%\n相似同类资源4：88%\n......");
                put("target","判定阈值：>=85%");
            }
        });
        links14_rule.add(3,new HashMap(){
            {
                put("source","判定阈值：>=85%");
                put("target","可选资源池");
                put("formatter","是");
            }
        });
        links14_rule.add(4,new HashMap(){
            {
                put("source","判定阈值：>=85%");
                put("target","扩展策略");
                put("formatter","否");
            }
        });
        links14_rule.add(5,new HashMap(){
            {
                put("source","可选资源池");
                put("target","策略选择");
            }
        });
        links14_rule.add(6,new HashMap(){
            {
                put("source","扩展策略");
                put("target","遍历空闲资源池");
            }
        });
        links14_rule.add(7,new HashMap(){
            {
                put("source","遍历空闲资源池");
                put("target","可选资源池");
            }
        });
        graph14_rule.put("nodes",nodes14_rule);
        graph14_rule.put("links",links14_rule);
        graphData.put("情报处理类资源优选策略1",graph14_rule);

        // 策略规则类图15
        Map<String, Object> graph15_rule = new HashMap<>();
        List<Map> nodes15_rule = new ArrayList<>(7);
        nodes15_rule.add(0,new HashMap(){
            {
                put("name","首选策略");
                put("x",400);
                put("y",100);
            }
        });
        nodes15_rule.add(1,new HashMap(){
            {
                put("name","集群处理能力相似度计算");
                put("x",400);
                put("y",200);
            }
        });
        nodes15_rule.add(2,new HashMap(){
            {
                put("name","按相似度排序的集群列表：\n组合资源集群1：97%\n组合资源集群2：95%\n组合资源集群3：90%\n组合资源集群4：88%\n......");
                put("x",400);
                put("y",300);
            }
        });
        nodes15_rule.add(3,new HashMap(){
            {
                put("name","判定阈值：>=90%");
                put("x",400);
                put("y",400);
            }
        });
        nodes15_rule.add(3,new HashMap(){
            {
                put("name","可选资源池");
                put("x",350);
                put("y",500);
            }
        });
        nodes15_rule.add(4,new HashMap(){
            {
                put("name","扩展策略");
                put("x",450);
                put("y",500);
            }
        });
        nodes15_rule.add(5,new HashMap(){
            {
                put("name","遍历空闲资源池");
                put("x",450);
                put("y",600);
            }
        });
        nodes15_rule.add(6,new HashMap(){
            {
                put("name","策略选择");
                put("x",350);
                put("y",600);
            }
        });
        List<Map<String,Object>> links15_rule = new ArrayList<>(8);
        links15_rule.add(0,new HashMap(){
            {
                put("source","首选策略");
                put("target","集群处理能力相似度计算");
            }
        });
        links15_rule.add(1,new HashMap(){
            {
                put("source","集群处理能力相似度计算");
                put("target","按相似度排序的集群列表：\n组合资源集群1：97%\n组合资源集群2：95%\n组合资源集群3：90%\n组合资源集群4：88%\n......");
            }
        });
        links15_rule.add(2,new HashMap(){
            {
                put("source","按相似度排序的集群列表：\n组合资源集群1：97%\n组合资源集群2：95%\n组合资源集群3：90%\n组合资源集群4：88%\n......");
                put("target","判定阈值：>=90%");
            }
        });
        links15_rule.add(3,new HashMap(){
            {
                put("source","判定阈值：>=90%");
                put("target","可选资源池");
                put("formatter","是");
            }
        });
        links15_rule.add(4,new HashMap(){
            {
                put("source","判定阈值：>=90%");
                put("target","扩展策略");
                put("formatter","否");
            }
        });
        links15_rule.add(5,new HashMap(){
            {
                put("source","可选资源池");
                put("target","策略选择");
            }
        });
        links15_rule.add(6,new HashMap(){
            {
                put("source","扩展策略");
                put("target","遍历空闲资源池");
            }
        });
        links15_rule.add(7,new HashMap(){
            {
                put("source","遍历空闲资源池");
                put("target","可选资源池");
            }
        });
        graph15_rule.put("nodes",nodes15_rule);
        graph15_rule.put("links",links15_rule);
        graphData.put("情报处理类资源优选策略2",graph15_rule);

        // 策略规则类图16
        Map<String, Object> graph16_rule = new HashMap<>();
        List<Map> nodes16_rule = new ArrayList<>(7);
        nodes16_rule.add(0,new HashMap(){
            {
                put("name","首选策略");
                put("x",400);
                put("y",100);
            }
        });
        nodes16_rule.add(1,new HashMap(){
            {
                put("name","集群处理能力相似度计算\n以更继资源的物理位置为次要考量指标");
                put("x",400);
                put("y",200);
            }
        });
        nodes16_rule.add(2,new HashMap(){
            {
                put("name","按相似度排序的集群列表：\n组合资源集群1：97%\n组合资源集群2：95%\n组合资源集群3：90%\n组合资源集群4：88%\n......");
                put("x",400);
                put("y",300);
            }
        });
        nodes16_rule.add(3,new HashMap(){
            {
                put("name","判定阈值：>=90%");
                put("x",400);
                put("y",400);
            }
        });
        nodes16_rule.add(3,new HashMap(){
            {
                put("name","可选资源池");
                put("x",350);
                put("y",500);
            }
        });
        nodes16_rule.add(4,new HashMap(){
            {
                put("name","扩展策略");
                put("x",450);
                put("y",500);
            }
        });
        nodes16_rule.add(5,new HashMap(){
            {
                put("name","遍历空闲资源池");
                put("x",450);
                put("y",600);
            }
        });
        nodes16_rule.add(6,new HashMap(){
            {
                put("name","策略选择");
                put("x",350);
                put("y",600);
            }
        });
        List<Map<String,Object>> links16_rule = new ArrayList<>(8);
        links16_rule.add(0,new HashMap(){
            {
                put("source","首选策略");
                put("target","集群处理能力相似度计算\n以更继资源的物理位置为次要考量指标");
            }
        });
        links16_rule.add(1,new HashMap(){
            {
                put("source","集群处理能力相似度计算\n以更继资源的物理位置为次要考量指标");
                put("target","按相似度排序的集群列表：\n组合资源集群1：97%\n组合资源集群2：95%\n组合资源集群3：90%\n组合资源集群4：88%\n......");
            }
        });
        links16_rule.add(2,new HashMap(){
            {
                put("source","按相似度排序的集群列表：\n组合资源集群1：97%\n组合资源集群2：95%\n组合资源集群3：90%\n组合资源集群4：88%\n......");
                put("target","判定阈值：>=90%");
            }
        });
        links16_rule.add(3,new HashMap(){
            {
                put("source","判定阈值：>=90%");
                put("target","可选资源池");
                put("formatter","是");
            }
        });
        links16_rule.add(4,new HashMap(){
            {
                put("source","判定阈值：>=90%");
                put("target","扩展策略");
                put("formatter","否");
            }
        });
        links16_rule.add(5,new HashMap(){
            {
                put("source","可选资源池");
                put("target","策略选择");
            }
        });
        links16_rule.add(6,new HashMap(){
            {
                put("source","扩展策略");
                put("target","遍历空闲资源池");
            }
        });
        links16_rule.add(7,new HashMap(){
            {
                put("source","遍历空闲资源池");
                put("target","可选资源池");
            }
        });
        graph16_rule.put("nodes",nodes16_rule);
        graph16_rule.put("links",links16_rule);
        graphData.put("情报处理类资源优选策略3",graph16_rule);

        // 策略规则类图17
        Map<String, Object> graph17_rule = new HashMap<>();
        List<Map> nodes17_rule = new ArrayList<>(7);
        nodes17_rule.add(0,new HashMap(){
            {
                put("name","首选策略");
                put("x",400);
                put("y",100);
            }
        });
        nodes17_rule.add(1,new HashMap(){
            {
                put("name","资源能力参数相似度计算");
                put("x",400);
                put("y",200);
            }
        });
        nodes17_rule.add(2,new HashMap(){
            {
                put("name","按相似度排序的资源列表：\n相似同类资源1：97%\n相似同类资源2：95%\n相似同类资源3：90%\n相似同类资源4：88%\n......");
                put("x",400);
                put("y",300);
            }
        });
        nodes17_rule.add(3,new HashMap(){
            {
                put("name","判定阈值：>=90%");
                put("x",400);
                put("y",400);
            }
        });
        nodes17_rule.add(3,new HashMap(){
            {
                put("name","可选资源池");
                put("x",350);
                put("y",500);
            }
        });
        nodes17_rule.add(4,new HashMap(){
            {
                put("name","扩展策略");
                put("x",450);
                put("y",500);
            }
        });
        nodes17_rule.add(5,new HashMap(){
            {
                put("name","遍历空闲资源池");
                put("x",450);
                put("y",600);
            }
        });
        nodes17_rule.add(6,new HashMap(){
            {
                put("name","策略选择");
                put("x",350);
                put("y",600);
            }
        });
        List<Map<String,Object>> links17_rule = new ArrayList<>(8);
        links17_rule.add(0,new HashMap(){
            {
                put("source","首选策略");
                put("target","资源能力参数相似度计算");
            }
        });
        links17_rule.add(1,new HashMap(){
            {
                put("source","资源能力参数相似度计算");
                put("target","按相似度排序的资源列表：\n相似同类资源1：97%\n相似同类资源2：95%\n相似同类资源3：90%\n相似同类资源4：88%\n......");
            }
        });
        links17_rule.add(2,new HashMap(){
            {
                put("source","按相似度排序的资源列表：\n相似同类资源1：97%\n相似同类资源2：95%\n相似同类资源3：90%\n相似同类资源4：88%\n......");
                put("target","判定阈值：>=90%");
            }
        });
        links17_rule.add(3,new HashMap(){
            {
                put("source","判定阈值：>=90%");
                put("target","可选资源池");
                put("formatter","是");
            }
        });
        links17_rule.add(4,new HashMap(){
            {
                put("source","判定阈值：>=90%");
                put("target","扩展策略");
                put("formatter","否");
            }
        });
        links17_rule.add(5,new HashMap(){
            {
                put("source","可选资源池");
                put("target","策略选择");
            }
        });
        links17_rule.add(6,new HashMap(){
            {
                put("source","扩展策略");
                put("target","遍历空闲资源池");
            }
        });
        links17_rule.add(7,new HashMap(){
            {
                put("source","遍历空闲资源池");
                put("target","可选资源池");
            }
        });
        graph17_rule.put("nodes",nodes17_rule);
        graph17_rule.put("links",links17_rule);
        graphData.put("决策控制类资源优选策略1",graph1_rule);

        // 策略规则类图18
        Map<String, Object> graph18_rule = new HashMap<>();
        List<Map> nodes18_rule = new ArrayList<>(7);
        nodes18_rule.add(0,new HashMap(){
            {
                put("name","首选策略");
                put("x",400);
                put("y",100);
            }
        });
        nodes18_rule.add(1,new HashMap(){
            {
                put("name","集群决策能力相似度计算");
                put("x",400);
                put("y",200);
            }
        });
        nodes18_rule.add(2,new HashMap(){
            {
                put("name","按相似度排序的集群列表：\n组合资源集群1：97%\n组合资源集群2：95%\n组合资源集群3：90%\n组合资源集群4：88%\n......");
                put("x",400);
                put("y",300);
            }
        });
        nodes18_rule.add(3,new HashMap(){
            {
                put("name","判定阈值：>=95%");
                put("x",400);
                put("y",400);
            }
        });
        nodes18_rule.add(3,new HashMap(){
            {
                put("name","可选资源池");
                put("x",350);
                put("y",500);
            }
        });
        nodes18_rule.add(4,new HashMap(){
            {
                put("name","扩展策略");
                put("x",450);
                put("y",500);
            }
        });
        nodes18_rule.add(5,new HashMap(){
            {
                put("name","遍历空闲资源池");
                put("x",450);
                put("y",600);
            }
        });
        nodes18_rule.add(6,new HashMap(){
            {
                put("name","策略选择");
                put("x",350);
                put("y",600);
            }
        });
        List<Map<String,Object>> links18_rule = new ArrayList<>(8);
        links18_rule.add(0,new HashMap(){
            {
                put("source","首选策略");
                put("target","集群决策能力相似度计算");
            }
        });
        links18_rule.add(1,new HashMap(){
            {
                put("source","集群决策能力相似度计算");
                put("target","按相似度排序的集群列表：\n组合资源集群1：97%\n组合资源集群2：95%\n组合资源集群3：90%\n组合资源集群4：88%\n......");
            }
        });
        links18_rule.add(2,new HashMap(){
            {
                put("source","按相似度排序的集群列表：\n组合资源集群1：97%\n组合资源集群2：95%\n组合资源集群3：90%\n组合资源集群4：88%\n......");
                put("target","判定阈值：>=95%");
            }
        });
        links18_rule.add(3,new HashMap(){
            {
                put("source","判定阈值：>=95%");
                put("target","可选资源池");
                put("formatter","是");
            }
        });
        links18_rule.add(4,new HashMap(){
            {
                put("source","判定阈值：>=95%");
                put("target","扩展策略");
                put("formatter","否");
            }
        });
        links18_rule.add(5,new HashMap(){
            {
                put("source","可选资源池");
                put("target","策略选择");
            }
        });
        links18_rule.add(6,new HashMap(){
            {
                put("source","扩展策略");
                put("target","遍历空闲资源池");
            }
        });
        links18_rule.add(7,new HashMap(){
            {
                put("source","遍历空闲资源池");
                put("target","可选资源池");
            }
        });
        graph18_rule.put("nodes",nodes18_rule);
        graph18_rule.put("links",links18_rule);
        graphData.put("决策控制类资源优选策略2",graph18_rule);

        // 策略规则类图19
        Map<String, Object> graph19_rule = new HashMap<>();
        List<Map> nodes19_rule = new ArrayList<>(7);
        nodes19_rule.add(0,new HashMap(){
            {
                put("name","首选策略");
                put("x",400);
                put("y",100);
            }
        });
        nodes19_rule.add(1,new HashMap(){
            {
                put("name","集群决策能力相似度计算\n以更继资源物理位置为次要考量指标");
                put("x",400);
                put("y",200);
            }
        });
        nodes19_rule.add(2,new HashMap(){
            {
                put("name","按相似度排序的集群列表：\n组合资源集群1：97%\n组合资源集群2：95%\n组合资源集群3：90%\n组合资源集群4：88%\n......");
                put("x",400);
                put("y",300);
            }
        });
        nodes19_rule.add(3,new HashMap(){
            {
                put("name","判定阈值：>=95%");
                put("x",400);
                put("y",400);
            }
        });
        nodes19_rule.add(3,new HashMap(){
            {
                put("name","可选资源池");
                put("x",350);
                put("y",500);
            }
        });
        nodes19_rule.add(4,new HashMap(){
            {
                put("name","扩展策略");
                put("x",450);
                put("y",500);
            }
        });
        nodes19_rule.add(5,new HashMap(){
            {
                put("name","遍历空闲资源池");
                put("x",450);
                put("y",600);
            }
        });
        nodes19_rule.add(6,new HashMap(){
            {
                put("name","策略选择");
                put("x",350);
                put("y",600);
            }
        });
        List<Map<String,Object>> links19_rule = new ArrayList<>(8);
        links19_rule.add(0,new HashMap(){
            {
                put("source","首选策略");
                put("target","集群决策能力相似度计算\n以更继资源物理位置为次要考量指标");
            }
        });
        links19_rule.add(1,new HashMap(){
            {
                put("source","集群决策能力相似度计算\n以更继资源物理位置为次要考量指标");
                put("target","按相似度排序的集群列表：\n组合资源集群1：97%\n组合资源集群2：95%\n组合资源集群3：90%\n组合资源集群4：88%\n......");
            }
        });
        links19_rule.add(2,new HashMap(){
            {
                put("source","按相似度排序的集群列表：\n组合资源集群1：97%\n组合资源集群2：95%\n组合资源集群3：90%\n组合资源集群4：88%\n......");
                put("target","判定阈值：>=95%");
            }
        });
        links19_rule.add(3,new HashMap(){
            {
                put("source","判定阈值：>=95%");
                put("target","可选资源池");
                put("formatter","是");
            }
        });
        links19_rule.add(4,new HashMap(){
            {
                put("source","判定阈值：>=95%");
                put("target","扩展策略");
                put("formatter","否");
            }
        });
        links19_rule.add(5,new HashMap(){
            {
                put("source","可选资源池");
                put("target","策略选择");
            }
        });
        links19_rule.add(6,new HashMap(){
            {
                put("source","扩展策略");
                put("target","遍历空闲资源池");
            }
        });
        links19_rule.add(7,new HashMap(){
            {
                put("source","遍历空闲资源池");
                put("target","可选资源池");
            }
        });
        graph19_rule.put("nodes",nodes19_rule);
        graph19_rule.put("links",links19_rule);
        graphData.put("决策控制类资源优选策略3",graph19_rule);

        // 策略规则类图20
        Map<String, Object> graph20_rule = new HashMap<>();
        List<Map> nodes20_rule = new ArrayList<>(7);
        nodes20_rule.add(0,new HashMap(){
            {
                put("name","首选策略");
                put("x",400);
                put("y",100);
            }
        });
        nodes20_rule.add(1,new HashMap(){
            {
                put("name","资源能力参数相似度计算");
                put("x",400);
                put("y",200);
            }
        });
        nodes20_rule.add(2,new HashMap(){
            {
                put("name","按相似度排序的资源列表：\n相似同类资源1：97%\n相似同类资源2：95%\n相似同类资源3：90%\n相似同类资源4：88%\n......");
                put("x",400);
                put("y",300);
            }
        });
        nodes20_rule.add(3,new HashMap(){
            {
                put("name","判定阈值：>=85%");
                put("x",400);
                put("y",400);
            }
        });
        nodes20_rule.add(3,new HashMap(){
            {
                put("name","可选资源池");
                put("x",350);
                put("y",500);
            }
        });
        nodes20_rule.add(4,new HashMap(){
            {
                put("name","扩展策略");
                put("x",450);
                put("y",500);
            }
        });
        nodes20_rule.add(5,new HashMap(){
            {
                put("name","遍历空闲资源池");
                put("x",450);
                put("y",600);
            }
        });
        nodes20_rule.add(6,new HashMap(){
            {
                put("name","策略选择");
                put("x",350);
                put("y",600);
            }
        });
        List<Map<String,Object>> links20_rule = new ArrayList<>(8);
        links20_rule.add(0,new HashMap(){
            {
                put("source","首选策略");
                put("target","资源能力参数相似度计算");
            }
        });
        links20_rule.add(1,new HashMap(){
            {
                put("source","资源能力参数相似度计算");
                put("target","按相似度排序的资源列表：\n相似同类资源1：97%\n相似同类资源2：95%\n相似同类资源3：90%\n相似同类资源4：88%\n......");
            }
        });
        links20_rule.add(2,new HashMap(){
            {
                put("source","按相似度排序的资源列表：\n相似同类资源1：97%\n相似同类资源2：95%\n相似同类资源3：90%\n相似同类资源4：88%\n......");
                put("target","判定阈值：>=85%");
            }
        });
        links20_rule.add(3,new HashMap(){
            {
                put("source","判定阈值：>=85%");
                put("target","可选资源池");
                put("formatter","是");
            }
        });
        links20_rule.add(4,new HashMap(){
            {
                put("source","判定阈值：>=85%");
                put("target","扩展策略");
                put("formatter","否");
            }
        });
        links20_rule.add(5,new HashMap(){
            {
                put("source","可选资源池");
                put("target","策略选择");
            }
        });
        links20_rule.add(6,new HashMap(){
            {
                put("source","扩展策略");
                put("target","遍历空闲资源池");
            }
        });
        links20_rule.add(7,new HashMap(){
            {
                put("source","遍历空闲资源池");
                put("target","可选资源池");
            }
        });
        graph20_rule.put("nodes",nodes20_rule);
        graph20_rule.put("links",links20_rule);
        graphData.put("火力打击类资源优选策略1",graph20_rule);

        // 策略规则类图21
        Map<String, Object> graph21_rule = new HashMap<>();
        List<Map> nodes21_rule = new ArrayList<>(7);
        nodes21_rule.add(0,new HashMap(){
            {
                put("name","首选策略");
                put("x",400);
                put("y",100);
            }
        });
        nodes21_rule.add(1,new HashMap(){
            {
                put("name","集群打击能力相似度计算");
                put("x",400);
                put("y",200);
            }
        });
        nodes21_rule.add(2,new HashMap(){
            {
                put("name","按相似度排序的集群列表：\n组合资源集群1：97%\n组合资源集群2：95%\n组合资源集群3：90%\n组合资源集群4：88%\n......");
                put("x",400);
                put("y",300);
            }
        });
        nodes21_rule.add(3,new HashMap(){
            {
                put("name","判定阈值：>=90%");
                put("x",400);
                put("y",400);
            }
        });
        nodes21_rule.add(3,new HashMap(){
            {
                put("name","可选资源池");
                put("x",350);
                put("y",500);
            }
        });
        nodes21_rule.add(4,new HashMap(){
            {
                put("name","扩展策略");
                put("x",450);
                put("y",500);
            }
        });
        nodes21_rule.add(5,new HashMap(){
            {
                put("name","遍历空闲资源池");
                put("x",450);
                put("y",600);
            }
        });
        nodes21_rule.add(6,new HashMap(){
            {
                put("name","策略选择");
                put("x",350);
                put("y",600);
            }
        });
        List<Map<String,Object>> links21_rule = new ArrayList<>(8);
        links21_rule.add(0,new HashMap(){
            {
                put("source","首选策略");
                put("target","集群打击能力相似度计算");
            }
        });
        links21_rule.add(1,new HashMap(){
            {
                put("source","集群打击能力相似度计算");
                put("target","按相似度排序的集群列表：\n组合资源集群1：97%\n组合资源集群2：95%\n组合资源集群3：90%\n组合资源集群4：88%\n......");
            }
        });
        links21_rule.add(2,new HashMap(){
            {
                put("source","按相似度排序的集群列表：\n组合资源集群1：97%\n组合资源集群2：95%\n组合资源集群3：90%\n组合资源集群4：88%\n......");
                put("target","判定阈值：>=90%");
            }
        });
        links21_rule.add(3,new HashMap(){
            {
                put("source","判定阈值：>=90%");
                put("target","可选资源池");
                put("formatter","是");
            }
        });
        links21_rule.add(4,new HashMap(){
            {
                put("source","判定阈值：>=90%");
                put("target","扩展策略");
                put("formatter","否");
            }
        });
        links21_rule.add(5,new HashMap(){
            {
                put("source","可选资源池");
                put("target","策略选择");
            }
        });
        links21_rule.add(6,new HashMap(){
            {
                put("source","扩展策略");
                put("target","遍历空闲资源池");
            }
        });
        links21_rule.add(7,new HashMap(){
            {
                put("source","遍历空闲资源池");
                put("target","可选资源池");
            }
        });
        graph21_rule.put("nodes",nodes21_rule);
        graph21_rule.put("links",links21_rule);
        graphData.put("火力打击类资源优选策略2",graph21_rule);

        // 策略规则类图22
        Map<String, Object> graph22_rule = new HashMap<>();
        List<Map> nodes22_rule = new ArrayList<>(7);
        nodes22_rule.add(0,new HashMap(){
            {
                put("name","首选策略");
                put("x",400);
                put("y",100);
            }
        });
        nodes22_rule.add(1,new HashMap(){
            {
                put("name","集群打击能力相似度计算\n以更继资源物理位置为次要考量指标");
                put("x",400);
                put("y",200);
            }
        });
        nodes22_rule.add(2,new HashMap(){
            {
                put("name","按相似度排序的集群列表：\n组合资源集群1：97%\n组合资源集群2：95%\n组合资源集群3：90%\n组合资源集群4：88%\n......");
                put("x",400);
                put("y",300);
            }
        });
        nodes22_rule.add(3,new HashMap(){
            {
                put("name","判定阈值：>=90%");
                put("x",400);
                put("y",400);
            }
        });
        nodes22_rule.add(3,new HashMap(){
            {
                put("name","可选资源池");
                put("x",350);
                put("y",500);
            }
        });
        nodes22_rule.add(4,new HashMap(){
            {
                put("name","扩展策略");
                put("x",450);
                put("y",500);
            }
        });
        nodes22_rule.add(5,new HashMap(){
            {
                put("name","遍历空闲资源池");
                put("x",450);
                put("y",600);
            }
        });
        nodes22_rule.add(6,new HashMap(){
            {
                put("name","策略选择");
                put("x",350);
                put("y",600);
            }
        });
        List<Map<String,Object>> links22_rule = new ArrayList<>(8);
        links22_rule.add(0,new HashMap(){
            {
                put("source","首选策略");
                put("target","集群打击能力相似度计算\n以更继资源物理位置为次要考量指标");
            }
        });
        links22_rule.add(1,new HashMap(){
            {
                put("source","集群打击能力相似度计算\n以更继资源物理位置为次要考量指标");
                put("target","按相似度排序的集群列表：\n组合资源集群1：97%\n组合资源集群2：95%\n组合资源集群3：90%\n组合资源集群4：88%\n......");
            }
        });
        links22_rule.add(2,new HashMap(){
            {
                put("source","按相似度排序的集群列表：\n组合资源集群1：97%\n组合资源集群2：95%\n组合资源集群3：90%\n组合资源集群4：88%\n......");
                put("target","判定阈值：>=90%");
            }
        });
        links22_rule.add(3,new HashMap(){
            {
                put("source","判定阈值：>=90%");
                put("target","可选资源池");
                put("formatter","是");
            }
        });
        links22_rule.add(4,new HashMap(){
            {
                put("source","判定阈值：>=90%");
                put("target","扩展策略");
                put("formatter","否");
            }
        });
        links22_rule.add(5,new HashMap(){
            {
                put("source","可选资源池");
                put("target","策略选择");
            }
        });
        links22_rule.add(6,new HashMap(){
            {
                put("source","扩展策略");
                put("target","遍历空闲资源池");
            }
        });
        links22_rule.add(7,new HashMap(){
            {
                put("source","遍历空闲资源池");
                put("target","可选资源池");
            }
        });
        graph22_rule.put("nodes",nodes22_rule);
        graph22_rule.put("links",links22_rule);
        graphData.put("火力打击类资源优选策略3",graph22_rule);

        //算法模型类图1
        Map<String, Object> graph1_algorithm = new HashMap<>();
        List<Map> nodes1_algorithm = new ArrayList<>(7);
        nodes1_algorithm.add(0,new HashMap(){
            {
                put("name","侦察无人机失效");
                put("x",400);
                put("y",100);
            }
        });
        nodes1_algorithm.add(1,new HashMap(){
            {
                put("name","获取探测能力C1");
                put("x",400);
                put("y",200);
            }
        });
        nodes1_algorithm.add(2,new HashMap(){
            {
                put("name","20%<=C1<=50%");
                put("x",400);
                put("y",300);
            }
        });
        nodes1_algorithm.add(3,new HashMap(){
            {
                put("name","NSGA-II算法");
                put("x",400);
                put("y",400);
            }
        });
        nodes1_algorithm.add(4,new HashMap(){
            {
                put("name","迭代次数大于100");
                put("x",400);
                put("y",500);
            }
        });
        nodes1_algorithm.add(5,new HashMap(){
            {
                put("name","结束");
                put("x",400);
                put("y",600);
            }
        });
        List<Map<String,Object>> links1_algorithm = new ArrayList<>(8);
        links1_algorithm.add(0,new HashMap(){
            {
                put("source","侦察无人机失效");
                put("target","获取探测能力C1");
            }
        });
        links1_algorithm.add(1,new HashMap(){
            {
                put("source","获取探测能力C1");
                put("target","20%<=C1<=50%");
            }
        });
        links1_algorithm.add(2,new HashMap(){
            {
                put("source","20%<=C1<=50%");
                put("target","NSGA-II算法");
                put("formatter","是");
            }
        });
        links1_algorithm.add(3,new HashMap(){
            {
                put("source","20%<=C1<=50%");
                put("target","结束");
                put("formatter","否");
                put("curveness",1);
            }
        });
        links1_algorithm.add(4,new HashMap(){
            {
                put("source","NSGA-II算法");
                put("target","迭代次数大于100");
            }
        });
        /*links1_algorithm.add(5,new HashMap(){
            {
                put("source","迭代次数大于100");
                put("target","NSGA-II算法");
                put("formatter","否");
            }
        });*/
        links1_algorithm.add(5,new HashMap(){
            {
                put("source","迭代次数大于100");
                put("target","结束");
                put("formatter","是");
            }
        });
        graph1_algorithm.put("nodes",nodes1_algorithm);
        graph1_algorithm.put("links",links1_algorithm);
        graphData.put("传感探测类资源失效算法模型1",graph1_algorithm);

        //算法模型类图2
        Map<String, Object> graph2_algorithm = new HashMap<>();
        List<Map> nodes2_algorithm = new ArrayList<>(7);
        nodes2_algorithm.add(0,new HashMap(){
            {
                put("name","侦察无人机失效");
                put("x",400);
                put("y",100);
            }
        });
        nodes2_algorithm.add(1,new HashMap(){
            {
                put("name","获取探测能力C1");
                put("x",400);
                put("y",200);
            }
        });
        nodes2_algorithm.add(2,new HashMap(){
            {
                put("name","C1<20%");
                put("x",400);
                put("y",300);
            }
        });
        nodes2_algorithm.add(3,new HashMap(){
            {
                put("name","DLS算法(动态列表调度)");
                put("x",400);
                put("y",400);
            }
        });
        nodes2_algorithm.add(4,new HashMap(){
            {
                put("name","能力满足阈值：70%");
                put("x",400);
                put("y",500);
            }
        });
        nodes2_algorithm.add(5,new HashMap(){
            {
                put("name","结束");
                put("x",400);
                put("y",600);
            }
        });
        List<Map<String,Object>> links2_algorithm = new ArrayList<>(8);
        links2_algorithm.add(0,new HashMap(){
            {
                put("source","侦察无人机失效");
                put("target","获取探测能力C1");
            }
        });
        links2_algorithm.add(1,new HashMap(){
            {
                put("source","获取探测能力C1");
                put("target","C1<20%");
            }
        });
        links2_algorithm.add(2,new HashMap(){
            {
                put("source","C1<20%");
                put("target","DLS算法(动态列表调度)");
                put("formatter","是");
            }
        });
        links2_algorithm.add(3,new HashMap(){
            {
                put("source","C1<20%");
                put("target","结束");
                put("formatter","否");
                put("curveness",1);
            }
        });
        links2_algorithm.add(4,new HashMap(){
            {
                put("source","DLS算法(动态列表调度)");
                put("target","能力满足阈值：70%");
            }
        });
        /*links2_algorithm.add(5,new HashMap(){
            {
                put("source","能力满足阈值：70%");
                put("target","DLS算法(动态列表调度)");
                put("formatter","否");
            }
        });*/
        links2_algorithm.add(5,new HashMap(){
            {
                put("source","能力满足阈值：70%");
                put("target","结束");
                put("formatter","是");
            }
        });
        graph2_algorithm.put("nodes",nodes2_algorithm);
        graph2_algorithm.put("links",links2_algorithm);
        graphData.put("传感探测类资源失效算法模型2",graph2_algorithm);

        //算法模型类图3
        Map<String, Object> graph3_algorithm = new HashMap<>();
        List<Map> nodes3_algorithm = new ArrayList<>(7);
        nodes3_algorithm.add(0,new HashMap(){
            {
                put("name","情报处理中心失效");
                put("x",400);
                put("y",100);
            }
        });
        nodes3_algorithm.add(1,new HashMap(){
            {
                put("name","获取处理能力C1");
                put("x",400);
                put("y",200);
            }
        });
        nodes3_algorithm.add(2,new HashMap(){
            {
                put("name","20%<=C1<=50%");
                put("x",400);
                put("y",300);
            }
        });
        nodes3_algorithm.add(3,new HashMap(){
            {
                put("name","n-Best+rollout算法");
                put("x",400);
                put("y",400);
            }
        });
        nodes3_algorithm.add(4,new HashMap(){
            {
                put("name","r = 2");
                put("x",400);
                put("y",500);
            }
        });
        nodes3_algorithm.add(5,new HashMap(){
            {
                put("name","结束");
                put("x",400);
                put("y",600);
            }
        });
        List<Map<String,Object>> links3_algorithm = new ArrayList<>(8);
        links3_algorithm.add(0,new HashMap(){
            {
                put("source","情报处理中心失效");
                put("target","获取处理能力C1");
            }
        });
        links3_algorithm.add(1,new HashMap(){
            {
                put("source","获取处理能力C1");
                put("target","20%<=C1<=50%");
            }
        });
        links3_algorithm.add(2,new HashMap(){
            {
                put("source","20%<=C1<=50%");
                put("target","n-Best+rollout算法");
                put("formatter","是");
            }
        });
        links3_algorithm.add(3,new HashMap(){
            {
                put("source","20%<=C1<=50%");
                put("target","结束");
                put("formatter","否");
                put("curveness",1);
            }
        });
        links3_algorithm.add(4,new HashMap(){
            {
                put("source","n-Best+rollout算法");
                put("target","r = 2");
            }
        });
        /*links3_algorithm.add(5,new HashMap(){
            {
                put("source","r = 2");
                put("target","n-Best+rollout算法");
                put("formatter","否");
            }
        });*/
        links3_algorithm.add(5,new HashMap(){
            {
                put("source","r = 2");
                put("target","结束");
                put("formatter","是");
            }
        });
        graph3_algorithm.put("nodes",nodes3_algorithm);
        graph3_algorithm.put("links",links3_algorithm);
        graphData.put("情报处理类资源失效算法模型1",graph3_algorithm);

        //算法模型类图4
        Map<String, Object> graph4_algorithm = new HashMap<>();
        List<Map> nodes4_algorithm = new ArrayList<>(7);
        nodes4_algorithm.add(0,new HashMap(){
            {
                put("name","情报处理中心失效");
                put("x",400);
                put("y",100);
            }
        });
        nodes4_algorithm.add(1,new HashMap(){
            {
                put("name","获取处理能力C1");
                put("x",400);
                put("y",200);
            }
        });
        nodes4_algorithm.add(2,new HashMap(){
            {
                put("name","C1<20%");
                put("x",400);
                put("y",300);
            }
        });
        nodes4_algorithm.add(3,new HashMap(){
            {
                put("name","DLS算法(动态列表调度)");
                put("x",400);
                put("y",400);
            }
        });
        nodes4_algorithm.add(4,new HashMap(){
            {
                put("name","能力满足阈值：80%");
                put("x",400);
                put("y",500);
            }
        });
        nodes4_algorithm.add(5,new HashMap(){
            {
                put("name","结束");
                put("x",400);
                put("y",600);
            }
        });
        List<Map<String,Object>> links4_algorithm = new ArrayList<>(8);
        links4_algorithm.add(0,new HashMap(){
            {
                put("source","情报处理中心失效");
                put("target","获取处理能力C1");
            }
        });
        links4_algorithm.add(1,new HashMap(){
            {
                put("source","获取处理能力C1");
                put("target","C1<20%");
            }
        });
        links4_algorithm.add(2,new HashMap(){
            {
                put("source","C1<20%");
                put("target","DLS算法(动态列表调度)");
                put("formatter","是");
            }
        });
        links4_algorithm.add(3,new HashMap(){
            {
                put("source","C1<20%");
                put("target","结束");
                put("formatter","否");
                put("curveness",1);
            }
        });
        links4_algorithm.add(4,new HashMap(){
            {
                put("source","DLS算法(动态列表调度)");
                put("target","能力满足阈值：80%");
            }
        });
        /*links4_algorithm.add(5,new HashMap(){
            {
                put("source","能力满足阈值：80%");
                put("target","DLS算法(动态列表调度)");
                put("formatter","否");
            }
        });*/
        links4_algorithm.add(5,new HashMap(){
            {
                put("source","能力满足阈值：80%");
                put("target","结束");
                put("formatter","是");
            }
        });
        graph4_algorithm.put("nodes",nodes4_algorithm);
        graph4_algorithm.put("links",links4_algorithm);
        graphData.put("情报处理类资源失效算法模型2",graph4_algorithm);

        //算法模型类图5
        Map<String, Object> graph5_algorithm = new HashMap<>();
        List<Map> nodes5_algorithm = new ArrayList<>(7);
        nodes5_algorithm.add(0,new HashMap(){
            {
                put("name","通信无人机失效");
                put("x",400);
                put("y",100);
            }
        });
        nodes5_algorithm.add(1,new HashMap(){
            {
                put("name","获取通信能力C1");
                put("x",400);
                put("y",200);
            }
        });
        nodes5_algorithm.add(2,new HashMap(){
            {
                put("name","20%<=C1<=50%");
                put("x",400);
                put("y",300);
            }
        });
        nodes5_algorithm.add(3,new HashMap(){
            {
                put("name","n-Best+rollout算法");
                put("x",400);
                put("y",400);
            }
        });
        nodes5_algorithm.add(4,new HashMap(){
            {
                put("name","n = 3");
                put("x",400);
                put("y",500);
            }
        });
        nodes5_algorithm.add(5,new HashMap(){
            {
                put("name","结束");
                put("x",400);
                put("y",600);
            }
        });
        List<Map<String,Object>> links5_algorithm = new ArrayList<>(8);
        links5_algorithm.add(0,new HashMap(){
            {
                put("source","通信无人机失效");
                put("target","获取通信能力C1");
            }
        });
        links5_algorithm.add(1,new HashMap(){
            {
                put("source","获取通信能力C1");
                put("target","20%<=C1<=50%");
            }
        });
        links5_algorithm.add(2,new HashMap(){
            {
                put("source","20%<=C1<=50%");
                put("target","n-Best+rollout算法");
                put("formatter","是");
            }
        });
        links5_algorithm.add(3,new HashMap(){
            {
                put("source","20%<=C1<=50%");
                put("target","结束");
                put("formatter","否");
                put("curveness",1);
            }
        });
        links5_algorithm.add(4,new HashMap(){
            {
                put("source","n-Best+rollout算法");
                put("target","n = 3");
            }
        });
        /*links5_algorithm.add(5,new HashMap(){
            {
                put("source","n = 3");
                put("target","n-Best+rollout算法");
                put("formatter","否");
            }
        });*/
        links5_algorithm.add(5,new HashMap(){
            {
                put("source","n = 3");
                put("target","结束");
                put("formatter","是");
            }
        });
        graph5_algorithm.put("nodes",nodes5_algorithm);
        graph5_algorithm.put("links",links5_algorithm);
        graphData.put("通信类资源失效算法模型1",graph5_algorithm);

        //算法模型类图6
        Map<String, Object> graph6_algorithm = new HashMap<>();
        List<Map> nodes6_algorithm = new ArrayList<>(7);
        nodes6_algorithm.add(0,new HashMap(){
            {
                put("name","通信无人机失效");
                put("x",400);
                put("y",100);
            }
        });
        nodes6_algorithm.add(1,new HashMap(){
            {
                put("name","获取通信能力C1");
                put("x",400);
                put("y",200);
            }
        });
        nodes6_algorithm.add(2,new HashMap(){
            {
                put("name","C1<20%");
                put("x",400);
                put("y",300);
            }
        });
        nodes6_algorithm.add(3,new HashMap(){
            {
                put("name","DLS算法(动态列表调度)");
                put("x",400);
                put("y",400);
            }
        });
        nodes6_algorithm.add(4,new HashMap(){
            {
                put("name","能力满足阈值：50%");
                put("x",400);
                put("y",500);
            }
        });
        nodes6_algorithm.add(5,new HashMap(){
            {
                put("name","结束");
                put("x",400);
                put("y",600);
            }
        });
        List<Map<String,Object>> links6_algorithm = new ArrayList<>(8);
        links6_algorithm.add(0,new HashMap(){
            {
                put("source","通信无人机失效");
                put("target","获取通信能力C1");
            }
        });
        links6_algorithm.add(1,new HashMap(){
            {
                put("source","获取通信能力C1");
                put("target","C1<20%");
            }
        });
        links6_algorithm.add(2,new HashMap(){
            {
                put("source","C1<20%");
                put("target","DLS算法(动态列表调度)");
                put("formatter","是");
            }
        });
        links6_algorithm.add(3,new HashMap(){
            {
                put("source","C1<20%");
                put("target","结束");
                put("formatter","否");
                put("curveness",1);
            }
        });
        links6_algorithm.add(4,new HashMap(){
            {
                put("source","DLS算法(动态列表调度)");
                put("target","能力满足阈值：50%");
            }
        });
        /*links6_algorithm.add(5,new HashMap(){
            {
                put("source","能力满足阈值：50%");
                put("target","DLS算法(动态列表调度)");
                put("formatter","否");
            }
        });*/
        links6_algorithm.add(5,new HashMap(){
            {
                put("source","能力满足阈值：50%");
                put("target","结束");
                put("formatter","是");
            }
        });
        graph6_algorithm.put("nodes",nodes6_algorithm);
        graph6_algorithm.put("links",links6_algorithm);
        graphData.put("通信类资源失效算法模型2",graph6_algorithm);

        //算法模型类图7
        Map<String, Object> graph7_algorithm = new HashMap<>();
        List<Map> nodes7_algorithm = new ArrayList<>(7);
        nodes7_algorithm.add(0,new HashMap(){
            {
                put("name","对面歼击机失效");
                put("x",400);
                put("y",100);
            }
        });
        nodes7_algorithm.add(1,new HashMap(){
            {
                put("name","获取打击能力C1");
                put("x",400);
                put("y",200);
            }
        });
        nodes7_algorithm.add(2,new HashMap(){
            {
                put("name","20%<=C1<=50%");
                put("x",400);
                put("y",300);
            }
        });
        nodes7_algorithm.add(3,new HashMap(){
            {
                put("name","NSGA-II算法");
                put("x",400);
                put("y",400);
            }
        });
        nodes7_algorithm.add(4,new HashMap(){
            {
                put("name","迭代次数大于50");
                put("x",400);
                put("y",500);
            }
        });
        nodes7_algorithm.add(5,new HashMap(){
            {
                put("name","结束");
                put("x",400);
                put("y",600);
            }
        });
        List<Map<String,Object>> links7_algorithm = new ArrayList<>(8);
        links7_algorithm.add(0,new HashMap(){
            {
                put("source","对面歼击机失效");
                put("target","获取打击能力C1");
            }
        });
        links7_algorithm.add(1,new HashMap(){
            {
                put("source","获取打击能力C1");
                put("target","20%<=C1<=50%");
            }
        });
        links7_algorithm.add(2,new HashMap(){
            {
                put("source","20%<=C1<=50%");
                put("target","NSGA-II算法");
                put("formatter","是");
            }
        });
        links7_algorithm.add(3,new HashMap(){
            {
                put("source","20%<=C1<=50%");
                put("target","结束");
                put("formatter","否");
                put("curveness",1);
            }
        });
        links7_algorithm.add(4,new HashMap(){
            {
                put("source","NSGA-II算法");
                put("target","迭代次数大于50");
            }
        });
        /*links7_algorithm.add(5,new HashMap(){
            {
                put("source","迭代次数大于50");
                put("target","NSGA-II算法");
                put("formatter","否");
            }
        });*/
        links7_algorithm.add(5,new HashMap(){
            {
                put("source","迭代次数大于50");
                put("target","结束");
                put("formatter","是");
            }
        });
        graph7_algorithm.put("nodes",nodes7_algorithm);
        graph7_algorithm.put("links",links7_algorithm);
        graphData.put("兵力火力类资源失效算法模型1",graph1_algorithm);

        //算法模型类图8
        Map<String, Object> graph8_algorithm = new HashMap<>();
        List<Map> nodes8_algorithm = new ArrayList<>(7);
        nodes8_algorithm.add(0,new HashMap(){
            {
                put("name","对面歼击机失效");
                put("x",400);
                put("y",100);
            }
        });
        nodes8_algorithm.add(1,new HashMap(){
            {
                put("name","获取打击能力C1");
                put("x",400);
                put("y",200);
            }
        });
        nodes8_algorithm.add(2,new HashMap(){
            {
                put("name","C1<20%");
                put("x",400);
                put("y",300);
            }
        });
        nodes8_algorithm.add(3,new HashMap(){
            {
                put("name","DLS算法(动态列表调度)");
                put("x",400);
                put("y",400);
            }
        });
        nodes8_algorithm.add(4,new HashMap(){
            {
                put("name","能力满足阈值：60%");
                put("x",400);
                put("y",500);
            }
        });
        nodes8_algorithm.add(5,new HashMap(){
            {
                put("name","结束");
                put("x",400);
                put("y",600);
            }
        });
        List<Map<String,Object>> links8_algorithm = new ArrayList<>(8);
        links8_algorithm.add(0,new HashMap(){
            {
                put("source","对面歼击机失效");
                put("target","获取打击能力C1");
            }
        });
        links8_algorithm.add(1,new HashMap(){
            {
                put("source","获取打击能力C1");
                put("target","C1<20%");
            }
        });
        links8_algorithm.add(2,new HashMap(){
            {
                put("source","C1<20%");
                put("target","DLS算法(动态列表调度)");
                put("formatter","是");
            }
        });
        links8_algorithm.add(3,new HashMap(){
            {
                put("source","C1<20%");
                put("target","结束");
                put("formatter","否");
                put("curveness",1);
            }
        });
        links8_algorithm.add(4,new HashMap(){
            {
                put("source","DLS算法(动态列表调度)");
                put("target","能力满足阈值：60%");
            }
        });
        /*links8_algorithm.add(5,new HashMap(){
            {
                put("source","能力满足阈值：60%");
                put("target","DLS算法(动态列表调度)");
                put("formatter","否");
            }
        });*/
        links8_algorithm.add(5,new HashMap(){
            {
                put("source","能力满足阈值：60%");
                put("target","结束");
                put("formatter","是");
            }
        });
        graph8_algorithm.put("nodes",nodes8_algorithm);
        graph8_algorithm.put("links",links8_algorithm);
        graphData.put("兵力火力类资源失效算法模型2",graph8_algorithm);

    };

    public static void main(String[] args) throws JsonProcessingException {
        System.out.println(mapper.writeValueAsString(graphData));
    }

}
