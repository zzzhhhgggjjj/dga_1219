package com.atguigu.dga.meta.bean;

import lombok.Data;

import java.util.Date;

@Data
public class TableMetaInfoVO {  //view object


        /**
         * 表id
         */
        private Long id;

        /**
         * 表名
         */
        private String tableName;

        /**
         * 库名
         */
        private String schemaName;

        /**
         * 数据量大小 ( 来源:hdfs)
         */
        private Long tableSize=0L;

        /**
         * 所有副本数据总量大小  ( 来源:hdfs)
         */
        private Long tableTotalSize=0L;

        /**
         * 表备注 ( 来源:hive)
         */
        private String tableComment;

        /**
         * 技术负责人   ( 来源: 附加)
         */
        private String tecOwnerUserName;

        /**
         * 业务负责人 ( 来源: 附加)
         */
        private String busiOwnerUserName;

        /**
         * 最后修改时间   ( 来源:hdfs)
         */
        private Date tableLastModifyTime;


        /**
         * 最后访问时间
         */
        private Date tableLastAccessTime;


}
