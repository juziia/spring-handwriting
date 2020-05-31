package com.spring.aop;

public interface AopProxy {

    Object getProxy();


    Object getProxy(ClassLoader classLoader);

}
