package com.spring.beans;

/**
 *  bean工厂,管理bean的顶层设计接口
 */
public interface BeanFactory {


    Object getBean(String beanName);

    Object getBean(Class<?> beanClass);
}
