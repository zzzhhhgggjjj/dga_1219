package com.atguigu.dga.governance.assessor.impl.calc;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.dga.governance.assessor.Assessor;
import com.atguigu.dga.governance.bean.AssessParam;
import com.atguigu.dga.governance.bean.GovernanceAssessDetail;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

@Component("NO_ACCESS")
public class NoAccessAssessor extends Assessor {
    @Override
    protected void checkProblem(GovernanceAssessDetail governanceAssessDetail, AssessParam assessParam) throws  Exception {
        //取考评时间  日期
        String assessDate = assessParam.getAssessDate();
        Calendar assessCald = Calendar.getInstance();
        assessCald.setTime(DateUtils.parseDate(assessDate,"yyyy-MM-dd") );
        int assessDateInt = assessCald.get(Calendar.DATE);
        //取 最近一次访问时间  日期

        Date tableLastAccessTime = assessParam.getTableMetaInfo().getTableLastAccessTime();
        Calendar lastAccessCald = Calendar.getInstance();
        lastAccessCald.setTime(tableLastAccessTime);
        int lastAccessDateInt = lastAccessCald.get(Calendar.DATE);

        // 获得差值  跟参数中的阈值 对比
        int diffDays = assessDateInt - lastAccessDateInt;
        String metricParamsJson = assessParam.getGovernanceMetric().getMetricParamsJson();
        JSONObject jsonObject = JSON.parseObject(metricParamsJson);
        Integer days = jsonObject.getInteger("days");

        // 判断给分
        if(diffDays>=days){
            governanceAssessDetail.setAssessScore(BigDecimal.ZERO);
            governanceAssessDetail.setAssessProblem("长期未被访问,距离上次访问："+diffDays+"天");
        }
    }


}
