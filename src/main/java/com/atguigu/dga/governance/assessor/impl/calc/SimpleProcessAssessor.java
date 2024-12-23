package com.atguigu.dga.governance.assessor.impl.calc;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.dga.ds.bean.TDsTaskDefinition;
import com.atguigu.dga.governance.assessor.Assessor;
import com.atguigu.dga.governance.bean.AssessParam;
import com.atguigu.dga.governance.bean.GovernanceAssessDetail;
import com.atguigu.dga.governance.util.SqlParser;
import com.atguigu.dga.meta.bean.TableMetaInfo;
import com.google.common.collect.Sets;
import lombok.Getter;
import org.antlr.runtime.tree.Tree;
import org.apache.arrow.flatbuf.Int;
import org.apache.hadoop.hive.ql.lib.Dispatcher;
import org.apache.hadoop.hive.ql.lib.Node;
import org.apache.hadoop.hive.ql.parse.ASTNode;
import org.apache.hadoop.hive.ql.parse.HiveParser;
import org.apache.hadoop.hive.ql.parse.ParseException;
import org.apache.hadoop.hive.ql.parse.SemanticException;
import org.junit.Test;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;


@Component("SIMPLE_PROCESS")
public class SimpleProcessAssessor extends Assessor {
    @Override
    protected void checkProblem(GovernanceAssessDetail governanceAssessDetail, AssessParam assessParam) throws Exception {
            //1   在遍历过程中收集复杂的语法清单 （join  left join  right join  full join  group by  union  函数)
            //2   以上都没的情况 收集所有 where条件下涉及的字段收集被查询表
            //3  找到被查询表的元数据信息 分区字段  和 sql中过滤字段做对比
        if(assessParam.getTableMetaInfo().getTableMetaInfoExtra().getDwLevel().equals("ODS") ||assessParam.getTDsTaskDefinition()==null){  //ods层没有sql处理
            return;
        }

        TDsTaskDefinition tDsTaskDefinition = assessParam.getTDsTaskDefinition();
        if(tDsTaskDefinition==null){
            System.out.println(tDsTaskDefinition);
        }
        String sql = tDsTaskDefinition.getSql();
        SimpleProcessDispather dispatcher = new SimpleProcessDispather();
        SqlParser.parseSQL(sql,dispatcher);

        Map<String, TableMetaInfo> tableMetaInfoMap = assessParam.getTableMetaInfoMap();// 全库的所有表的元数据
        String schemaName = assessParam.getTableMetaInfo().getSchemaName();

        governanceAssessDetail.setAssessComment("复杂处理："+dispatcher.getProcessSet()+" 过滤字段："+dispatcher.getWhereFieldName()+" 被查询表："+dispatcher.getRefTableName());

        boolean isSimple=false;
        if( dispatcher.getProcessSet().size()>0){
            return;
        }else {
            Set<String> refTableName = dispatcher.getRefTableName();
            for (String tableName : refTableName) {
                TableMetaInfo tableMetaInfo = tableMetaInfoMap.get(schemaName + "." + tableName);
                if(tableMetaInfo==null){
                    System.out.println(1111);
                }
                String partitionColNameJson = tableMetaInfo.getPartitionColNameJson(); //被查询表的分区集合
                List<JSONObject> partitionColObjList = JSON.parseArray(partitionColNameJson, JSONObject.class);

                Set<String> whereFieldNameSet = dispatcher.getWhereFieldName();
                Integer sameCount=0;
                for (String fieldName : whereFieldNameSet) {     //统计被查询字段 有多少个和分区字段相同
                    for (JSONObject partitionObj : partitionColObjList) {
                        String partitionName = partitionObj.getString("name");
                        if(fieldName.equals(partitionName)){
                            sameCount++;
                        }
                    }
                }
                if(sameCount==whereFieldNameSet.size()){ //如果所有字段 都是分区字段
                    isSimple=true;          //视为简单处理
                }
            }

        }

         if(isSimple){
            governanceAssessDetail.setAssessScore(BigDecimal.ZERO);
            governanceAssessDetail.setAssessProblem("是简单处理");
        }

    }


       class SimpleProcessDispather implements Dispatcher{
        //  目标 ：收集  1 复杂语法清单  2 收集所有 where条件下涉及的字段  3 收集被查询表
        @Getter
        Set<String>  processSet=new HashSet<>(); //语法清单 （join  left join  right join  full join  group by  union  函数)

        @Getter
        Set<String>  whereFieldName=new HashSet<>();

        @Getter
        Set<String>  refTableName=new HashSet<>();



        Set<Integer> realProcessSet= Sets.newHashSet(HiveParser.TOK_JOIN,  //join 包含通过where 连接的情况
                HiveParser.TOK_GROUPBY,       //  group by
                HiveParser.TOK_LEFTOUTERJOIN,       //  left join
                HiveParser.TOK_RIGHTOUTERJOIN,     //   right join
                HiveParser.TOK_FULLOUTERJOIN,     // full join
                HiveParser.TOK_FUNCTION,     //count(1)
                HiveParser.TOK_FUNCTIONDI,  //count(distinct xx)
                HiveParser.TOK_FUNCTIONSTAR, // count(*)
                HiveParser.TOK_SELECTDI,  // distinct
                HiveParser.TOK_UNIONALL   // union
        );

        Set<String> operators=Sets.newHashSet("=",">","<",">=","<=" ,"<>"  ,"like"); // in / not in 属于函数计算


        @Override
        public Object dispatch(Node nd, Stack<Node> stack, Object... nodeOutputs) throws SemanticException {
                // 判断节点是否是清单中的处理内容
                ASTNode astNode = (ASTNode) nd;
                if ( realProcessSet.contains(astNode.getType()) ){
                    processSet.add(astNode.getText());
                }
                if(operators.contains(astNode.getText()) ){  //收集字段
                    ArrayList<Node> children = astNode.getChildren();
                    for (Node child : children) {
                        ASTNode  operatorChildNode = (ASTNode) child;
                        if(operatorChildNode.getType()==HiveParser.DOT){   //带表名的字段名
                            ASTNode fieldNode = (ASTNode)operatorChildNode.getChild(1);
                            whereFieldName.add(fieldNode.getText());
                        }else if(operatorChildNode.getType()==HiveParser.TOK_TABLE_OR_COL){  //不带表名的字段名
                            ASTNode fieldNode = (ASTNode)operatorChildNode.getChild(0);
                            whereFieldName.add(fieldNode.getText());
                        }

                    }

                }
                if(astNode.getType()==HiveParser.TOK_TABREF) {  //收集表
                    ASTNode tableNode =(ASTNode) astNode.getChild(0);
                    String tableName=null;
                    if(tableNode.getChildren().size()==1){
                        tableName=  tableNode.getChild(0).getText();  //无库名
                    }else{
                        tableName=  tableNode.getChild(1).getText();//有库名
                    }

                    refTableName.add(tableName);
                }

            return null;
        }
    }

    public static void main(String[] args) throws ParseException, SemanticException {
//        String sql =" select * from  t1 join t2 on t1.id =t2.oid  where  t1.dt='xxxx' and t2.name='xxxx'";
//        SimpleProcessDispather simpleProcessDispather = new SimpleProcessDispather();
//        SqlParser.parseSQL(sql,simpleProcessDispather);
//        System.out.println("simpleProcessDispather = " + simpleProcessDispather.getProcessSet());
    }
}
