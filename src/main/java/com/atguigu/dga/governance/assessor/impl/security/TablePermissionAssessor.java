package com.atguigu.dga.governance.assessor.impl.security;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.dga.governance.assessor.Assessor;
import com.atguigu.dga.governance.bean.AssessParam;
import com.atguigu.dga.governance.bean.GovernanceAssessDetail;
import com.atguigu.dga.meta.bean.TableMetaInfo;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.FsPermission;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;

@Component("TABLE_PERMISSION")
public class TablePermissionAssessor extends Assessor {

    //检查该表最高权限的目录或者文件，如果超过文件超过{file_permission}或者目录超过{dir_permission}则给0分 其余给10分
    //1 取得表的目录路径
    //2 创建hdfs的客户端  FileSystem
    //3 对目录进行访问  递归  ：
    //   递归逻辑 ：
    //  如果是目录  判断目录权限   然后下探
    //   如果是文件  判断文件权限
    // 如果 遍历过程中出现 超权限情况 直接退出遍历
    //4  根据最终递归返回值 ，来进行打分评判
    @Override
    protected void checkProblem(GovernanceAssessDetail governanceAssessDetail, AssessParam assessParam) throws Exception {
        //1 取得表的目录路径   取得建议权限的参数
        TableMetaInfo tableMetaInfo = assessParam.getTableMetaInfo();
        String tableFsPath = tableMetaInfo.getTableFsPath();
        String tableFsOwner = tableMetaInfo.getTableFsOwner();

        String metricParamsJson = assessParam.getGovernanceMetric().getMetricParamsJson();
        JSONObject paramJsonObj = JSON.parseObject(metricParamsJson);
        String filePermission = paramJsonObj.getString("file_permission");
        String dirPermission = paramJsonObj.getString("dir_permission");

        boolean isTableBeyondPermission = checkIsTableBeyondPermission(tableFsPath, tableFsOwner, filePermission, dirPermission);


        //4  根据最终递归返回值 ，来进行打分评判
        if(isTableBeyondPermission ){
            governanceAssessDetail.setAssessScore(BigDecimal.ZERO);
            governanceAssessDetail.setAssessProblem("表中文件或目录超过建议权限");
        }


    }

    private boolean  checkIsTableBeyondPermission(String tableFsPath,String tableFsOwner ,String filePermission ,String dirPermission ) throws URISyntaxException, IOException, InterruptedException {
        //2 创建hdfs的客户端  FileSystem
        FileSystem fileSystem = FileSystem.get(new URI(tableFsPath), new Configuration(), tableFsOwner);

        FileStatus[] fileStatuses = fileSystem.listStatus( new Path(tableFsPath));
        boolean isPathBeyondPermission = checkIsPathBeyondPermission(fileStatuses, filePermission, dirPermission, fileSystem);
        return isPathBeyondPermission;

    }



    // 递归逻辑 ：
    //               //  如果是目录  判断目录权限   然后下探
    //              //   如果是文件  判断文件权限
    //               // 如果 遍历过程中出现 超权限情况 直接退出遍历
    private boolean checkIsPathBeyondPermission(FileStatus[] fileStatuses ,String filePermission ,String dirPermission,FileSystem fileSystem) throws IOException {


        for (FileStatus fileStatus : fileStatuses) {
            if(fileStatus.isDirectory()){

                FsPermission permission = fileStatus.getPermission();
                System.out.println(fileStatus.getPath()+":  permission = " + permission+" dirPermission:"+dirPermission);
                if(fileStatus.getPath().getName().indexOf("/ods_activity_rule_full/dt=2023-04-28")>0){
                    System.out.println(111);
                }
                boolean isBeyondPermission = checkIsBeyondPermission(permission, dirPermission);
                if(isBeyondPermission){
                    return true;
                }
                FileStatus[] subFileStatus = fileSystem.listStatus(fileStatus.getPath()); //下级目录的展开
                boolean subIsBeyondPermission = checkIsPathBeyondPermission(subFileStatus, filePermission, dirPermission, fileSystem);// 下探
                if(subIsBeyondPermission){
                    return  true;
                }
            }else {
                FsPermission permission = fileStatus.getPermission();
                boolean isBeyondPermission = checkIsBeyondPermission(permission, filePermission);
                if(isBeyondPermission){
                    return true;
                }
            }

        }
        return false;
    }


    private boolean  checkIsBeyondPermission(FsPermission permission ,String suggestPermission  ){

        char[] suggestPermissionChars = suggestPermission.toCharArray();  //754 644

            Integer sugUserPerInt = Integer.valueOf(suggestPermissionChars[0]+""  );
            Integer sugGroupPerInt = Integer.valueOf(suggestPermissionChars[1]+"");
            Integer sugOtherPerInt = Integer.valueOf(suggestPermissionChars[2]+"");
            if(permission.getUserAction().ordinal()  >sugUserPerInt){
                return  true;
            }
            if(permission.getGroupAction().ordinal()==7){
                System.out.println(111);
            }
            if(permission.getGroupAction().ordinal()  >sugGroupPerInt){
                return  true;
            }
            if(permission.getOtherAction().ordinal()  >sugOtherPerInt){
                return  true;
            }
        return  false;

    }

}
