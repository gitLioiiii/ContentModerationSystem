package com.example.demo.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

// 先显示security,再显示aop，最后显示mybatis
// 切面
@Aspect
@Component
public class LoginAspect {
    // 切入点
    // PCD （Pointcut Designator）切入点指示器
    // execution 用于匹配方法执行的连接点
    // * 表示任意返回类型
    // com.example.demo.controller.SecurityController.login(..)是切入点表达式
    // (..) 表示任意参数
    @Pointcut("execution(* com.example.demo.controller.SecurityController.login(..))")
    public void pointcut() {}
    
    @Around("pointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        String methodName = joinPoint.getSignature().getName();
        long startTime = System.currentTimeMillis();
        
        System.out.println("""
            =================== 登录日志开始 ===================
            你好，登录开始！
            """);
        System.out.println("时间: " + java.time.LocalDateTime.now());
        System.out.println("方法: " + methodName);
        
        // 记录登录参数（隐藏密码）
        if (args.length > 0 && args[0] instanceof com.example.demo.entity.UserEntity) {
            com.example.demo.entity.UserEntity user = (com.example.demo.entity.UserEntity) args[0];
            System.out.println("用户名: " + user.getUsername());
            System.out.println("密码: [已隐藏]");
        }
        
        try {
            // 执行登录方法
            Object result = joinPoint.proceed();
            
            long endTime = System.currentTimeMillis();
            System.out.println("登录结果: 成功");
            System.out.println("执行时间: " + (endTime - startTime) + "ms");
            System.out.println("""
                =================== 登录日志结束 ===================
                你好，登录成功！
                """);
            
            return result;
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            System.out.println("登录结果: 失败 - " + e.getMessage());
            System.out.println("执行时间: " + (endTime - startTime) + "ms");
            System.out.println("""
                =================== 登录日志结束 ===================
                你好，登录失败！
                """);
            throw e;
        }
    }
}

// HTTP请求 → Spring Security过滤器链 → Controller方法 → AOP切面(@Around) → 
// Service方法 → MyBatis数据库操作 → 返回结果 → AOP切面(记录结果) → 
// Controller返回 → Spring Security过滤器链 → HTTP响应
