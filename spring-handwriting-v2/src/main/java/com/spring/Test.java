package com.spring;

import com.spring.context.ApplicationContext;
import com.xm.HelloWorldController;

public class Test {

    public static void main(String[] args) {


        ApplicationContext applicationContext = new ApplicationContext("classpath:application.properties");

        HelloWorldController helloWorldController = (HelloWorldController) applicationContext.getBean("helloWorldController");
        System.out.println(helloWorldController);
//        helloWorldController.test;
    }
}
