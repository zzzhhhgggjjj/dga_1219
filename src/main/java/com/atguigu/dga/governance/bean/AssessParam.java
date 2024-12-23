package com.atguigu.dga.governance.bean;

import com.atguigu.dga.ds.bean.TDsTaskDefinition;
import com.atguigu.dga.meta.bean.TableMetaInfo;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class AssessParam {

    String assessDate;

    TableMetaInfo tableMetaInfo;

    GovernanceMetric governanceMetric;

    Map<String,TableMetaInfo> tableMetaInfoMap;

    //ds 任务定义
    List<TDsTaskDefinition> taskDefinitionList ;

    TDsTaskDefinition tDsTaskDefinition;
}
