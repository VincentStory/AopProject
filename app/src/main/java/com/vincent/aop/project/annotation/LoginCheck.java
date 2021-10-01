package com.vincent.aop.project.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// 用户登录检测
@Target({ElementType.PARAMETER, ElementType.METHOD}) // 目标作用在参数和方法之上
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginCheck {

    public String title() default "";

    //是否保存请求的参数
    public boolean isSaveRequestData() default true;


}
