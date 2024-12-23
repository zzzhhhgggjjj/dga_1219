package com.atguigu.dga.governance.mapper;

import com.atguigu.dga.governance.bean.GovernanceAssessTable;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 表治理考评情况 Mapper 接口
 * </p>
 *
 * @author zhangchen
 * @since 2023-05-13
 */
@Mapper
@DS("dga")
public interface GovernanceAssessTableMapper extends BaseMapper<GovernanceAssessTable> {


    @Select("select\n" +
            "    #{assessDate} assess_date, table_name,schema_name,tec_owner,\n" +
            "    avg(if(governance_type='SPEC',assess_score,null)) score_spec_avg,\n" +
            "    avg(if(governance_type='STORAGE',assess_score,null)) score_storage_avg,\n" +
            "    avg(if(governance_type='CALC',assess_score,null)) score_calc_avg,\n" +
            "    avg(if(governance_type='QUALITY',assess_score,null)) score_quality_avg,\n" +
            "    avg(if(governance_type='SECURITY',assess_score,null)) score_security_avg,\n" +
            "    sum(if(assess_score<10,1,0)) problem_num \n" +
            "    from governance_assess_detail where assess_date=#{assessDate}\n" +
            "group by  table_name,schema_name,tec_owner")
    public List<GovernanceAssessTable> getGovernanceAssessGroupByTable(@Param("assessDate") String assessDate);

}
