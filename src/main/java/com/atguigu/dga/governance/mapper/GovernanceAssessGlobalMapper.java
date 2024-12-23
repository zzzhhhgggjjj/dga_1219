package com.atguigu.dga.governance.mapper;

import com.atguigu.dga.governance.bean.GovernanceAssessGlobal;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 治理总考评表 Mapper 接口
 * </p>
 *
 * @author zhangchen
 * @since 2023-05-13
 */
@Mapper
@DS("dga")
public interface GovernanceAssessGlobalMapper extends BaseMapper<GovernanceAssessGlobal> {


    @Select("select\n" +
            "       avg(score_spec_avg)*10 score_spec,\n" +
            "       avg(score_storage_avg)*10 score_storage,\n" +
            "       avg( score_calc_avg)*10 score_calc,\n" +
            "       avg( score_quality_avg)*10 score_quality,\n" +
            "       avg( score_security_avg) *10 score_security,\n" +
            "       avg( score_on_type_weight)   score,\n" +
            "       count(*) table_num,\n" +
            "       sum(problem_num) problem_num,\n" +
            "        now() create_time,\n" +
            "       #{assessDate} assess_date\n" +
            "from governance_assess_table\n" +
            " where   assess_date=#{assessDate}")
    public  GovernanceAssessGlobal  selectAssessGlobal(@Param("assessDate") String assessDate);
}
