package com.atguigu.dga.governance.service.impl;

import com.atguigu.dga.governance.bean.GovernanceAssessTable;
import com.atguigu.dga.governance.bean.GovernanceType;
import com.atguigu.dga.governance.mapper.GovernanceAssessTableMapper;
import com.atguigu.dga.governance.service.GovernanceAssessTableService;
import com.atguigu.dga.governance.service.GovernanceTypeService;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 表治理考评情况 服务实现类
 * </p>
 *
 * @author zhangchen
 * @since 2023-05-13
 */
@Service
@DS("dga")
public class GovernanceAssessTableServiceImpl extends ServiceImpl<GovernanceAssessTableMapper, GovernanceAssessTable> implements GovernanceAssessTableService {


    @Autowired
    GovernanceTypeService governanceTypeService;


    public void  calcAssessTable(String assessDate){


        remove(new QueryWrapper<GovernanceAssessTable>().eq("assess_date",assessDate));

        List<GovernanceAssessTable> governanceAssessGroupByTable = baseMapper.getGovernanceAssessGroupByTable(assessDate);

        List<GovernanceType> governanceTypeList = governanceTypeService.list();

        BigDecimal specWeight=BigDecimal.ZERO;
        BigDecimal storageWeight=BigDecimal.ZERO;
        BigDecimal calcWeight=BigDecimal.ZERO;
        BigDecimal qualityWeight=BigDecimal.ZERO;
        BigDecimal securityWeight=BigDecimal.ZERO;
        for (GovernanceType governanceType : governanceTypeList) {
            if(governanceType.getTypeCode().equals("SPEC")){
                specWeight=governanceType.getTypeWeight();
            } else if(governanceType.getTypeCode().equals("STORAGE")){
                storageWeight=governanceType.getTypeWeight();
            } else if(governanceType.getTypeCode().equals("CALC")){
                calcWeight=governanceType.getTypeWeight();
            } else if(governanceType.getTypeCode().equals("QUALITY")){
                qualityWeight=governanceType.getTypeWeight();
            }else if(governanceType.getTypeCode().equals("SECURITY")){
                securityWeight=governanceType.getTypeWeight();
            }

        }




        for (GovernanceAssessTable governanceAssessTable : governanceAssessGroupByTable) {

            BigDecimal specWeightScore = governanceAssessTable.getScoreSpecAvg().multiply(specWeight).movePointLeft(1);
            BigDecimal storageWeightScore = governanceAssessTable.getScoreStorageAvg().multiply(storageWeight).movePointLeft(1);
            BigDecimal calcWeightScore = governanceAssessTable.getScoreCalcAvg().multiply(calcWeight).movePointLeft(1);
            BigDecimal qualityWeightScore =  (governanceAssessTable.getScoreQualityAvg()==null?BigDecimal.TEN:governanceAssessTable.getScoreQualityAvg() ) .multiply(qualityWeight).movePointLeft(1);
            BigDecimal securityWeightScore = governanceAssessTable.getScoreSecurityAvg().multiply(securityWeight).movePointLeft(1);
            BigDecimal score =specWeightScore.add(storageWeightScore).add(calcWeightScore).add(qualityWeightScore).add(securityWeightScore);
            governanceAssessTable.setScoreOnTypeWeight( score.setScale(1, RoundingMode.HALF_UP));
            governanceAssessTable.setCreateTime(new Date());
        }

        saveBatch(governanceAssessGroupByTable);



    }
}
