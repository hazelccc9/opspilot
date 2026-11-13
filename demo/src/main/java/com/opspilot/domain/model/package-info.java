/**
 * 领域模型：运行时对象与评测对象的统一定义。
 *
 * <p>这里刻意把两类对象放在同一个包，但用注释和测试区分它们的用途：
 *
 * <ul>
 *   <li><b>运行时对象</b>：{@code Alert}、{@code Incident}、{@code Evidence}、
 *       {@code Hypothesis}、{@code Action}、{@code Ticket}、{@code Runbook} 等。
 *       Agent 在调查过程中只能看到这些对象，以及工具返回的原始信号。</li>
 *   <li><b>评测对象</b>：{@code GroundTruth}、{@code IncidentCase}。
 *       只有评测框架可以读取，不能进入 Prompt、工具参数或 Agent 上下文。</li>
 * </ul>
 *
 * <p>{@code IncidentSignals} 是合成数据集里的原始信号包（发布、指标、日志、依赖）。
 * 在真实运行时，Agent 通过只读工具按需查询这些信号，而不是一次性拿到整包数据；
 * 数据集用整包形式是为了让评测可重复、可离线运行。
 *
 * <p>所有模型都是不可变 {@code record}。可空字段会在对应组件的 Javadoc 中明确标注；
 * 集合字段默认按“空集合”处理，避免调用方到处判空。
 */
package com.opspilot.domain.model;
