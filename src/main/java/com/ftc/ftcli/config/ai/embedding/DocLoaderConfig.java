package com.ftc.ftcli.config.ai.embedding;

import com.ftc.ftcli.properties.embedding.GithubProperties;
import dev.langchain4j.data.document.loader.github.GitHubDocumentLoader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-17 10:39:05
 * @describe 文档加载器配置
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties({GithubProperties.class})
public class DocLoaderConfig {

    private final GithubProperties githubProperties;

    @Bean
    public GitHubDocumentLoader githubDocumentLoader() {
        return GitHubDocumentLoader.builder()
                .gitHubToken(githubProperties.getToken())
                .build();
    }
}
