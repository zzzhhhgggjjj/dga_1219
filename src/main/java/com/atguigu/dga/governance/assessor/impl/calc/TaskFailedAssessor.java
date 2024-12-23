package com.atguigu.dga.governance.assessor.impl.calc;

import com.atguigu.dga.ds.bean.TDsTaskInstance;
import com.atguigu.dga.ds.service.TDsTaskInstanceService;
import com.atguigu.dga.governance.assessor.Assessor;
import com.atguigu.dga.governance.bean.AssessParam;
import com.atguigu.dga.governance.bean.GovernanceAssessDetail;
import com.atguigu.dga.meta.bean.TableMetaInfo;
import com.atguigu.dga.meta.contants.MetaConst;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;


@Component("TASK_FAILED")
public class TaskFailedAssessor extends Assessor {

    @Autowired
    TDsTaskInstanceService tDsTaskInstanceService;

    @Override
    protected void checkProblem(GovernanceAssessDetail governanceAssessDetail, AssessParam assessParam) throws Exception {
        TableMetaInfo tableMetaInfo = assessParam.getTableMetaInfo();
        String assessDate = assessParam.getAssessDate();

        //1  获得当日任务实例 检查实例是否有错误: 条件 1 对应表 2 当日 3 运行失败
        List<TDsTaskInstance> failedTaskList = tDsTaskInstanceService.list(new QueryWrapper<TDsTaskInstance>().eq("name", tableMetaInfo.getSchemaName() + "." + tableMetaInfo.getTableName())
                .eq("date_format(start_time,'%Y-%m-%d')", assessDate)
                .eq("state", MetaConst.TASK_STATE_FAILED));

        //2  打分
        if(failedTaskList.size()>0){
            governanceAssessDetail.setAssessScore(BigDecimal.ZERO);
            governanceAssessDetail.setAssessProblem("存在任务失败");
        }

    }
}
