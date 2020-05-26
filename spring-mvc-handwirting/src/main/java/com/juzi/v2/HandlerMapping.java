package com.juzi.v2;

import com.juzi.v1.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 *  建立url与controller的之间的联系
 */
public class HandlerMapping {


    private String url;     // url
    private Object controller;      // controller对象
    private Method method;      // url对应的方法
    private Map<String, Integer> paramIndexMapping;  // 建立方法参数与位置的映射
    private Class[] typeClasses;        // 参数列表

    public HandlerMapping(String url, Method method, Object controller) {
        this.url = url;
        this.method = method;
        this.controller = controller;
        this.paramIndexMapping = new HashMap<>();
        this.paramIndexMapping();
    }

    private void paramIndexMapping() {
        Class<?>[] parameterTypes = this.method.getParameterTypes();
        this.typeClasses = new Class[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            Class parameterType = parameterTypes[i];
            if (parameterType == HttpServletRequest.class) {
                paramIndexMapping.put(HttpServletRequest.class.getName(), i);
                typeClasses[i] = parameterType;
                continue;
            } else if (parameterType == HttpServletResponse.class) {
                paramIndexMapping.put(HttpServletResponse.class.getName(), i);
                typeClasses[i] = parameterType;
                continue;
            }

            // 获取方法参数上所有的注解
            Annotation[][] parameterAnnotations = method.getParameterAnnotations();
            if (parameterAnnotations == null || parameterAnnotations.length == 0) continue;
            for (int j = 0; j < parameterAnnotations.length; j++) {
                for (Annotation annotation : parameterAnnotations[j]) {
                    // 判断类型是否是RequestParam
                    if (annotation instanceof RequestParam) {
                        RequestParam requestParam = (RequestParam) annotation;
                        String paramKey = requestParam.value();
                        typeClasses[i] = parameterType; //  将
                        paramIndexMapping.put(paramKey, i);  // 将RequestParam中的value属性值作为key,参数位置作为value
                    }
                }
            }
        }

    }

    public String getUrl() {
        return url;
    }

    public Object getController() {
        return controller;
    }

    public Method getMethod() {
        return method;
    }

    public Map<String, Integer> getParamIndexMapping() {
        return paramIndexMapping;
    }

    public Class[] getTypeClasses() {
        return typeClasses;
    }
}
