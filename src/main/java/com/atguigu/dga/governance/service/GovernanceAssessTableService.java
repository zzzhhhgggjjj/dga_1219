package com.atguigu.dga.governance.service;

import com.atguigu.dga.governance.bean.GovernanceAssessTable;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 表治理考评情况 服务类
 * </p>
 *
 * @author zhangchen
 * @since 2023-05-13
 */
public interface GovernanceAssessTableService extends IService<GovernanceAssessTable> {


    public void  calcAssessTable(String assessDate);
}
