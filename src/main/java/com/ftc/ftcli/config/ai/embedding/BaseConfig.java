package com.ftc.ftcli.config.ai.embedding;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.ftc.ftcli.properties.embedding.GithubProperties;
import com.ftc.ftcli.properties.embedding.ModelProperties;
import com.ftc.ftcli.properties.embedding.StoreChromaProperties;
import com.ftc.ftcli.properties.embedding.StoreESProperties;
import dev.langchain4j.community.model.zhipu.ZhipuAiEmbeddingModel;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.chroma.ChromaApiVersion;
import dev.langchain4j.store.embedding.chroma.ChromaEmbeddingStore;
import dev.langchain4j.store.embedding.elasticsearch.ElasticsearchConfigurationFullText;
import dev.langchain4j.store.embedding.elasticsearch.ElasticsearchEmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-17 10:39:05
 * @describe 基础配置：嵌入模型、向量存储
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties({ModelProperties.class, StoreChromaProperties.class, GithubProperties.class})
public class BaseConfig {

    private final ModelProperties modelProperties;

    private final StoreChromaProperties storeChromaProperties;

    private final StoreESProperties storeESProperties;

    private final ElasticsearchClient esClient;

    @Bean
    public EmbeddingModel embeddingModel() {
        return ZhipuAiEmbeddingModel.builder()
                .apiKey(modelProperties.getApiKey())
                .model(modelProperties.getModelName())
                .build();
    }

    @Bean
    @Primary
    public EmbeddingStore<TextSegment> embeddingStore() {
        return ChromaEmbeddingStore.builder()
                .baseUrl(storeChromaProperties.getUrl())
                .apiVersion(ChromaApiVersion.V2)
                .tenantName(storeChromaProperties.getTenant())
                .databaseName(storeChromaProperties.getDatabase())
                .collectionName(storeChromaProperties.getCollection())
                .logRequests(false)
                .logResponses(false)
                .build();
    }

    @Bean
    public EmbeddingStore<TextSegment> esEmbeddingStore() {
        return ElasticsearchEmbeddingStore.builder()
                .client(esClient)
                .configuration(ElasticsearchConfigurationFullText.builder().build())
                .indexName(storeESProperties.getIndex())
                .build();
    }
}
