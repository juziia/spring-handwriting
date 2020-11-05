package com.spring.beans.support;

public class BeanWrapper {

    private Object instance;

    public BeanWrapper(Object instance) {
        this.instance = instance;
    }

    /**
     *  被包装的bean实例
     * @return
     */
    public Object getWrappedInstance(){
        return instance;
    }

    /**
     *  被包装的bean的class对象
     * @return
     */
    public Class<?> getWrappedClass(){

        return this.instance.getClass();
    }

}
