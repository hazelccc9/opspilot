package com.opspilot.application.incident;

/**
 * 故障不存在异常
 */
public class IncidentNotFoundException extends RuntimeException {

    /**
     * 构造故障不存在异常
     *
     * @param incidentId 查询不到的故障 ID
     */
    public IncidentNotFoundException(String incidentId) {
        // 把故障 ID 写入异常消息，便于调用方和日志快速定位
        super("Incident not found: " + incidentId);
    }
}
