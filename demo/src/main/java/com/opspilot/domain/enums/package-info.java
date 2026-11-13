/**
 * 领域枚举：用封闭集合表达告警处理中的关键状态和分类。
 *
 * <p>枚举值会同时出现在 Java 代码、JSONL 数据集和 JSON Schema 中。新增或重命名枚举值时，
 * 必须同步修改：
 *
 * <ul>
 *   <li>{@code demo-data/schema/incident.schema.json}；</li>
 *   <li>数据集生成器 {@code IncidentDatasetGenerator}；</li>
 *   <li>相关测试和 {@code docs/domain-model.md}。</li>
 * </ul>
 *
 * <p>这样做的原因是：字符串枚举一旦漂移，评测标签和 Agent 输出就无法比较，
 * 最终会变成“看起来有评测、实际对不上”的假指标。
 */
package com.opspilot.domain.enums;
