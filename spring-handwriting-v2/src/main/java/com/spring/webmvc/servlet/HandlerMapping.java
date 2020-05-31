package com.spring.webmvc.servlet;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

public class HandlerMapping {

    private Pattern url;
    private Object handler;
    private Method method;

    public HandlerMapping(Pattern url, Object handler, Method method) {
        this.url = url;
        this.handler = handler;
        this.method = method;
    }

    public Pattern getUrl() {
        return url;
    }

    public Object getHandler() {
        return handler;
    }

    public Method getMethod() {
        return method;
    }
}
