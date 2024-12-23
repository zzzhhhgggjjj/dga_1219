package com.atguigu.dga.governance.mapper;

import com.atguigu.dga.governance.bean.GovernanceAssessTable;
import com.atguigu.dga.governance.bean.GovernanceAssessTecOwner;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 技术负责人治理考评表 Mapper 接口
 * </p>
 *
 * @author zhangchen
 * @since 2023-05-13
 */
@Mapper
@DS("dga")
public interface GovernanceAssessTecOwnerMapper extends BaseMapper<GovernanceAssessTecOwner> {


    @Select("select tec_owner ,\n" +
            "       avg(score_spec_avg) score_spec,\n" +
            "       avg(score_storage_avg) score_storage,\n" +
            "       avg( score_calc_avg) score_calc,\n" +
            "       avg( score_quality_avg) score_quality,\n" +
            "       avg( score_security_avg) score_security,\n" +
            "       avg( score_on_type_weight) score,\n" +
            "       count(*) table_num,\n" +
            "       sum(problem_num) problem_num,\n" +
            "        now() create_time,\n" +
            "      #{assessDate}  assess_date \n" +
            " from governance_assess_table\n" +
            " where   assess_date=#{assessDate}\n" +
            " group by  tec_owner")
    public List<GovernanceAssessTecOwner> selectAssessGroupByTecOwner(@Param("assessDate") String assessDate);

}
