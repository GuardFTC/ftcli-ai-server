package com.ftc.ftcli.properties.embedding;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-08 21:00:00
 * @describe GitHub文档加载配置属性类
 */
@Data
@ConfigurationProperties(prefix = "ai.embedding.github")
public class GithubProperties {

    /**
     * GitHub Token
     */
    private String token;
}
