package com.atguigu.dga.governance.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * <p>
 * 考评指标参数表
 * </p>
 *
 * @author zhangchen
 * @since 2023-05-09
 */
@Data
@TableName("governance_metric")
public class GovernanceMetric implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 指标名称
     */
    private String metricName;

    /**
     * 指标编码
     */
    private String metricCode;

    /**
     * 指标描述
     */
    private String metricDesc;

    /**
     * 治理类型
     */
    private String governanceType;

    /**
     * 指标参数
     */
    private String metricParamsJson;

    /**
     * 治理连接
     */
    private String governanceUrl;

    /**
     * 是否启用
     */
    private String isDisabled;
}
