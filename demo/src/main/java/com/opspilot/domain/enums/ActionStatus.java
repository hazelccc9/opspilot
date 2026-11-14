package com.opspilot.domain.enums;

/**
 * 处置动作的生命周期状态
 *
 * 安全约束：动作必须先经过 {@code PROPOSED -> APPROVED}，才允许进入
 * {@code EXECUTING}。任何写操作都不能从 {@code PROPOSED} 直接跳到 {@code EXECUTED}。
 */
public enum ActionStatus {

    /** Agent 已提出建议，尚未经过人工审批。 */
    PROPOSED,

    /** 人工已批准，可以进入执行阶段。 */
    APPROVED,

    /** 人工拒绝，动作终止，不能执行。 */
    REJECTED,

    /** 正在执行（本项目仅允许沙箱或 dry-run）。 */
    EXECUTING,

    /** 执行成功并已记录结果。 */
    EXECUTED,

    /** 执行失败，需要回滚、重试或升级人工。 */
    FAILED,

    /** 只做了 dry-run 演练，没有产生真实副作用。 */
    DRY_RUN
}
