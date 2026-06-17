package com.ftc.ftcli.config.ai.embedding;

import com.ftc.ftcli.properties.embedding.GithubProperties;
import com.ftc.ftcli.properties.embedding.ModelProperties;
import com.ftc.ftcli.properties.embedding.StoreProperties;
import dev.langchain4j.community.model.zhipu.ZhipuAiEmbeddingModel;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.chroma.ChromaApiVersion;
import dev.langchain4j.store.embedding.chroma.ChromaEmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-17 10:39:05
 * @describe 嵌入模型和向量存储配置
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties({ModelProperties.class, StoreProperties.class, GithubProperties.class})
public class StoreAndModelConfig {

    private final ModelProperties modelProperties;

    private final StoreProperties storeProperties;

    @Bean
    public EmbeddingModel embeddingModel() {
        return ZhipuAiEmbeddingModel.builder()
                .apiKey(modelProperties.getApiKey())
                .model(modelProperties.getModelName())
                .build();
    }

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        return ChromaEmbeddingStore.builder()
                .baseUrl(storeProperties.getUrl())
                .apiVersion(ChromaApiVersion.V2)
                .tenantName(storeProperties.getTenant())
                .databaseName(storeProperties.getDatabase())
                .collectionName(storeProperties.getCollection())
                .logRequests(false)
                .logResponses(false)
                .build();
    }
}
