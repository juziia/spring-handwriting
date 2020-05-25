package com.juzi.v1;

import javax.servlet.http.HttpServlet;

public class FrameworkServlet extends HttpServlet {

    private String contextConfigLocation;

    public String getContextConfigLocation() {
        return contextConfigLocation;
    }

    public void setContextConfigLocation(String contextConfigLocation) {
        this.contextConfigLocation = contextConfigLocation;
    }
}
