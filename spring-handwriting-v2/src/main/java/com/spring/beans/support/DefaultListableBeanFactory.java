package com.spring.beans.support;

import com.spring.beans.config.BeanDefinition;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultListableBeanFactory {

    // 存储bean的定义信息
    protected Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>(16);

    // 缓存单例对象
    protected Map<String,Object> singletonObjects = new ConcurrentHashMap<>(16);

    // ioc容器
    protected Map<String,BeanWrapper> factoryBeanInstanceCache = new ConcurrentHashMap<>(16);



    public int getBeanDefinitionCount() {

        return this.beanDefinitionMap.size();
    }

    public String[] getBeanDefinitionNames() {
        return beanDefinitionMap.keySet().toArray(new String[getBeanDefinitionCount()]);
    }
}
