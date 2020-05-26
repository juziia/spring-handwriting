package com.juzi.controller;


import com.juzi.service.DemoService;
import com.juzi.v1.annotation.Autowired;
import com.juzi.v1.annotation.JController;
import com.juzi.v1.annotation.RequestMapping;
import com.juzi.v1.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@JController
@RequestMapping("///demo")
public class DemoController {

    @Autowired
    private DemoService demoService;


    @RequestMapping("getName")
    public void getName(HttpServletRequest request, HttpServletResponse response,
                            @RequestParam("name") String name) throws IOException {
        demoService.add();
        response.getWriter().write("my name is "+name);

    }


    @RequestMapping("getStr")
    public String getStr(){
        return "return one str";
    }



}
