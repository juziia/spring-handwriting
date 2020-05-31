package com.spring.aop.framework;

import com.spring.aop.support.ReflectMethodInvocation;

public interface MethodInterceptor {


    Object invoke(ReflectMethodInvocation invocation) throws Throwable;

}
