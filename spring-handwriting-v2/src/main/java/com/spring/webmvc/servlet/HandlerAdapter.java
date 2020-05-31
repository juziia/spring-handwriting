package com.spring.webmvc.servlet;


import com.spring.annotation.RequestParam;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class HandlerAdapter {

    public boolean support(Object handler){return handler instanceof HandlerMapping; }

    public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception{
        //  如果是HandlerMapping
        if(support(handler)) {
            HandlerMapping handlerMapping = (HandlerMapping) handler;
            Method method = handlerMapping.getMethod();

            Map<String,Integer> paramIndexMapping = new HashMap<>();
            Class<?>[] parameterTypes = method.getParameterTypes();

            Object[] args = new Object[parameterTypes.length];

            // 参数类型与参数的顺序映射
            for (int i = 0; i < parameterTypes.length; i++) {
                if(parameterTypes[i] == HttpServletRequest.class){
                    paramIndexMapping.put(HttpServletRequest.class.getName(),i);
                }else if(parameterTypes[i] == HttpServletResponse.class){
                    paramIndexMapping.put(HttpServletResponse.class.getName(),i);
                }
            }

            // 获取参数上的所有的注解
            Annotation[][] parameterAnnotations = method.getParameterAnnotations();

            if(parameterAnnotations != null) {
                for (Annotation[] parameterAnnotation : parameterAnnotations) {
                    if (parameterAnnotation != null) {
                        for (int i = 0; i < parameterAnnotation.length; i++) {
                            if (parameterAnnotation[i]  instanceof RequestParam) {
                                RequestParam requestParam = (RequestParam) parameterAnnotation[i];
                                paramIndexMapping.put(requestParam.value(),i);
                            }
                        }
                    }
                }
            }


            Map<String,String[]> parameterMap = request.getParameterMap();
            Set<Map.Entry<String, String[]>> entries = parameterMap.entrySet();
            for (Map.Entry<String, String[]> entry : entries) {
                if(paramIndexMapping.containsKey(entry.getKey())){
                    args[paramIndexMapping.get(entry.getKey())] = Arrays.toString(entry.getValue()).replaceAll("\\[|\\]","");
                }
            }

            if(paramIndexMapping.containsKey(HttpServletRequest.class.getName())){
                args[paramIndexMapping.get(HttpServletRequest.class.getName())] = request;
            }

            if(paramIndexMapping.containsKey(HttpServletResponse.class.getName())){
                args[paramIndexMapping.get(HttpServletResponse.class.getName())] = response;
            }

            Object result = method.invoke(handlerMapping.getHandler(), args);
            if(result == null || result instanceof Void){
                return null;
            }

            if (result instanceof String){
                response.setCharacterEncoding("utf-8");
                response.getWriter().write(new String(result.toString().getBytes("ISO-8859-1"),"UTF-8"));
                return null;
            }

            if (result instanceof ModelAndView) return (ModelAndView) result;

        }
        return null;
    }

}
