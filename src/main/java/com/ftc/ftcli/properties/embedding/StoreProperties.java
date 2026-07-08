package com.ftc.ftcli.properties.embedding;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-07-08 19:17:35
 * @describe 向量存储配置属性类
 */
@Data
@ConfigurationProperties(prefix = "ai.embedding.store")
public class StoreProperties {

    /**
     * 向量写入批次大小
     */
    private int batchSize = 50;
}
