package com.opspilot.dataset;

import java.nio.file.Path;

/**
 * 数据集路径解析工具。
 *
 * Maven 会传入 {@code opspilot.data.dir}；没有配置时回退到 {@code demo-data}，
 * 保证在 IDE 中以 {@code demo} 模块为工作目录时也能正常运行。
 */
final class DatasetPaths {

    /** 指定数据集目录的系统属性名。 */
    private static final String DATA_DIR_PROPERTY = "opspilot.data.dir";

    /**
     * 工具类构造函数，禁止外部实例化。
     */
    private DatasetPaths() {
        // 工具类只提供静态方法，禁止实例化
    }

    /**
     * 解析数据集根目录。
     *
     * @return 数据集目录的绝对路径
     */
    static Path dataDir() {
        // 优先使用显式配置的数据集目录
        String configured = System.getProperty(DATA_DIR_PROPERTY);
        if (configured != null && !configured.isBlank()) {
            return Path.of(configured).toAbsolutePath().normalize();
        }
        // 未配置时回退到模块上一级的 demo-data 目录
        return Path.of(System.getProperty("user.dir"))
                .resolve("..")
                .resolve("demo-data")
                .toAbsolutePath()
                .normalize();
    }

    /**
     * 解析故障数据集文件路径。
     *
     * @return incidents.jsonl 的路径
     */
    static Path incidents() {
        // 数据集文件固定位于数据目录下
        return dataDir().resolve("incidents.jsonl");
    }

    /**
     * 解析 JSON Schema 文件路径。
     *
     * @return incident.schema.json 的路径
     */
    static Path schema() {
        // Schema 文件位于数据目录的 schema 子目录下
        return dataDir().resolve("schema").resolve("incident.schema.json");
    }
}
