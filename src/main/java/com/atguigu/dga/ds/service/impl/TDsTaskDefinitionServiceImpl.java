package com.atguigu.dga.ds.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.dga.ds.bean.TDsTaskDefinition;
import com.atguigu.dga.ds.mapper.TDsTaskDefinitionMapper;
import com.atguigu.dga.ds.service.TDsTaskDefinitionService;
import com.atguigu.dga.meta.util.SQLUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author zhangchen
 * @since 2023-05-12
 */
@Service
@DS("ds")
public class TDsTaskDefinitionServiceImpl extends ServiceImpl<TDsTaskDefinitionMapper, TDsTaskDefinition> implements TDsTaskDefinitionService {


    ////     提取列表 所有涉及考评的表的 任务定义 本次考评日运行过的   taskDefinition
    // 是否需要写sql  还是 mbp 的方法组合即可
    //   select * from TaskDefinition where 1 xxx  in (xx)  and  2  exists ( select 1 from  task_instance ti where ti.xx=td.xx and ti start_date =assessDate    )
    @Override
    public List<TDsTaskDefinition> getTaskDefinitionListForAssess(String assessDate, List<String> tableNameList) {
        List<TDsTaskDefinition> tDsTaskDefinitionList = list(new QueryWrapper<TDsTaskDefinition>().in("name", tableNameList)
                .exists(" select 1 from  t_ds_task_instance ti where ti.task_code=code and date_format(ti.start_time,'%Y-%m-%d')  = '"+ SQLUtil.filterUnsafeSql(assessDate)+"'"));

        for (TDsTaskDefinition tDsTaskDefinition : tDsTaskDefinitionList) {
            String sql = extractSqlFromShellParam(tDsTaskDefinition.getTaskParams());

            tDsTaskDefinition.setSql(sql);
        }

        return tDsTaskDefinitionList;
    }

    //从shell 任务节点的参数中提取sql
    //1  通过json的转化 提取出shell脚本

    //2  找到sql在shell中的头尾坐标 进行切割
    private  String extractSqlFromShellParam(String shellTaskParam){
        //1  通过json的转化 提取出shell脚本
        JSONObject paramJsonObj = JSON.parseObject(shellTaskParam);
        String shellScript = paramJsonObj.getString("rawScript");
        //2  找到sql在shell中的头尾坐标 进行切割
        int sqlStartIdx=-1;
        int sqlEndIdx=-1;
        sqlStartIdx = shellScript.indexOf("with"); // -1 标识没找到
        if(sqlStartIdx<0){
            sqlStartIdx=shellScript.indexOf("insert");
        }

        int qouteIdx  = shellScript.indexOf(";", sqlStartIdx);
        int backslashIdx  = shellScript.indexOf("\"", sqlStartIdx);
        if(qouteIdx>0&&backslashIdx>0){  //都存在取最小
            sqlEndIdx=  Math.min(qouteIdx,backslashIdx);
        }else{
            sqlEndIdx=Math.max(qouteIdx,backslashIdx); //某个不存在是  取最大 即（存在的那个）
        }

        String sqlString = shellScript.substring(sqlStartIdx, sqlEndIdx);
        return sqlString;
    }
}
