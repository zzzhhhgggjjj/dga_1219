package com.atguigu.dga.meta.bean;

import lombok.Data;

@Data
public class TableMetaInfoQuery {

    String schemaName;
    String tableName;
    String dwLevel;
    Integer pageSize;
    Integer pageNo;
}
