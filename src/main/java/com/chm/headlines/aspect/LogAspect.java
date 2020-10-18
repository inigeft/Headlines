package com.chm.headlines.aspect;

import org.apache.commons.lang.text.StrBuilder;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LogAspect {
    public static final Logger logger = LoggerFactory.getLogger(LogAspect.class);

    @Before("execution(* com.chm.headlines.controller.*Controller.*(..))")
    public void beforeMethod(JoinPoint joinPoint) {     //没明白JointPoint的含义，也没明白如何定位到程序中具体的点?
        StrBuilder strBuilder = new StrBuilder();
        for(Object arg: joinPoint.getArgs()) {
            strBuilder.append("arg:");
            strBuilder.append(arg);
            strBuilder.append("| ");
        }
        logger.info("Before Method:"+strBuilder.toString());
    }

    @After("execution(* com.chm.headlines.controller.*Controller.*(..))")
    public void afterMethod(JoinPoint joinPoint) {
        logger.info("After Method:");
    }
}
