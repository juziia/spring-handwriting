package com.juzi.service.impl;

import com.juzi.service.DemoService;
import com.juzi.v1.annotation.JService;


@JService
public class DemoServiceImpl implements DemoService {
    @Override
    public void add() {
        System.out.println("添加了一个demo");
    }



}
