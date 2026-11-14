package com.opspilot.application.mock;

/** Mock 平台中找不到告警、用例或故障场景。 */
public class MockResourceNotFoundException extends RuntimeException {

    public MockResourceNotFoundException(String message) {
        super(message);
    }
}
