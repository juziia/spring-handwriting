package com.xm;


import com.spring.annotation.LazyInit;
import com.spring.annotation.Service;

@Service
//@LazyInit(true)
public class TestServiceImpl implements TestService {
    @Override
    public void hello() {
        System.out.println("hello .... ");
    }
}
