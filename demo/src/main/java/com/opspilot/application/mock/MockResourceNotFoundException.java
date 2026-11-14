package com.opspilot.application.mock;

/**
 * Mock 资源不存在异常
 */
public class MockResourceNotFoundException extends RuntimeException {

    /**
     * 构造 Mock 资源不存在异常。
     *
     * @param message 包含资源类型的错误消息
     */
    public MockResourceNotFoundException(String message) {
        // 消息由调用方组装，直接交给父类保存
        super(message);
    }
}
