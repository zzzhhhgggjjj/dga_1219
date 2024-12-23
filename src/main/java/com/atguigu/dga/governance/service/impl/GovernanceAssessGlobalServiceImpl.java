package com.atguigu.dga.governance.service.impl;

import com.atguigu.dga.governance.bean.GovernanceAssessGlobal;
import com.atguigu.dga.governance.bean.GovernanceAssessTecOwner;
import com.atguigu.dga.governance.mapper.GovernanceAssessGlobalMapper;
import com.atguigu.dga.governance.service.GovernanceAssessGlobalService;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 治理总考评表 服务实现类
 * </p>
 *
 * @author zhangchen
 * @since 2023-05-13
 */
@Service
@DS("dga")
public class GovernanceAssessGlobalServiceImpl extends ServiceImpl<GovernanceAssessGlobalMapper, GovernanceAssessGlobal> implements GovernanceAssessGlobalService {


    public void calcAssessGlobal(String assessDate){
        remove(new QueryWrapper<GovernanceAssessGlobal>().eq("assess_date",assessDate));

        GovernanceAssessGlobal governanceAssessGlobal = baseMapper.selectAssessGlobal(assessDate);

        save(governanceAssessGlobal);
    }
}
