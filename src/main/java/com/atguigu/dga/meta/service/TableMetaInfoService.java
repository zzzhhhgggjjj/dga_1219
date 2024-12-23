package com.atguigu.dga.meta.service;

import com.atguigu.dga.meta.bean.TableMetaInfo;
import com.atguigu.dga.meta.bean.TableMetaInfoQuery;
import com.atguigu.dga.meta.bean.TableMetaInfoVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 元数据表 服务类
 * </p>
 *
 * @author zhangchen
 * @since 2023-05-06
 */
public interface TableMetaInfoService extends IService<TableMetaInfo> {


    //初始化table_meta_info    extract  init
    public  void initTableMeta(String assessDate,String schemaName);

    public List<TableMetaInfoVO> getTableMetaInfoList(TableMetaInfoQuery tableMetaInfoQuery);

    public Integer getTableMetaInfoCount(TableMetaInfoQuery tableMetaInfoQuery);

    public List<TableMetaInfo> getTableMetaInfoAllList();

}
