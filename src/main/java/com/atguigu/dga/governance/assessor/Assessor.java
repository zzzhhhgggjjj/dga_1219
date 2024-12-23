package com.atguigu.dga.governance.assessor;

import com.atguigu.dga.governance.bean.AssessParam;
import com.atguigu.dga.governance.bean.GovernanceAssessDetail;
import com.atguigu.dga.meta.bean.TableMetaInfo;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;

public abstract class Assessor {


    // 父类的控制方法   默认不变的操作+ 调用子类的核心操作
    public  final  GovernanceAssessDetail mainAssess(AssessParam assessParam){
        GovernanceAssessDetail governanceAssessDetail = new GovernanceAssessDetail();
        //1 处理默认值
        TableMetaInfo tableMetaInfo = assessParam.getTableMetaInfo();
        governanceAssessDetail.setAssessDate(assessParam.getAssessDate());
        governanceAssessDetail.setTableName(tableMetaInfo.getTableName());
        governanceAssessDetail.setSchemaName(tableMetaInfo.getSchemaName());

        governanceAssessDetail.setMetricId(assessParam.getGovernanceMetric().getId()+""); ;
        governanceAssessDetail.setMetricName(assessParam.getGovernanceMetric().getMetricName());
        governanceAssessDetail.setGovernanceType(assessParam.getGovernanceMetric().getGovernanceType());

        governanceAssessDetail.setTecOwner(tableMetaInfo.getTableMetaInfoExtra().getTecOwnerUserName());
        governanceAssessDetail.setAssessScore(BigDecimal.TEN);

        //3   调用子类的
        try {
            checkProblem(governanceAssessDetail,assessParam);
        }catch (Exception e){
            governanceAssessDetail.setIsAssessException("1");
           //2 把异常捕获同时 转换为string 写入字段中
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter=new PrintWriter(stringWriter);
            e.printStackTrace(printWriter);
            Integer len = stringWriter.toString().length();
            governanceAssessDetail.setAssessExceptionMsg(stringWriter.toString().substring(0, Math.min(len,2000) ) );
        }
        governanceAssessDetail.setCreateTime(new Date());

        return governanceAssessDetail;
    }


    protected abstract  void checkProblem(GovernanceAssessDetail governanceAssessDetail,AssessParam assessParam) throws  Exception;
}
