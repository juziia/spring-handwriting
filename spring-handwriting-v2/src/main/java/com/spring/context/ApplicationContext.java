package com.spring.context;

import com.spring.annotation.Autowired;
import com.spring.annotation.Controller;
import com.spring.annotation.Service;
import com.spring.beans.BeanFactory;
import com.spring.beans.config.BeanDefinition;
import com.spring.beans.support.BeanDefinitionReader;
import com.spring.beans.support.BeanWrapper;
import com.spring.beans.support.DefaultListableBeanFactory;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class ApplicationContext extends DefaultListableBeanFactory implements BeanFactory {

    private String[] configLocations;

    private BeanDefinitionReader reader;

    public ApplicationContext(String... configLocations) {
        this.configLocations = configLocations;
        // 初始化容器
        refresh();
    }

    private void refresh() {
        // 定位配置文件
        reader = new BeanDefinitionReader(configLocations);
        // 加载配置文件,获取bean的定义信息
        List<BeanDefinition> beanDefinitions = reader.loadBeanDefinitions();
        // 注册所有bean的定义信息
        registerBeanDefinitions(beanDefinitions);

        // 自动注入所有的bean
        autowired();

    }

    private void autowired() {
        Set<Map.Entry<String, BeanDefinition>> entries = beanDefinitionMap.entrySet();

        for (Map.Entry<String, BeanDefinition> definitionEntry : entries) {
            if (!definitionEntry.getValue().isLazyInit()) {
                // 不是延迟加载则进行注入
                this.getBean(definitionEntry.getKey());

            }
        }
    }

    private void registerBeanDefinitions(List<BeanDefinition> beanDefinitions) {
        if (beanDefinitions == null || beanDefinitions.isEmpty()) return;

        for (BeanDefinition beanDefinition : beanDefinitions) {
            // 将bean的定义信息注册到伪ioc容器中
            super.beanDefinitionMap.put(beanDefinition.getFactoryBeanName(), beanDefinition);
        }

    }

    @Override
    public Object getBean(String beanName) {

        // 初始化bean,以防止循环依赖
        Object instance = initializationBean(beanName, beanDefinitionMap.get(beanName));

        // di  向bean中的依赖对象进行注入
        populateBean(beanName, new BeanWrapper(instance));

        return instance;
    }

    private void populateBean(String beanName, BeanWrapper beanWrapper) {
        Class<?> beanClass = beanWrapper.getWrappedClass();
        Field[] fields = beanClass.getDeclaredFields();

        if (fields != null && fields.length != 0) {
            for (Field field : fields) {
                if(field.isAnnotationPresent(Autowired.class)){
                    String beanFieldName = field.getAnnotation(Autowired.class).value();
                    if ("".equals(beanFieldName)){
                        beanFieldName = field.getType().getName();
                    }
                    //
                    if(!factoryBeanInstanceCache.containsKey(beanFieldName)) continue;

                    field.setAccessible(true);
                    try {
                        field.set(beanWrapper.getWrappedInstance(),this.factoryBeanInstanceCache.get(beanFieldName).getWrappedInstance());
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }

                }
            }
        }

        if(! factoryBeanInstanceCache.containsKey(beanName)){
            factoryBeanInstanceCache.put(beanName,beanWrapper);
            factoryBeanInstanceCache.put(beanDefinitionMap.get(beanName).getClassName(),beanWrapper);
        }


    }

    private Object initializationBean(String beanName, BeanDefinition beanDefinition) {

        Object instance = null;

        // 如果 是多例那么将每次重新创建对象
        if (singletonObjects.containsKey(beanName) && beanDefinitionMap.get(beanName).isSingleton()) {
            instance = singletonObjects.get(beanName);
        } else {
            try {
                Class<?> beanClazz = Class.forName(beanDefinition.getClassName());
                // 只有被Service,Controller注解的类才被初始化
                if (beanClazz.isAnnotationPresent(Service.class) || beanClazz.isAnnotationPresent(Controller.class)) {

                    instance = beanClazz.newInstance();
                    singletonObjects.put(beanDefinition.getFactoryBeanName(), instance);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }


        }


        return instance;
    }


    @Override
    public Object getBean(Class<?> beanClass) {
        return null;
    }


    public Properties getConfig(){
        return reader.getConfig();
    }
}
