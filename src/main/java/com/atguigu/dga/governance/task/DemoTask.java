package com.atguigu.dga.governance.task;


import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;


public class DemoTask {

   // @Scheduled(cron = "0 0 3 * * *")
    public void testSchedule(){
        System.out.println("11111");
    }


    @Scheduled(fixedDelay = 5000)
    public void text2(){
        System.out.println("text2 测试"+ DateFormatUtils.format(new Date(),"mm:ss"));
    }
    @Scheduled(fixedRate = 5000)
    public void text3(){
        System.out.println("text3 测试"+DateFormatUtils.format(new Date(),"mm:ss"));
    }
    @Scheduled(initialDelay =3000,  fixedRate = 5000)
    public void text4(){
        System.out.println("text4 测试"+DateFormatUtils.format(new Date(),"mm:ss"));
    }

}
