package com.atguigu.dga.governance.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class SpringBeanProvider  implements ApplicationContextAware {  //ApplicationContextAware 实现人力资源部

    public static  ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context=applicationContext; //组件容器  员工池
    }


    public static   <T>    T getBean(String name,Class<T> tClass){
        return context.getBean(name,tClass);  //根据名称和类名 提取组件
    }
}
