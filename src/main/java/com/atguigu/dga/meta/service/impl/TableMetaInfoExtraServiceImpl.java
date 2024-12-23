package com.atguigu.dga.meta.service.impl;

import com.atguigu.dga.meta.bean.TableMetaInfo;
import com.atguigu.dga.meta.bean.TableMetaInfoExtra;
import com.atguigu.dga.meta.contants.MetaConst;
import com.atguigu.dga.meta.mapper.TableMetaInfoExtraMapper;
import com.atguigu.dga.meta.service.TableMetaInfoExtraService;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 元数据表附加信息 服务实现类
 * </p>
 *
 * @author zhangchen
 * @since 2023-05-08
 */
@Service
@DS("dga")
public class TableMetaInfoExtraServiceImpl extends ServiceImpl<TableMetaInfoExtraMapper, TableMetaInfoExtra> implements TableMetaInfoExtraService {


    // 根源生成的元数据信息对辅助信息进行初始化
    public void genTableMetaInfoExtra(List<TableMetaInfo> tableMetaInfoList){
        //1 迭代每张表
        //2  判断该表的辅助信息是否已经有了
        //3  如果没有则进行初始化
        //4 保存
        List<TableMetaInfoExtra> tableMetaInfoExtraList=new ArrayList<>();
        for (TableMetaInfo tableMetaInfo : tableMetaInfoList) {
            TableMetaInfoExtra tableMetaInfoExtra = getOne(new QueryWrapper<TableMetaInfoExtra>().eq("table_name", tableMetaInfo.getTableName())
                    .eq("schema_name", tableMetaInfo.getSchemaName()));
            if(tableMetaInfoExtra==null){
                tableMetaInfoExtra=new TableMetaInfoExtra();
                tableMetaInfoExtra.setTableName(tableMetaInfo.getTableName());
                tableMetaInfoExtra.setSchemaName(tableMetaInfo.getSchemaName());
                tableMetaInfoExtra.setLifecycleType(MetaConst.LIFECYCLE_TYPE_UNSET);
                tableMetaInfoExtra.setSecurityLevel(MetaConst.SECURITY_LEVEL_UNSET);

                tableMetaInfoExtra.setDwLevel( getInitDwLevelByTableName(tableMetaInfo.getTableName()));
                tableMetaInfoExtra.setCreateTime(new Date());
                tableMetaInfoExtraList.add(tableMetaInfoExtra);
            }


        }

        saveOrUpdateBatch(tableMetaInfoExtraList);


    }


    private String getInitDwLevelByTableName(String tableName){
        if(tableName.startsWith("ods")){
            return "ODS";
        } else if (tableName.startsWith("dwd")) {
            return "DWD";
        }else if (tableName.startsWith("dim")) {
            return "DIM";
        }else if (tableName.startsWith("dws")) {
            return "DWS";
        }else if (tableName.startsWith("ads")) {
            return "ADS";
        }else if (tableName.startsWith("dm")) {
            return "DM";    //  跨数据域   针对某些特定场景的一些表  比较灵活
        }else  {
            return "OTHER";
        }
    }

}
