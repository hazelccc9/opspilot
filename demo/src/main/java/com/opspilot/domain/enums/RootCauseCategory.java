package com.opspilot.domain.enums;

/**
 * 根因分类：评测时用于判断 Agent 的根因假设是否正确。
 *
 * <p>这 5 类覆盖了本项目合成数据集中的主要故障模式。真实生产故障可能同时涉及多个根因，
 * 当前只允许选择一个主分类；无法判断时使用 {@link #UNKNOWN}，并在评测中单独统计拒答率。
 */
public enum RootCauseCategory {

    /** 坏发布：新版本代码/配置引入回归，通常与发布时间高度相关。 */
    BAD_DEPLOY,

    /** 依赖超时：下游服务变慢或不可用，调用方线程被阻塞。 */
    DEPENDENCY_TIMEOUT,

    /** 连接池耗尽：慢查询、连接泄漏或并发过高导致拿不到数据库连接。 */
    CONNECTION_POOL_EXHAUSTION,

    /** 流量突增：请求量远超基线，CPU、队列或下游容量被打满。 */
    TRAFFIC_SPIKE,

    /** 配置错误：配置值非法、绑定失败或刷新后行为异常。 */
    CONFIG_ERROR,

    /** 信息不足或超出已知分类，Agent 应明确拒答并考虑升级人工。 */
    UNKNOWN
}
