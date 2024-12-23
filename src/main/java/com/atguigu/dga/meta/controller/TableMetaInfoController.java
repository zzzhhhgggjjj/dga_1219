package com.atguigu.dga.meta.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.dga.meta.bean.TableMetaInfo;
import com.atguigu.dga.meta.bean.TableMetaInfoExtra;
import com.atguigu.dga.meta.bean.TableMetaInfoQuery;
import com.atguigu.dga.meta.bean.TableMetaInfoVO;
import com.atguigu.dga.meta.service.TableMetaInfoExtraService;
import com.atguigu.dga.meta.service.TableMetaInfoService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * <p>
 * 元数据表 前端控制器
 * </p>
 *
 * @author zhangchen
 * @since 2023-05-06
 */
@RestController
@RequestMapping("/tableMetaInfo")
public class TableMetaInfoController {

    @Autowired
    TableMetaInfoService tableMetaInfoService;

    @Autowired
    TableMetaInfoExtraService tableMetaInfoExtraService;

    @PostMapping("/init-tables/{schemaName}/{assessDate}")
    public String initTableMeta(@PathVariable("schemaName")String schemaName,@PathVariable("assessDate") String assessDate ){
        tableMetaInfoService.initTableMeta(assessDate,schemaName);
        return "success";
    }

    @GetMapping("/table-list")
    @CrossOrigin
    public String getTableList(TableMetaInfoQuery tableMateInfoQuery ){
        List<TableMetaInfoVO> tableMetaInfoList = tableMetaInfoService.getTableMetaInfoList(tableMateInfoQuery);
        Integer  total=   tableMetaInfoService.getTableMetaInfoCount(tableMateInfoQuery);

        JSONObject pageObj =new JSONObject();
        pageObj.put("total",total);
        pageObj.put("list",tableMetaInfoList);

        return  pageObj.toJSONString();
    }

    @GetMapping("/table/{tableId}")
    @CrossOrigin
    public String getTableMetaInfo( @PathVariable("tableId") String tableId){
        TableMetaInfo tableMetaInfo = tableMetaInfoService.getById(tableId);

        TableMetaInfoExtra tableMetaInfoExtra = tableMetaInfoExtraService.getOne(new QueryWrapper<TableMetaInfoExtra>()
                .eq("schema_name", tableMetaInfo.getSchemaName())
                .eq("table_name", tableMetaInfo.getTableName()));

        tableMetaInfo.setTableMetaInfoExtra(tableMetaInfoExtra);

        return JSON.toJSONString(tableMetaInfo);

    }


    @PostMapping("/tableExtra")
    @CrossOrigin
    public String saveTableExtra(@RequestBody TableMetaInfoExtra tableMetaInfoExtra){
        tableMetaInfoExtra.setUpdateTime(new Date());
        tableMetaInfoExtraService.updateById(tableMetaInfoExtra);
        return "success";

    }

}
