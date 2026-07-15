package com.ftc.ftcli.properties.embedding;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-03 10:35:05
 * @describe ElasticSearch向量存储配置属性类
 */
@Data
@Slf4j
@ConfigurationProperties(prefix = "ai.embedding.store.elasticsearch")
public class StoreESProperties {

    /**
     * ElasticSearch服务URL
     */
    private String url;

    /**
     * API-KEY
     */
    private String apiKey;
}
