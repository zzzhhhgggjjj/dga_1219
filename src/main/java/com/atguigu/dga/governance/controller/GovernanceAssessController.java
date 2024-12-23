package com.atguigu.dga.governance.controller;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.dga.governance.bean.GovernanceAssessDetail;
import com.atguigu.dga.governance.bean.GovernanceAssessGlobal;
import com.atguigu.dga.governance.bean.GovernanceAssessTecOwner;
import com.atguigu.dga.governance.service.GovernanceAssessDetailService;
import com.atguigu.dga.governance.service.GovernanceAssessGlobalService;
import com.atguigu.dga.governance.service.GovernanceAssessTecOwnerService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/governance")
public class GovernanceAssessController {

    @Autowired
    GovernanceAssessDetailService governanceAssessDetailService;

    @Autowired
    GovernanceAssessGlobalService governanceAssessGlobalService;

    @Autowired
    GovernanceAssessTecOwnerService governanceAssessTecOwnerService;

    @GetMapping("/globalScore")
    public String getGlobalScore(){
        //最近一次考评全局记录
        GovernanceAssessGlobal governanceAssessGlobal = governanceAssessGlobalService.getOne(new QueryWrapper<GovernanceAssessGlobal>().orderByDesc("assess_date").last("limit 1 "));
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("assessDate",governanceAssessGlobal.getAssessDate());
        jsonObject.put("sumScore",governanceAssessGlobal.getScore());
        List<BigDecimal> scoreList=new ArrayList<>();
        scoreList.add(governanceAssessGlobal.getScoreSpec());
        scoreList.add(governanceAssessGlobal.getScoreStorage());
        scoreList.add(governanceAssessGlobal.getScoreCalc());
        scoreList.add(governanceAssessGlobal.getScoreQuality()==null?BigDecimal.valueOf(100):governanceAssessGlobal.getScoreQuality());
        scoreList.add(governanceAssessGlobal.getScoreSecurity());

        jsonObject.put("scoreList",scoreList);

        return jsonObject.toJSONString();
    }

    @GetMapping("/rankList")
    public  String getRankList(){
        //条件：最近一次考评日期
        QueryWrapper<GovernanceAssessTecOwner> queryWrapper = new QueryWrapper<GovernanceAssessTecOwner>().select("if(tec_owner is null , '未分配',tec_owner) as tecOwner", "score").last("where assess_date=(select max(assess_date) from governance_assess_tec_owner)");
        List<Map<String, Object>> listMaps = governanceAssessTecOwnerService.listMaps(queryWrapper);

        return JSON.toJSONString(listMaps);

    }

    @GetMapping("/problemNum")
    public String getProblemNum(){
        Map problemNumMap=new HashMap();

        List<Map<String, Object>> problemNumList = governanceAssessDetailService.getProblemNum();


        for (Map<String, Object> map : problemNumList) {
            String governance_type = (String)map.get("governance_type");
            Long ct = (Long)map.get("ct");

            problemNumMap.put(governance_type,ct);

        }
        return  JSON.toJSONString(problemNumMap);

    }

    @GetMapping("/problemList/{governanceType}/{pageNo}/{pageSize}")
    public String getProblemList(@PathVariable("governanceType") String governanceType ,
                                 @PathVariable("pageNo") Integer pageNo,
                                 @PathVariable("pageSize") Integer pageSize ){
        int from= (pageNo-1)*pageSize;
        QueryWrapper<GovernanceAssessDetail> queryWrapper = new QueryWrapper<GovernanceAssessDetail>().eq("governance_type", governanceType).lt("assess_score",BigDecimal.TEN)
                .last(" and assess_date = (select max(assess_date) from governance_assess_detail)  limit " + from + "," + pageSize);
        List<GovernanceAssessDetail> list = governanceAssessDetailService.list(queryWrapper);
         return  JSON.toJSONString(list);
    }

    @PostMapping("/assess/{date}")
    public String assess(@PathVariable("date") String assessDate){
        governanceAssessDetailService.mainAssess(assessDate);
        return "success";
    }

}
