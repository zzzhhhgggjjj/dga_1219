package com.atguigu.dga.governance.service;

import com.atguigu.dga.governance.bean.GovernanceAssessDetail;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 治理考评结果明细 服务类
 * </p>
 *
 * @author zhangchen
 * @since 2023-05-09
 */
public interface GovernanceAssessDetailService extends IService<GovernanceAssessDetail> {


    public  void   governanceAssess(String assessDate);

    public void   mainAssess(String assessDate);

    public List<Map<String,Object>> getProblemNum();
}
