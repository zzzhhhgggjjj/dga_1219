package com.atguigu.dga.meta.mapper;

import com.atguigu.dga.meta.bean.TableMetaInfo;
import com.atguigu.dga.meta.bean.TableMetaInfoVO;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 元数据表 Mapper 接口
 * </p>
 *
 * @author zhangchen
 * @since 2023-05-06
 */
@Mapper
@DS("dga")
public interface TableMetaInfoMapper extends BaseMapper<TableMetaInfo> {

    @Select("${SQL}")
    public List<TableMetaInfoVO> getTableMetaList(@Param("SQL") String sql );

    @Select("${SQL}")
    public Integer getTableMetaCount(@Param("SQL") String sql );



    @Select(" select tm.id as tm_id ," +
            "tm.schema_name as tm_schema_name, " +
            "tm.table_name as tm_table_name," +
            "tm.create_time as tm_create_time," +
            "tm.update_time as tm_update_time," +
            " te.id as te_id ," +
            "te.schema_name as te_schema_name ," +
            "te.table_name as te_table_name," +
            "te.create_time as te_create_time," +
            "te.update_time as te_update_time," +
            "    tm.* ,te.* from  table_meta_info tm join table_meta_info_extra te" +
            " on tm.table_name=te.table_name and tm.schema_name=te.schema_name " +
            " where assess_date= (select max(assess_date)  from table_meta_info  )"
             )
    @ResultMap("meta_map")
    public List<TableMetaInfo> selectTableMetaInfoAllList( );
}
