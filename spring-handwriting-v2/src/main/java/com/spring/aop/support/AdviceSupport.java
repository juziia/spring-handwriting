package com.spring.aop.support;

import java.lang.reflect.Method;
import java.util.List;

public class AdviceSupport {

    private Class<?> targetClass;

    public AdviceSupport(Class targetClass){
        this.targetClass = targetClass;
    }

    public Class getTargetClass(){
        return targetClass;
    }


    public List<Object> getInterceptorsAndDynamicInterceptionAdvice(Method method,  Class<?> targetClass) {

        return null;
    }

}
