package com.opspilot.application.incident;

/** 同一条告警已经创建过故障，拒绝重复创建。 */
public class DuplicateAlertException extends RuntimeException {

    public DuplicateAlertException(String alertId) {
        super("Alert already has an incident: " + alertId);
    }
}
