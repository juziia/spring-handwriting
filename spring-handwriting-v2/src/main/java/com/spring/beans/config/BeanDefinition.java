package com.spring.beans.config;


/**
 *  描述bean的定义信息
 */

public class BeanDefinition {

    private String factoryBeanName; // 存储在容器中beand的名称

    private String className;   // bean的全限定类名

    private boolean isLazyInit = false; // 是否延迟加载  true 延迟加载  false 立即加载

    private boolean isSingleton = true;

    public String getFactoryBeanName() {
        return factoryBeanName;
    }

    public void setFactoryBeanName(String factoryBeanName) {
        this.factoryBeanName = factoryBeanName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public boolean isLazyInit() {
        return isLazyInit;
    }

    public void setLazyInit(boolean lazyInit) {
        isLazyInit = lazyInit;
    }

    public boolean isSingleton() {
        return isSingleton;
    }

    public void setSingleton(boolean singleton) {
        isSingleton = singleton;
    }
}
