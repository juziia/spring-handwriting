package com.spring.webmvc.servlet;

import java.util.Map;

public class ModelAndView {

    private String view;        // 视图名称

    private Map<String,?> model;       // 数据

    public ModelAndView(String view) {
        this.view = view;
    }

    public ModelAndView(String view, Map<String, ?> model) {
        this.view = view;
        this.model = model;
    }

    public String getView() {
        return view;
    }

    public Map<String, ?> getModel() {
        return model;
    }
}
