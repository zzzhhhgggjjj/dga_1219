package com.atguigu.dga.governance.service;

import com.atguigu.dga.governance.bean.GovernanceAssessTecOwner;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 技术负责人治理考评表 服务类
 * </p>
 *
 * @author zhangchen
 * @since 2023-05-13
 */
public interface GovernanceAssessTecOwnerService extends IService<GovernanceAssessTecOwner> {

    public void calcAssessTecOwner(String assessDate);
}
