package com.atguigu.dga.governance.task;


import com.atguigu.dga.governance.bean.GovernanceAssessDetail;
import com.atguigu.dga.governance.service.GovernanceAssessDetailService;
import com.atguigu.dga.meta.service.TableMetaInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class GovernanceAssessTask {

    @Value("${govenance.schema-name}")
    String schemaNames;

    @Autowired
    TableMetaInfoService tableMetaInfoService;

    @Autowired
    GovernanceAssessDetailService governanceAssessDetailService;

    @Scheduled(cron = "0 23 14 * * *")
    public void  assess(){
        // 1 元数据采集
        String[] schemaNameArr = schemaNames.split(",");
        for (String schemaName : schemaNameArr) {
            tableMetaInfoService.initTableMeta("2023-05-01",schemaName);
        }
        //2  考评
        governanceAssessDetailService.mainAssess("2023-05-01");

    }
}
