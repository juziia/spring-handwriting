package com.juzi.v1.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)  // @Target:  标识当前注解的作用域    type标识类
@Retention(RetentionPolicy.RUNTIME)
public @interface JService {

    String value() default "";      // bean的名称

}
