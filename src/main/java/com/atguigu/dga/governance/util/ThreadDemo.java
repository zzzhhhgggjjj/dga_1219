package com.atguigu.dga.governance.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ThreadDemo {


    static ThreadPoolExecutor threadPoolExecutor= new ThreadPoolExecutor(10,20,60, TimeUnit.SECONDS,new LinkedBlockingDeque<>());

    public static void main(String[] args) {
        Integer[] nums= {1,2,3,4,5,6};
        //取6个数的平方  然后加总    //假设每次取平方是复杂运算 每次需要计算5秒
        long startTs = System.currentTimeMillis();
        List<CompletableFuture<Integer>> completableFutureList=new ArrayList<>();
        for (Integer num : nums) {
            CompletableFuture<Integer> completableFuture = CompletableFuture.supplyAsync(() -> {
                //计算逻辑 并且有返回值  此处逻辑 开始 /////// //全部为异步执行，不会阻塞其他程序 包括外层循环
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return num * num;
                /// //////////////////  异步程序结束
            }, threadPoolExecutor);

            completableFutureList.add(completableFuture);

        }

        List<Integer> integerList = completableFutureList.stream().map(future -> future.join()).collect(Collectors.toList());

        Integer total=0;
        for (Integer num : integerList) {
            total+=num;
        }
        System.out.println(total);
        System.out.println("System.currentTimeMillis()-startTs = " + (System.currentTimeMillis() - startTs));


    }
}
