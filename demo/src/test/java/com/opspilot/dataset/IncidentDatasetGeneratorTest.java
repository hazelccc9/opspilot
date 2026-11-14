package com.opspilot.dataset;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 数据集生成测试。
 */
class IncidentDatasetGeneratorTest {

    /**
     * 当系统属性 {@code opspilot.generateDataset=true} 时生成数据集并验证文件非空。
     *
     * @throws Exception 生成或读取文件失败
     */
    @Test
    void generateDatasetWhenRequested() throws Exception {
        // 默认跳过，避免普通测试运行覆盖仓库里的数据集
        if (!Boolean.getBoolean("opspilot.generateDataset")) {
            return;
        }
        // 生成到测试约定的数据集路径
        Path output = DatasetPaths.incidents();
        IncidentDatasetGenerator.generate(output);
        // 生成后确认文件存在且内容非空
        assertTrue(Files.exists(output));
        assertTrue(Files.size(output) > 0);
    }
}
