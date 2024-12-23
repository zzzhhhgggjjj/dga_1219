package com.atguigu.dga.governance.mapper;

import com.atguigu.dga.governance.bean.GovernanceAssessDetail;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 治理考评结果明细 Mapper 接口
 * </p>
 *
 * @author zhangchen
 * @since 2023-05-09
 */
@Mapper
@DS("dga")
public interface GovernanceAssessDetailMapper extends BaseMapper<GovernanceAssessDetail> {


    @Select("select governance_type, count(*) ct from governance_assess_detail\n" +
            "where  assess_date=(select max(assess_date) from governance_assess_detail )\n" +
            "and assess_score<10\n" +
            "group by governance_type")
    public List<Map<String,Object>> selectProblemNum();
}
