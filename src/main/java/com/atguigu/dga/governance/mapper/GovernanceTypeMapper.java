package com.atguigu.dga.governance.mapper;

import com.atguigu.dga.governance.bean.GovernanceType;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 治理考评类别权重表 Mapper 接口
 * </p>
 *
 * @author zhangchen
 * @since 2023-05-09
 */
@Mapper
@DS("dga")
public interface GovernanceTypeMapper extends BaseMapper<GovernanceType> {

}
