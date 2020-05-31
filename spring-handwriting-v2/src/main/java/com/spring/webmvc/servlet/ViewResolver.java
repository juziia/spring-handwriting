package com.spring.webmvc.servlet;

import java.io.File;
import java.util.Locale;

/**
 *  ViewResolver 用于解析方法返回后的ModelAndView中的视图名称
 */
public class ViewResolver {

    private String templateRootDir;

    private String VIEW_SUFFIX = ".html";

    public ViewResolver(String templateRootDir) {
        this.templateRootDir = templateRootDir;
    }

    public View resolveViewName(String viewName) throws Exception{
        String file = this.getClass().getClassLoader().getResource(templateRootDir).getFile();

        return  new View(new File(file+"/"+viewName+VIEW_SUFFIX));
    }

}
