/**
 * OpsPilot 的领域层。
 *
 * <p>这一层只描述“告警处理”这件事本身，不依赖 Spring、Web、数据库或任何 LLM SDK。
 * 这样做的目的：
 *
 * <ul>
 *   <li>领域对象可以在单元测试、评测脚本、持久化层和 Agent 运行时之间复用；</li>
 *   <li>不会因为换模型、换数据库、换 Web 框架而改动业务语义；</li>
 *   <li>评测标签（{@link com.opspilot.domain.model.GroundTruth}）和运行时对象在物理上分开，
 *       从结构上避免 ground truth 泄漏进 Agent 上下文。</li>
 * </ul>
 *
 * <p>领域对象统一使用 Java {@code record}：不可变、可序列化、线程安全，适合作为
 * JSONL 数据集、trace 和评测报告的共同数据格式。
 *
 * <p>当前包含两个子包：
 *
 * <ul>
 *   <li>{@code enums}：封闭取值集合，例如严重级别、根因类别、动作类型；</li>
 *   <li>{@code model}：运行时聚合与评测用例，例如 {@code Alert}、{@code Evidence}、
 *       {@code Hypothesis}、{@code Action}、{@code IncidentCase}。</li>
 * </ul>
 */
package com.opspilot.domain;
