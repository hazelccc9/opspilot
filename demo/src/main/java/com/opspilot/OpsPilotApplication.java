package com.opspilot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * OpsPilot 应用启动入口。
 *
 * 通过 Spring Boot 启动 Web 应用，并自动装配 REST、应用服务和基础设施组件。
 */
@SpringBootApplication
public class OpsPilotApplication {

    /**
     * 应用主入口。
     *
     * @param args 启动参数，交由 Spring Boot 解析
     */
    public static void main(String[] args) {
        // 启动 Spring 容器，加载 REST、应用服务和基础设施组件
        SpringApplication.run(OpsPilotApplication.class, args);
    }
}
