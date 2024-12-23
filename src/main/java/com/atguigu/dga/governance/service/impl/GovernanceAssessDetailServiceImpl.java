package com.atguigu.dga.governance.service.impl;

import com.atguigu.dga.ds.bean.TDsTaskDefinition;
import com.atguigu.dga.ds.service.TDsTaskDefinitionService;
import com.atguigu.dga.ds.service.TDsTaskInstanceService;
import com.atguigu.dga.governance.assessor.Assessor;
import com.atguigu.dga.governance.bean.AssessParam;
import com.atguigu.dga.governance.bean.GovernanceAssessDetail;
import com.atguigu.dga.governance.bean.GovernanceMetric;
import com.atguigu.dga.governance.mapper.GovernanceAssessDetailMapper;
import com.atguigu.dga.governance.service.*;
import com.atguigu.dga.governance.util.SpringBeanProvider;
import com.atguigu.dga.meta.bean.TableMetaInfo;
import com.atguigu.dga.meta.service.TableMetaInfoExtraService;
import com.atguigu.dga.meta.service.TableMetaInfoService;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <p>
 * 治理考评结果明细 服务实现类
 * </p>
 *
 * @author zhangchen
 * @since 2023-05-09
 */
@Service
@DS("dga")
public class GovernanceAssessDetailServiceImpl extends ServiceImpl<GovernanceAssessDetailMapper, GovernanceAssessDetail> implements GovernanceAssessDetailService {


        ThreadPoolExecutor threadPoolExecutor=new ThreadPoolExecutor(100,100,600, TimeUnit.SECONDS,new LinkedBlockingDeque<>());

        @Autowired
        TableMetaInfoService tableMetaInfoService;

        @Autowired
        TableMetaInfoExtraService tableMetaInfoExtraService;

        @Autowired
        GovernanceMetricService governanceMetricService;

        @Autowired
       TDsTaskDefinitionService tDsTaskDefinitionService;

        @Autowired
        GovernanceAssessTecOwnerService governanceAssessTecOwnerService;
        @Autowired
        GovernanceAssessTableService governanceAssessTableService;
        @Autowired
        GovernanceAssessGlobalService governanceAssessGlobalService;

//1     把数仓元数据提取出来   table_meta_info   table_meta_info_extra
//2      把指标清单提取出来   governance_metrics
//3     元数据列表和指标列表  双层循环   依次 进行考评
//4    获得一个 考评结果明细列表   进行保存
    public  void   governanceAssess(String assessDate){
        //0  清理
        remove(new QueryWrapper<GovernanceAssessDetail>().eq("assess_date",assessDate));


        //1     把数仓元数据提取出来   table_meta_info   table_meta_info_extra   List<TableMetaInfo>
//        List<TableMetaInfo>  tableMetaInfoList=tableMetaInfoService.list();
//        for (TableMetaInfo tableMetaInfo : tableMetaInfoList) {
//            TableMetaInfoExtra tableMetaInfoExtra = tableMetaInfoExtraService.getOne();
//            tableMetaInfo.setTableMetaInfoExtra(tableMetaInfoExtra);
//        }

        List<TableMetaInfo>  tableMetaInfoList=tableMetaInfoService.getTableMetaInfoAllList();
        Map<String,TableMetaInfo> tableMetaInfoMap=new HashMap<>(tableMetaInfoList.size() );
        for (TableMetaInfo tableMetaInfo : tableMetaInfoList) {
            tableMetaInfoMap.put(tableMetaInfo.getSchemaName()+"."+tableMetaInfo.getTableName() ,tableMetaInfo);
        }



        //2      把指标清单提取出来   governance_metrics
        List<GovernanceMetric> governanceMetricList = governanceMetricService.list(new QueryWrapper<GovernanceMetric>().ne("is_disabled", "1"));


        //3    为了方便涉及ds的指标考评，提前一次性提取任务的ds定义
        //     提取列表 所有涉及考评的表的 任务定义 本次考评日运行过的   taskDefinition
        List<String> tableNameList = tableMetaInfoList.stream().map(tableMetaInfo -> tableMetaInfo.getSchemaName() + "." + tableMetaInfo.getTableName()).collect(Collectors.toList());

        List<TDsTaskDefinition> taskDefinitionList = tDsTaskDefinitionService.getTaskDefinitionListForAssess(assessDate, tableNameList);
        Map<String ,TDsTaskDefinition>  tDsTaskDefinitionMap= new HashMap<>(taskDefinitionList.size());
        for (TDsTaskDefinition tDsTaskDefinition : taskDefinitionList) {
            tDsTaskDefinitionMap.put(tDsTaskDefinition.getName(),tDsTaskDefinition);
        }



        //4     元数据列表和指标列表  双层循环   依次 进行考评
        List<GovernanceAssessDetail> governanceAssessDetailList=new ArrayList<>(tableMetaInfoList.size() * governanceMetricList.size());

        // 5    准备一个异步结果的列表
        List<CompletableFuture<GovernanceAssessDetail>>  completableFutureList=new ArrayList<>(tableMetaInfoList.size() * governanceMetricList.size());

        for (TableMetaInfo tableMetaInfo : tableMetaInfoList) {
            for (GovernanceMetric governanceMetric : governanceMetricList) {
                String metricCode = governanceMetric.getMetricCode();
                // 根据MetricCode产生一个实现类

                AssessParam assessParam = new AssessParam();
                assessParam.setAssessDate(assessDate);
                assessParam.setTableMetaInfo(tableMetaInfo);
                assessParam.setGovernanceMetric(governanceMetric);
                assessParam.setTableMetaInfoMap(tableMetaInfoMap);
                assessParam.setTDsTaskDefinition(tDsTaskDefinitionMap.get(tableMetaInfo.getSchemaName()+"."+tableMetaInfo.getTableName() ));

                Assessor assessor= SpringBeanProvider.getBean(metricCode,Assessor.class);   //getAssessorByCode(metricCode); // 根据code装配



             //   GovernanceAssessDetail governanceAssessDetail = assessor.mainAssess(assessParam); //同步方法
             //   governanceAssessDetailList.add(governanceAssessDetail);
                //异步方法
                CompletableFuture<GovernanceAssessDetail> completableFuture = CompletableFuture.supplyAsync(() -> {
                            //业务逻辑
                            GovernanceAssessDetail governanceAssessDetail = assessor.mainAssess(assessParam);
                            return governanceAssessDetail;
                        }, threadPoolExecutor
                );
                completableFutureList.add(completableFuture);


            }
        }
        //异步集结
          governanceAssessDetailList = completableFutureList.stream().map(future -> future.join()).collect(Collectors.toList());

        //5    获得一个 考评结果明细列表   进行保存
        saveBatch(governanceAssessDetailList);


    }



    public void   mainAssess(String assessDate){
        long startTs = System.currentTimeMillis();
        governanceAssess(assessDate);
        System.out.println(" 考评处理时间 = " + (System.currentTimeMillis() - startTs));

        governanceAssessTableService.calcAssessTable(assessDate);

        governanceAssessTecOwnerService.calcAssessTecOwner(assessDate);

        governanceAssessGlobalService.calcAssessGlobal(assessDate);

    }



    public List<Map<String,Object>> getProblemNum() {

      return    baseMapper.selectProblemNum();
    }


}
