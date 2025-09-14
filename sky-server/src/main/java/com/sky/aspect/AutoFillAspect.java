package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 自定义切面类，实现公共字段自动填充处理逻辑
 */
@Aspect
//也是bean要交给容器管理
@Component
@Slf4j
public class AutoFillAspect {

    /**
     * 切入点 ()切点表达式锁定mapper包下的类的所有方法 同时符合我们在方法上加入的AutoFill的注解
     */
    @Pointcut("execution(* com.sky.mapper.*.*(..))  && @annotation(com.sky.annotation.AutoFill)" )
    public void autoFillPointCut(){

    }
    //前置通知
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        log.info("开始进行公共字段自动填充...");
        //获取到当前被拦截方法上的数据库操作类型
        MethodSignature signature = (MethodSignature)joinPoint.getSignature();//方法签名对象
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);//获得方法上的注解对象
        OperationType operationType = autoFill.value();

        //获取到当前被拦截方法的参数--实体对象
        Object[] args = joinPoint.getArgs();
        if(args == null || args.length == 0){
            return;
        }
        Object arg = args[0];

        //准备赋值的数据
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();

        if(operationType == OperationType.INSERT){
            //根据当前不同的操作类型，为对应的属性通过反射来赋值
            Method setCreateTime = arg.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
            Method setUpdateTime = arg.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
            Method setCreateUser = arg.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
            Method setUpdateUser = arg.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

            //通过反射来为对象属性赋值
            setCreateTime.invoke(arg,now);
            setUpdateTime.invoke(arg,now);
            setCreateUser.invoke(arg,currentId);
            setUpdateUser.invoke(arg,currentId);
        }
        else if(operationType == OperationType.UPDATE){
            Method setUpdateTime = arg.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
            Method setUpdateUser = arg.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
            setUpdateTime.invoke(arg,now);
            setUpdateUser.invoke(arg,currentId);
        }



    }
}
