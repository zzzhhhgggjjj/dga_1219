package com.atguigu.dga.ds.service;

import com.atguigu.dga.ds.bean.TDsTaskDefinition;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author zhangchen
 * @since 2023-05-12
 */
public interface TDsTaskDefinitionService extends IService<TDsTaskDefinition> {


    //     提取列表 所有涉及考评的表的 任务定义 本次考评日运行过的   taskDefinition
    public List<TDsTaskDefinition> getTaskDefinitionListForAssess(String assessDate, List<String> tableNameList);

}
