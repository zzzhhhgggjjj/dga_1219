package com.atguigu.dga;

import com.atguigu.dga.governance.mapper.GovernanceAssessTecOwnerMapper;
import com.atguigu.dga.governance.service.GovernanceAssessDetailService;
import com.atguigu.dga.governance.service.GovernanceAssessGlobalService;
import com.atguigu.dga.governance.service.GovernanceAssessTableService;
import com.atguigu.dga.governance.service.GovernanceAssessTecOwnerService;
import com.atguigu.dga.meta.bean.TableMetaInfo;
import com.atguigu.dga.meta.service.TableMetaInfoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class Dga1219ApplicationTests {

    @Autowired
    TableMetaInfoService tableMetaInfoService;

    @Autowired
    GovernanceAssessDetailService governanceAssessDetailService;

    @Autowired
    GovernanceAssessTableService governanceAssessTableService;

    @Autowired
    GovernanceAssessTecOwnerService governanceAssessTecOwnerService;

    @Autowired
    GovernanceAssessGlobalService governanceAssessGlobalService;

    @Test
    void contextLoads() {
    }



    @Test
    void testTableMeta(){
        tableMetaInfoService.initTableMeta("2023-05-08","gmall");
    }


    @Test
    void testTableMetaList(){
        List<TableMetaInfo> tableMetaInfoAllList = tableMetaInfoService.getTableMetaInfoAllList();
        System.out.println(tableMetaInfoAllList);
    }

    @Test
    void  testAssess(){
        governanceAssessDetailService.governanceAssess("2023-05-01");
    }

    @Test
    void testAssessTable(){
        governanceAssessTableService.calcAssessTable("2023-05-01");
    }

    @Test
    void testAssessTecOwner(){
        governanceAssessTecOwnerService.calcAssessTecOwner("2023-05-01");
    }


    @Test
    void testAssessGlobal(){
        governanceAssessGlobalService.calcAssessGlobal("2023-05-01");
    }

    @Test
    void  testMainAssess(){
        governanceAssessDetailService.mainAssess("2023-05-01");
    }
}
