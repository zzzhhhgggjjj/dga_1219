package com.atguigu.dga.meta.service;

import com.atguigu.dga.meta.bean.TableMetaInfo;
import com.atguigu.dga.meta.bean.TableMetaInfoExtra;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 元数据表附加信息 服务类
 * </p>
 *
 * @author zhangchen
 * @since 2023-05-08
 */
public interface TableMetaInfoExtraService extends IService<TableMetaInfoExtra> {

    public void genTableMetaInfoExtra(List<TableMetaInfo> tableMetaInfoList);

}
