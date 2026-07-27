package com.eduhub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * EduHub 智能作业搜题平台 - 启动类
 *
 * @SpringBootApplication 包含三个注解：
 * 1. @SpringBootConfiguration - 标识这是一个配置类
 * 2. @EnableAutoConfiguration - 自动配置（面试考点：自动配置原理）
 * 3. @ComponentScan - 组件扫描
 */
@SpringBootApplication
@EnableCaching          // 启用缓存
@EnableTransactionManagement  // 启用事务管理
public class EduHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(EduHubApplication.class, args);
        System.out.println("==========================================");
        System.out.println("  EduHub 启动成功！");
        System.out.println("  API 文档: http://localhost:8080/doc.html");
        System.out.println("==========================================");
    }
}
