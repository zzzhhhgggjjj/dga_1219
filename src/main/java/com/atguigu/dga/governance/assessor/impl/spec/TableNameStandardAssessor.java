package com.atguigu.dga.governance.assessor.impl.spec;

import com.atguigu.dga.governance.assessor.Assessor;
import com.atguigu.dga.governance.bean.AssessParam;
import com.atguigu.dga.governance.bean.GovernanceAssessDetail;
import com.atguigu.dga.meta.bean.TableMetaInfoExtra;
import com.atguigu.dga.meta.contants.MetaConst;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component("TABLE_NAME_STANDARD")
public class TableNameStandardAssessor extends Assessor {

//    参考建数仓表规范
//    ODS层 ：开头:ods  结尾 :inc/full
//    结构ods_xx_( inc|full)
//    DIM层 :  dim开头     full/zip 结尾
//    结构: dim_xx_( zip|full)
//    DWD层:  dwd 开头  inc/full 结尾
//    结构： dwd_xx_xx_(inc|full)
//    DWS层： dws开头
//    结构dws_xx_xx_xx_(1d/nd/td)
//    ADS层： ads 开头
//    结构 ads_xxx
//    DM层： dm开头
//    结构: dm_xx
//    符合则 10分，否则0分
//    OTHER：
//    未纳入分层，给5分

    Pattern odsPattern = Pattern.compile("^ods_\\w+_(inc|full)$");
    Pattern dwdPattern = Pattern.compile("^dwd_\\w+_\\w+_(inc|full)$");
    Pattern dimPattern = Pattern.compile("^dim_\\w+_(zip|full)$");
    Pattern dwsPattern = Pattern.compile("^dws_\\w+_\\w+_\\w+_(\\d+d|nd|td)$");
    Pattern adsPattern = Pattern.compile("^ads_\\w+");
    Pattern dmPattern = Pattern.compile("^dm_\\w+");


    @Override
    protected void checkProblem(GovernanceAssessDetail governanceAssessDetail, AssessParam assessParam) throws Exception {

        TableMetaInfoExtra tableMetaInfoExtra = assessParam.getTableMetaInfo().getTableMetaInfoExtra();

        if(tableMetaInfoExtra.getDwLevel().equals(MetaConst.DW_LEVEL_UNSET)){
            governanceAssessDetail.setAssessScore(BigDecimal.ZERO);
            governanceAssessDetail.setAssessProblem("未填写数据分层");
            String governanceUrl = assessParam.getGovernanceMetric().getGovernanceUrl();
            governanceUrl= governanceUrl.replace("tableId", assessParam.getTableMetaInfo().getId() + "");
            governanceAssessDetail.setGovernanceUrl(governanceUrl);
            return;
        }else if (tableMetaInfoExtra.getDwLevel().equals(MetaConst.DW_LEVEL_OTHER)  ){
            governanceAssessDetail.setAssessScore(BigDecimal.valueOf(5));
            governanceAssessDetail.setAssessProblem("为纳入分层");
            String governanceUrl = assessParam.getGovernanceMetric().getGovernanceUrl();
            governanceUrl= governanceUrl.replace("tableId", assessParam.getTableMetaInfo().getId() + "");
            governanceAssessDetail.setGovernanceUrl(governanceUrl);
            return;
        }

        Pattern pattern=null;
        if (tableMetaInfoExtra.getDwLevel().equals(MetaConst.DW_LEVEL_ODS)){
            pattern=odsPattern;
        } else if ( tableMetaInfoExtra.getDwLevel().equals(MetaConst.DW_LEVEL_DWD)){
            pattern=dwdPattern;
        }else if ( tableMetaInfoExtra.getDwLevel().equals(MetaConst.DW_LEVEL_DIM )){
            pattern=dimPattern;
        }else if ( tableMetaInfoExtra.getDwLevel().equals(MetaConst.DW_LEVEL_DWS)){
            pattern=dwsPattern;
        }else if ( tableMetaInfoExtra.getDwLevel().equals(MetaConst.DW_LEVEL_ADS)){
            pattern=adsPattern;
        }else if ( tableMetaInfoExtra.getDwLevel().equals(MetaConst.DW_LEVEL_DM)){
            pattern=dmPattern;
        }

        Matcher matcher = pattern.matcher(assessParam.getTableMetaInfo().getTableName());

        if(!matcher.matches()){
            governanceAssessDetail.setAssessScore(BigDecimal.ZERO);
            governanceAssessDetail.setAssessProblem("表名不符合规范");
        }


    }


    public static void main(String[] args) {
        Pattern odsPattern = Pattern.compile("^ods_\\w+_(inc|full)$");
        Pattern emailPattern = Pattern.compile("^\\w+@\\w{1,16}\\.(com|cn|org)$");
        Matcher matcher = odsPattern.matcher("ods_inc");
        System.out.println("matcher = " + matcher.matches());


    }
}
