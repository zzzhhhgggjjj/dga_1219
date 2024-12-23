package com.atguigu.dga;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
///@ComponentScan(basePackages = "com.atguigu")
@EnableScheduling
public class Dga1219Application {

    public static void main(String[] args) {
        SpringApplication.run(Dga1219Application.class, args);
    }

}
