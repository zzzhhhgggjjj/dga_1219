package com.atguigu.dga.governance.util;

import org.apache.hadoop.hive.ql.lib.DefaultGraphWalker;
import org.apache.hadoop.hive.ql.lib.Dispatcher;
import org.apache.hadoop.hive.ql.lib.GraphWalker;
import org.apache.hadoop.hive.ql.lib.Node;
import org.apache.hadoop.hive.ql.parse.*;

import java.util.Collections;
import java.util.Stack;

public class SqlParser {



    public static final void  parseSQL(String sql , Dispatcher dispatcher) throws ParseException, SemanticException {

        //1  创建解析驱动
        ParseDriver parseDriver = new ParseDriver();
        //2 用解析驱动解析sql
        ASTNode astNode = parseDriver.parse(sql);
        //3  从无意的头节点 移动到query
        while(astNode.getToken()==null||astNode.getType()!= HiveParser.TOK_QUERY){
            astNode=(ASTNode)  astNode.getChild(0);
        }

        // 声明一个遍历器
        GraphWalker graphWalker= new DefaultGraphWalker(dispatcher);
        // 让遍历器开始遍历
        graphWalker.startWalking(Collections.singletonList(astNode),null);

    }

    public static void main(String[] args) throws ParseException, SemanticException {
        String sql= "select a,b,c from order_info where id='101'";
        TestDispatcher testDispatcher = new TestDispatcher();

        SqlParser.parseSQL(sql,testDispatcher);

    }

    static class TestDispatcher implements Dispatcher{


        @Override
        public Object dispatch(Node nd, Stack<Node> stack, Object... nodeOutputs) throws SemanticException {
            ASTNode astNode = (ASTNode) nd;

            System.out.println("nd.getName() = " + astNode.getToken().getText());
            return null;
        }
    }

}
