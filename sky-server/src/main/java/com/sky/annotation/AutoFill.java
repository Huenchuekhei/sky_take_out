package com.sky.annotation;

import com.sky.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)//规定了注解的使用范围（哪里可以贴）。
@Retention(RetentionPolicy.RUNTIME)//规定了注解的生命周期（信息保留到什么时候）。
public @interface AutoFill {
    OperationType value();
}
