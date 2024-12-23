package com.atguigu.dga.governance.assessor.impl.storage;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.dga.governance.assessor.Assessor;
import com.atguigu.dga.governance.bean.AssessParam;
import com.atguigu.dga.governance.bean.GovernanceAssessDetail;
import com.atguigu.dga.governance.bean.GovernanceMetric;
import com.atguigu.dga.meta.bean.TableMetaInfo;
import com.atguigu.dga.meta.contants.MetaConst;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component("TABLE_SIMILAR")
public class TableSimilarAssessor extends Assessor {


    @Override
    protected void checkProblem(GovernanceAssessDetail governanceAssessDetail, AssessParam assessParam) throws Exception {
        // 判断素材： 1本表的所有字段
        //          2 其他所有表的 所有字段   // 方案1 现查 每个表都会查一次全表 浪费性能  // 方案2 可以提前做全表查询 作为参数传递过来
        //1提取本表字段  List  表名
        TableMetaInfo curTableMetaInfo = assessParam.getTableMetaInfo();
        String  colNameJson = curTableMetaInfo.getColNameJson();
        List<JSONObject>  curColList = JSON.parseArray(colNameJson,JSONObject.class);
        String curFullTableName=curTableMetaInfo.getSchemaName()+"."+curTableMetaInfo.getTableName();

        Set<String> similarTableSet=new HashSet<>();

        //2提取相似度阈值  指标参数里
        GovernanceMetric governanceMetric = assessParam.getGovernanceMetric();
        String metricParamsJson = governanceMetric.getMetricParamsJson();
        JSONObject paramJsonObj = JSON.parseObject(metricParamsJson);
        BigDecimal similarPercent = paramJsonObj.getBigDecimal("percent");


        //3 提取其他所有表Map
        Map<String, TableMetaInfo> tableMetaInfoMap = assessParam.getTableMetaInfoMap();

        //4 迭代 ： 其他表
        for (TableMetaInfo otherTableMetaInfo : tableMetaInfoMap.values()) {
            //其他表 表名
            String otherFullTableName=otherTableMetaInfo.getSchemaName()+"."+otherTableMetaInfo.getTableName();
            if(!curFullTableName.equals(otherFullTableName)    //要求：不能是同名  是同层
                    &&   curTableMetaInfo.getTableMetaInfoExtra().getDwLevel().equals( otherTableMetaInfo.getTableMetaInfoExtra().getDwLevel())
                    &&  ! curTableMetaInfo.getTableMetaInfoExtra().getDwLevel().equals(MetaConst.DW_LEVEL_ODS)){
                //准备比较字段
                String  otherColNameJson = otherTableMetaInfo.getColNameJson();
                List<JSONObject>  otherColList = JSON.parseArray(otherColNameJson,JSONObject.class);
                //思路 做一个同名字段计数器   每有一个字段相同+1    计数器/总字段数  和 阈值进行比较
                int sameFieldCount=0;
                //两层迭代  两个字段集比较
                for (JSONObject curJsonObj : curColList) {
                    for (JSONObject otherJsonObj : otherColList) {
                        String curFiledName = curJsonObj.getString("name");
                        String otherFiledName = otherJsonObj.getString("name");
                        if(curFiledName.equals(otherFiledName)){
                            sameFieldCount++;  //相同字段加加
                        }
                    }
                }
                int totalCurFieldCount = curColList.size();
                //相同字段数/总字段 *100
                BigDecimal curSimilarPercent = BigDecimal.valueOf(sameFieldCount).divide(BigDecimal.valueOf(totalCurFieldCount),2, RoundingMode.HALF_UP).movePointRight(2) ;

                if(curSimilarPercent.compareTo(similarPercent)>0){
                    similarTableSet.add(otherFullTableName);
                }

            }


        }
        //如果至少有一张相似表 则被判为0分
        if (similarTableSet.size()>0){
            governanceAssessDetail.setAssessScore(BigDecimal.ZERO);
            governanceAssessDetail.setAssessProblem("存在相似表： "+similarTableSet  );
        }

    }
}
