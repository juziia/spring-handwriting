package com.juzi.v2;

import java.util.Arrays;
import java.util.Collections;

public class Convert {

    public void convert(String[] value, Class type, Integer index, Object[] args){
        if (type == Integer.class){
            args[index] = Integer.valueOf(value[0].trim());
        }else if (type ==  Double.class){
            args[index] = Double.valueOf(value[0].trim());
        }else if (type == String.class){
            System.out.println("---------------");
            String val = Arrays.toString(value);
            args[index] = val.substring(1,val.length() - 1);
        }

    }
}
