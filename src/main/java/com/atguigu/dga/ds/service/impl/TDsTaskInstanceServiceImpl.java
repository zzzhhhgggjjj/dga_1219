package com.atguigu.dga.ds.service.impl;

import com.atguigu.dga.ds.bean.TDsTaskInstance;
import com.atguigu.dga.ds.mapper.TDsTaskInstanceMapper;
import com.atguigu.dga.ds.service.TDsTaskInstanceService;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author zhangchen
 * @since 2023-05-12
 */
@Service
@DS("ds")
public class TDsTaskInstanceServiceImpl extends ServiceImpl<TDsTaskInstanceMapper, TDsTaskInstance> implements TDsTaskInstanceService {

}
