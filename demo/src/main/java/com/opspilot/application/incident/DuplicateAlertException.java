package com.opspilot.application.incident;

/**
 * 重复告警异常
 */
public class DuplicateAlertException extends RuntimeException {

    /**
     * 构造重复告警异常
     *
     * @param alertId 已经创建过故障的告警 ID
     */
    public DuplicateAlertException(String alertId) {
        // 把告警 ID 写入异常消息，方便日志和 API 错误响应定位问题
        super("Alert already has an incident: " + alertId);
    }
}
