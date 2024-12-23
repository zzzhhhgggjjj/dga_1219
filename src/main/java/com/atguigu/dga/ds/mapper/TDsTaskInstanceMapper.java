package com.atguigu.dga.ds.mapper;

import com.atguigu.dga.ds.bean.TDsTaskInstance;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author zhangchen
 * @since 2023-05-12
 */
@Mapper
@DS("ds")
public interface TDsTaskInstanceMapper extends BaseMapper<TDsTaskInstance> {

}
