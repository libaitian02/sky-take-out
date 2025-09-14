package com.sky.annotation;


import com.sky.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解，用于标识某个方法需要进行功能字段自动填充处理
 * AutoFill 注解定义了 “需要自动填充的场景”，AutoFillAspect 实现了 “自动填充的具体逻辑”，二者通过 AOP 机制绑定
 */
@Target(ElementType.METHOD) //指定注解只能加在方法上
//元注解（注解的注解），用于指定被它修饰的注解在 运行时是否可见
//SOURCE：注解仅在源代码中存在，编译时会被丢弃（不会进入 .class 文件）。
//CLASS：注解会被编译到 .class 文件中，但运行时不会被 JVM 保留（默认策略）。
//RUNTIME：注解会被编译到 .class 文件中，且运行时会被 JVM 保留，可通过反射机制获取。
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoFill {
    //指定数据库操作类型只有UPDATE和INSERT需要插入
    OperationType value();
}
