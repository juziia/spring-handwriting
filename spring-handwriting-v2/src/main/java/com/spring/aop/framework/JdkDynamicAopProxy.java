package com.spring.aop.framework;

import com.spring.aop.AopProxy;
import com.spring.aop.support.AdviceSupport;
import com.spring.aop.support.ReflectMethodInvocation;
import jdk.internal.org.objectweb.asm.commons.AdviceAdapter;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

public class JdkDynamicAopProxy implements AopProxy, InvocationHandler {

    private AdviceSupport adviced;

    public JdkDynamicAopProxy(AdviceSupport adviced) {
        this.adviced = adviced;
    }

    @Override
    public Object getProxy() {
        return getProxy(this.adviced.getTargetClass().getClassLoader());
    }

    @Override
    public Object getProxy(ClassLoader classLoader) {

        return Proxy.newProxyInstance(classLoader,this.adviced.getTargetClass().getInterfaces(),this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        List<Object> chain = adviced.getInterceptorsAndDynamicInterceptionAdvice(method, this.adviced.getTargetClass());

        //Object proxy,  Object target, Method method,  Object[] arguments,
        //             Class<?> targetClass, List<Object> interceptorsAndDynamicMethodMatchers)
        ReflectMethodInvocation invocation = new ReflectMethodInvocation(proxy, null, method, args, this.adviced.getTargetClass(), chain);

        return invocation.proceed();
    }
}
