package com.atguigu.dga.governance.assessor.impl.spec;

import com.atguigu.dga.governance.assessor.Assessor;
import com.atguigu.dga.governance.bean.AssessParam;
import com.atguigu.dga.governance.bean.GovernanceAssessDetail;
import com.atguigu.dga.meta.bean.TableMetaInfo;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;


@Component("TEC_OWNER")
public class CheckTecOwnerAssessor extends Assessor {

    @Override
    protected void checkProblem(GovernanceAssessDetail governanceAssessDetail, AssessParam assessParam) {
        TableMetaInfo tableMetaInfo = assessParam.getTableMetaInfo();
        if(tableMetaInfo.getTableMetaInfoExtra().getTecOwnerUserName()==null
                ||tableMetaInfo.getTableMetaInfoExtra().getTecOwnerUserName().length()==0){

            governanceAssessDetail.setAssessScore(BigDecimal.ZERO);
            governanceAssessDetail.setAssessProblem("未填写技术Owner");
            String governanceUrl = assessParam.getGovernanceMetric().getGovernanceUrl();
            //替换tableId
            governanceUrl = governanceUrl.replace("{tableId}", tableMetaInfo.getId() + "");
            governanceAssessDetail.setGovernanceUrl(governanceUrl);

        }
    }


}
