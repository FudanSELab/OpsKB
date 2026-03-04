package com.xidian.kg.service.Impl;

import com.xidian.kg.controller.util.Result;
import com.xidian.kg.dao.NodeDao;
import com.xidian.kg.dao.RelationDao;
import com.xidian.kg.entity.BasicNode;
import com.xidian.kg.entity.BasicRelationReturnVO;
import com.xidian.kg.entity.QueryRelation;
import com.xidian.kg.service.RelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RelationServiceImpl implements RelationService {

    @Autowired
    private RelationDao relationDao;

    @Autowired
    private NodeDao nodeDao;

    /**
     * 新建关系
     *
     * @param basicRelationReturnVO
     * @return
     */
    @Override
    public Result createRelation(BasicRelationReturnVO basicRelationReturnVO) {
        BasicNode start = basicRelationReturnVO.getStart();
        BasicNode end = basicRelationReturnVO.getEnd();
        if (nodeDao.queryNode(start, false).size() > 0 && nodeDao.queryNode(end, false).size() > 0) {
            if (relationDao.queryRelation(basicRelationReturnVO) > 0) {
                return new Result(false, "该关系已经存在！！");
            } else {
                Integer result = relationDao.createRelation(basicRelationReturnVO);
                return new Result(true, result);
            }
        } else {
            return new Result(false, "所选节点不存在！");
        }
    }

    /**
     * 删除关系
     *
     * @param basicRelationReturnVO
     * @return
     */
    @Override
    public Result deleteRelation(BasicRelationReturnVO basicRelationReturnVO) {
        BasicNode start = basicRelationReturnVO.getStart();
        BasicNode end = basicRelationReturnVO.getEnd();
        if (nodeDao.queryNode(start, false).size() > 0 && nodeDao.queryNode(end, false).size() > 0) {
            if (relationDao.queryRelation(basicRelationReturnVO) > 0) {
                Integer result = relationDao.deleteRelation(basicRelationReturnVO);
                return new Result(true, result);
            } else {
                return new Result(false, "关系不存在！");
            }
        } else {
            return new Result(false, "所选节点不存在！");
        }
    }

    /**
     * 获取某一节点的所有关系
     *
     * @param basicNode
     * @return
     */
    @Override
    public Result getNodeRelation(BasicNode basicNode) {
        if (nodeDao.queryNode(basicNode, false).size() > 0) {
            List<List> result = new ArrayList<>();
            if (relationDao.getNodeRelationForward(basicNode).size() > 0 || relationDao.getNodeRelationOpposite(basicNode).size() > 0) {
                List<BasicRelationReturnVO> relation = relationDao.getNodeRelationForward(basicNode);
                List<BasicRelationReturnVO> list= new ArrayList<>();
                for(BasicRelationReturnVO relationReturnVO : relation){
                    list.add(relationReturnVO);
                }
                relation = relationDao.getNodeRelationOpposite(basicNode);
                for(BasicRelationReturnVO relationReturnVO : relation){
                    list.add(relationReturnVO);
                }
                result.add(list);
            }else {
                return new Result(false, "关系不存在");
            }
            return new Result(true, result);
        } else {
            return new Result(false, "节点不存在");
        }
    }

    /**
     * 修改关系属性值
     *
     * @param basicRelationReturnVO
     * @return
     */
    @Override
    public Result updateRelationProperties(BasicRelationReturnVO basicRelationReturnVO) {
        BasicNode start = basicRelationReturnVO.getStart();
        BasicNode end = basicRelationReturnVO.getEnd();
        QueryRelation relation = basicRelationReturnVO.getRelation();
        if (nodeDao.queryNode(start, false).size() > 0 && nodeDao.queryNode(end, false).size() > 0) {
            if (relationDao.queryRelation(basicRelationReturnVO) > 0) {
                Integer result = relationDao.updateRelationProperties(basicRelationReturnVO);
                return new Result(true, result);
            } else {
                return new Result(false, "关系不存在！");
            }
        } else {
            return new Result(false, "节点不存在!");
        }
    }
}
