package com.spring.webmvc.servlet;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *  View:  view组件用于页面的渲染
 */
public class View {

    private File viewFile;
    public View(File file) {
        this.viewFile = file;
    }

    /**
     *  进行页面渲染的方法
     * @param model
     * @param request
     * @param response
     * @throws Exception
     */
    public void render(Map<String, ?> model,
                HttpServletRequest request, HttpServletResponse response) throws Exception{

        BufferedReader bufferedReader = new BufferedReader(new FileReader(viewFile));
        StringBuilder sb = new StringBuilder();
        String line = null;

        while(null != (line = bufferedReader.readLine())){
            line = new String(line.getBytes("ISO-8859-1"),"UTF-8");
            Pattern pattern = Pattern.compile("@\\{[^\\}]+\\}", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(line);
            while(matcher.find()){
                String paramName = matcher.group();
                paramName = paramName.replaceAll("@\\{|\\}","");
                Object paramValue = model.get(paramName);
                if(paramValue == null) continue;
                line = matcher.replaceFirst(paramValue.toString());
                matcher = pattern.matcher(line);
            }
            sb.append(line);

        }


        response.setCharacterEncoding("utf-8");
        response.setContentType("text/html;charset=utf-8");
        response.getWriter().write(sb.toString());
        return;
    }

}
