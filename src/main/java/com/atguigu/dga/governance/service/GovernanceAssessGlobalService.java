package com.atguigu.dga.governance.service;

import com.atguigu.dga.governance.bean.GovernanceAssessGlobal;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 治理总考评表 服务类
 * </p>
 *
 * @author zhangchen
 * @since 2023-05-13
 */
public interface GovernanceAssessGlobalService extends IService<GovernanceAssessGlobal> {


    public void calcAssessGlobal(String assessDate);
}
