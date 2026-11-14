package com.opspilot;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 应用上下文冒烟测试。
 */
@SpringBootTest
class OpsPilotApplicationTests {

    /**
     * 验证应用上下文可以正常启动。
     */
    @Test
    void contextLoads() {
        // 方法体为空：只要 Spring 上下文加载成功，测试即通过
    }
}
