package com.ftc.ftcli.properties.rag;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-16 16:00:09
 * @describe 内容检索器配置
 */
@Data
@ConfigurationProperties(prefix = "ai.rag.content-retriever")
public class ContentRetrieverProperties {

    /**
     * 检索最大结果数
     */
    private Integer maxResults;

    /**
     * 检索最小分数
     */
    private Double minScore;
}
