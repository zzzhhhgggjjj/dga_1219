package com.atguigu.dga.meta.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.support.spring.PropertyPreFilters;
import com.atguigu.dga.meta.bean.TableMetaInfo;
import com.atguigu.dga.meta.bean.TableMetaInfoExtra;
import com.atguigu.dga.meta.bean.TableMetaInfoQuery;
import com.atguigu.dga.meta.bean.TableMetaInfoVO;
import com.atguigu.dga.meta.mapper.TableMetaInfoMapper;
import com.atguigu.dga.meta.service.TableMetaInfoExtraService;
import com.atguigu.dga.meta.service.TableMetaInfoService;
import com.atguigu.dga.meta.util.SQLUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.metastore.IMetaStoreClient;
import org.apache.hadoop.hive.metastore.RetryingMetaStoreClient;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 元数据表 服务实现类
 * </p>
 *
 * @author zhangchen
 * @since 2023-05-06
 */
@Service
@DS("dga")
public class TableMetaInfoServiceImpl extends ServiceImpl<TableMetaInfoMapper, TableMetaInfo> implements TableMetaInfoService {

    IMetaStoreClient hiveClient = getHiveClient();

    @Autowired
    TableMetaInfoExtraService tableMetaInfoExtraService;

    @Override
    public void initTableMeta(String assessDate, String schemaName) {
       // 0 提取元数据前把当前考评日的元数据进行清理
        remove(new QueryWrapper<TableMetaInfo>().eq("assess_date",assessDate));

        // 1   hive 元数据   // 2   hdfs 元数据
        List<TableMetaInfo> tableMetaInfoList = extractMeta(assessDate, schemaName);


        // 3   保存 元数据
        saveOrUpdateBatch(tableMetaInfoList,500);

        // 4  补充辅助信息
        tableMetaInfoExtraService.genTableMetaInfoExtra(tableMetaInfoList);


    }



    // 1   hive 元数据
    // 获得一个列表 其中每个元素都是一张表的元数据信息
    private    List<TableMetaInfo> extractMeta(String assessDate, String schemaName){
        //1 从数据库中取所有表的清单 show tables
        List<TableMetaInfo> tableMetaInfoList ;
        try {
            List<String> tableNameList = hiveClient.getAllTables(schemaName);
            tableMetaInfoList=new ArrayList<>(tableNameList.size());
            for (String tableName : tableNameList) {
                TableMetaInfo tableMetaInfo=   getTableMeta(schemaName,tableName); //根据库名 表名 获得某个表的元数据
                addHdfsInfo(tableMetaInfo); //继续从hdfs中提取数据
                // 考评时间
                tableMetaInfo.setAssessDate(assessDate);
                //
                tableMetaInfo.setCreateTime(new Date());
                tableMetaInfoList.add(tableMetaInfo);
            }


        } catch (TException e) {
            throw new RuntimeException(e);
        }

        return  tableMetaInfoList;
    }

    //根据库名 表名 获得某个表的元数据
    private  TableMetaInfo  getTableMeta(String schemaName, String tableName){
        TableMetaInfo tableMetaInfo = new TableMetaInfo();
        try {
            Table table = hiveClient.getTable(schemaName, tableName);
            // 把table中的元数据 提取到 tableMetaInfo
            System.out.println(table);
            tableMetaInfo.setTableName(tableName);
            tableMetaInfo.setSchemaName(schemaName);
            // 过滤掉不需要的
            PropertyPreFilters.MySimplePropertyPreFilter mySimplePropertyPreFilter = new PropertyPreFilters().addFilter("comment", "name", "type");
            tableMetaInfo.setColNameJson(JSON.toJSONString(table.getSd().getCols(),mySimplePropertyPreFilter) );  // 字段名 ，字段类型，备注
            //分区
            tableMetaInfo.setPartitionColNameJson(JSON.toJSONString(table.getPartitionKeys(),mySimplePropertyPreFilter));
            //owner
            tableMetaInfo.setTableFsOwner(table.getOwner());
            //
            tableMetaInfo.setTableParametersJson(JSON.toJSONString(table.getParameters()));

            tableMetaInfo.setTableComment(table.getParameters().get("comment"));

            tableMetaInfo.setTableFsPath(table.getSd().getLocation());

            tableMetaInfo.setTableInputFormat(table.getSd().getInputFormat());
            tableMetaInfo.setTableOutputFormat(table.getSd().getOutputFormat());
            tableMetaInfo.setTableRowFormatSerde(table.getSd().getSerdeInfo().getSerializationLib());


            String tableCreateDate = DateFormatUtils.format(new Date(table.getCreateTime() * 1000L), "yyyy-MM-dd HH:mm:ss");
            // Date date = DateUtils.parseDate(tableCreateDate, "yyyy-MM-dd HH:mm:ss");

            tableMetaInfo.setTableCreateTime(  tableCreateDate );

            tableMetaInfo.setTableType(table.getTableType());

            if(table.getSd().getBucketCols().size()>0){
                tableMetaInfo.setTableBucketColsJson(JSON.toJSONString(table.getSd().getBucketCols()));
                tableMetaInfo.setTableBucketNum(table.getSd().getNumBuckets()+0L);
                tableMetaInfo.setTableSortColsJson(JSON.toJSONString(table.getSd().getSortCols()));
            }

        } catch (TException e) {
            throw new RuntimeException(e);
        }
        return tableMetaInfo;
    }


    // 初始化 hive 客户端
    private IMetaStoreClient getHiveClient() {
        // 把本地的hive文件加载为hiveConf对象
        HiveConf hiveConf = new HiveConf();
         hiveConf.addResource(Thread.currentThread().getContextClassLoader().
                 getResourceAsStream("hive-site.xml"));

//        hiveConf.addResource("hive-site.xml");
//        hiveConf.addResource(new URL("file:///home/atguigu/dga/hive-site.xml"));
        IMetaStoreClient client = null;
        try {  //创建客户端
            client = RetryingMetaStoreClient.getProxy(hiveConf, true);
        } catch (Exception e) {
            throw new RuntimeException(e);

        }
        return client;
    }


    public void addHdfsInfo(TableMetaInfo tableMetaInfo){
        try {
             FileSystem fileSystem = FileSystem.get(new URI(tableMetaInfo.getTableFsPath()), new Configuration(), tableMetaInfo.getTableFsOwner());
             FileStatus[] fileStatuses = fileSystem.listStatus(new Path(tableMetaInfo.getTableFsPath())); //listStatus获取某个目录下的文件或文件夹集合
             addFileInfo(fileStatuses,tableMetaInfo,fileSystem); //进行递归 遍历

            tableMetaInfo.setFsCapcitySize( fileSystem.getStatus().getCapacity() );
            tableMetaInfo.setFsRemainSize( fileSystem.getStatus().getRemaining() );
            tableMetaInfo.setFsUsedSize( fileSystem.getStatus().getUsed() );



        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }


    // 作业  ： tableMetaInfo 最近访问时间  ， 最近修改时间, 总副本大小
    // 递归处理 ， 每个递归方法 一定是一个 叶子节点的收敛处理  和  分支节点的继续下探 （ java  下探就增加方法栈的深度， 默认1w个左右 如果过深会造成栈溢出）
    public  void  addFileInfo(FileStatus[] fileStatuses , TableMetaInfo  tableMetaInfo,FileSystem fileSystem) throws IOException {
            //遍历所有 filestatus
        for (FileStatus fileStatus : fileStatuses) {
              if(! fileStatus.isDirectory()){     // 文件 取大小  累加tableSize
                  long accessTime = fileStatus.getAccessTime();
                  long modificationTime = fileStatus.getModificationTime();
                  short replication = fileStatus.getReplication();
                  long filesize = fileStatus.getLen();//文件字节数
                  tableMetaInfo.setTableSize(tableMetaInfo.getTableSize()+filesize);  //累加到表的总大小
                  tableMetaInfo.setTableTotalSize(tableMetaInfo.getTableTotalSize()+ filesize*replication); //总副本大小

                  //取最近的修改时间
                  if(tableMetaInfo.getTableLastModifyTime()==null){
                      tableMetaInfo.setTableLastModifyTime( new Date(modificationTime));
                  } else if (tableMetaInfo.getTableLastModifyTime().getTime()<modificationTime) {
                      tableMetaInfo.setTableLastModifyTime(new Date(modificationTime));
                  }

                  //取最近的访问时间
                  if(tableMetaInfo.getTableLastAccessTime()==null){
                      tableMetaInfo.setTableLastAccessTime( new Date(accessTime));
                  } else if (tableMetaInfo.getTableLastAccessTime().getTime()<accessTime) {
                      tableMetaInfo.setTableLastAccessTime(new Date(accessTime));
                  }
              } else{//  文件夹  获得文件夹下的Filestatus 进行递归
                  FileStatus[] subFileStatus = fileSystem.listStatus(fileStatus.getPath());
                  addFileInfo(subFileStatus,tableMetaInfo,fileSystem);
              }
        }



    }






    @Override
    public List<TableMetaInfoVO> getTableMetaInfoList(TableMetaInfoQuery tableMetaInfoQuery) {
        StringBuilder  sqlBuilder = new StringBuilder();
        sqlBuilder.append("select  tm.id ,tm.table_name,tm.schema_name," +
                "table_comment,table_size,table_total_size," +
                "tec_owner_user_name,busi_owner_user_name," +
                " table_last_access_time,table_last_modify_time "+
                 "from   table_meta_info tm  join table_meta_info_extra te  " +
                        "on  tm.table_name=te.table_name and  tm.schema_name= te.schema_name" );

        sqlBuilder.append(" where  assess_date = (select max(assess_date) from table_meta_info  ) ");
        if(tableMetaInfoQuery.getTableName()!=null&&tableMetaInfoQuery.getTableName().length()>0 ){
            sqlBuilder.append(" and  tm.table_name like '%" + SQLUtil.filterUnsafeSql( tableMetaInfoQuery.getTableName())+"%'");
        }
        if(tableMetaInfoQuery.getSchemaName()!=null&&tableMetaInfoQuery.getSchemaName().length()>0 ){
            sqlBuilder.append(" and  tm.schema_name  like '%" +SQLUtil.filterUnsafeSql( tableMetaInfoQuery.getSchemaName())+"%'");
        }
        if(tableMetaInfoQuery.getDwLevel()!=null&&tableMetaInfoQuery.getDwLevel().length()>0 ){
            sqlBuilder.append(" and  te.dw_level  like '%" +SQLUtil.filterUnsafeSql( tableMetaInfoQuery.getDwLevel())+"%'");
        }
        Integer pageNo = tableMetaInfoQuery.getPageNo();
        Integer pageSize = tableMetaInfoQuery.getPageSize();
        //    limit  20,20    //  根据页码和 每页大小 计算其实行号  行号从0
         Integer from= (pageNo-1) *pageSize;

        sqlBuilder.append(" limit "+from +","+pageSize);

        List tableMetaVOList = baseMapper.getTableMetaList(sqlBuilder.toString());

        //不推荐
//        List<TableMetaInfo> list = this.list();
//        for (TableMetaInfo tableMetaInfo : list) {
//            TableMetaInfoExtra tableMetaInfoExtraServiceOne = tableMetaInfoExtraService.getOne();
//            tableMetaInfo.set
//        }

        return tableMetaVOList;
    }


    @Override
    public Integer getTableMetaInfoCount(TableMetaInfoQuery tableMetaInfoQuery) {
        StringBuilder  sqlBuilder = new StringBuilder();
        sqlBuilder.append("select  count(*)"+
                "from   table_meta_info tm  join table_meta_info_extra te  " +
                "on  tm.table_name=te.table_name and  tm.schema_name= te.schema_name" );

        sqlBuilder.append(" where  assess_date = (select max(assess_date) from table_meta_info  ) ");
        if(tableMetaInfoQuery.getTableName()!=null&&tableMetaInfoQuery.getTableName().length()>0 ){
            sqlBuilder.append(" and  tm.table_name like '%" + SQLUtil.filterUnsafeSql( tableMetaInfoQuery.getTableName())+"%'");
        }
        if(tableMetaInfoQuery.getSchemaName()!=null&&tableMetaInfoQuery.getSchemaName().length()>0 ){
            sqlBuilder.append(" and  tm.schema_name  like '%" +SQLUtil.filterUnsafeSql( tableMetaInfoQuery.getSchemaName())+"%'");
        }
        if(tableMetaInfoQuery.getDwLevel()!=null&&tableMetaInfoQuery.getDwLevel().length()>0 ){
            sqlBuilder.append(" and  te.dw_level  like '%" +SQLUtil.filterUnsafeSql( tableMetaInfoQuery.getDwLevel())+"%'");
        }


        Integer tableMetaCount = baseMapper.getTableMetaCount(sqlBuilder.toString());

        //不推荐
//        List<TableMetaInfo> list = this.list();
//        for (TableMetaInfo tableMetaInfo : list) {
//            TableMetaInfoExtra tableMetaInfoExtraServiceOne = tableMetaInfoExtraService.getOne();
//            tableMetaInfo.set
//        }

        return tableMetaCount;
    }



    public List<TableMetaInfo> getTableMetaInfoAllList(){
        List<TableMetaInfo> tableMetaInfoList = baseMapper.selectTableMetaInfoAllList();
        return tableMetaInfoList;
    }

}
