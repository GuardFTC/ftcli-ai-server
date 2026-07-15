package com.ftc.ftcli.config.ai.rag;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.ftc.ftcli.common.util.ai.AiTraceLog;
import com.ftc.ftcli.properties.rag.ChromaRetrieverProperties;
import com.ftc.ftcli.properties.rag.EsRetrieverProperties;
import com.ftc.ftcli.properties.rag.WebSearchProperties;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.ContentMetadata;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.content.retriever.WebSearchContentRetriever;
import dev.langchain4j.rag.content.retriever.elasticsearch.ElasticsearchContentRetriever;
import dev.langchain4j.rag.query.router.LanguageModelQueryRouter;
import dev.langchain4j.rag.query.router.QueryRouter;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.elasticsearch.ElasticsearchConfigurationFullText;
import dev.langchain4j.web.search.WebSearchEngine;
import dev.langchain4j.web.search.tavily.TavilyWebSearchEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-17 10:50:47
 * @describe 查询路由器配置
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties({WebSearchProperties.class, ChromaRetrieverProperties.class, EsRetrieverProperties.class})
public class QueryRouterConfig {

    private final ChatModel model;

    private final EmbeddingModel embeddingModel;

    private final EmbeddingStore<TextSegment> embeddingStore;

    private final WebSearchProperties webSearchProperties;

    private final ChromaRetrieverProperties chromaRetrieverProperties;

    private final EsRetrieverProperties esRetrieverProperties;

    private final ElasticsearchClient esClient;

    @Bean
    public QueryRouter webAiQueryRouter() {

        //1.创建 Web 搜索引擎
        WebSearchEngine webSearchEngine = TavilyWebSearchEngine.builder()
                .apiKey(webSearchProperties.getApiKey())
                .build();

        //2.创建网络内容检索器
        ContentRetriever webSearchContentRetriever = WebSearchContentRetriever.builder()
                .webSearchEngine(webSearchEngine)
                .maxResults(webSearchProperties.getMaxResults())
                .build();

        //3.包装检索器，添加追踪日志
        ContentRetriever tracedRetriever = getTracedRetriever("web检索器", webSearchContentRetriever);

        //7.使用LLM路由：由模型自行判断用户问题是否需要联网检索，替代正则匹配
        return new LanguageModelQueryRouter(model, Map.of(
                tracedRetriever, "用于查询实时信息、最新新闻、时事热点、技术框架最新版本、产品价格、赛事结果等需要联网获取的动态内容。不要用于回答编程概念、语法规则、设计模式等稳定的知识性问题。"
        ));
    }

    @Bean
    public QueryRouter localAiQueryRouter() {

        //1.创建Chroma文档检索器
        ContentRetriever chromaStoreRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .maxResults(chromaRetrieverProperties.getMaxResults())
                .minScore(chromaRetrieverProperties.getMinScore())
                .build();

        //2.包装Chroma文档检索器，添加追踪日志
        ContentRetriever chromaStoreTracedRetriever = getTracedRetriever("chroma检索器", chromaStoreRetriever);

        //3.创建ES文档检索器（BM25全文检索）
        ContentRetriever esContentRetriever = ElasticsearchContentRetriever.builder()
                .client(esClient)
                .configuration(ElasticsearchConfigurationFullText.builder().build())
                .build();

        //4.包装ES文档检索器，添加追踪日志（maxResults/minScore在beta版中未生效，通过应用层过滤）
        ContentRetriever esStoreTracedRetriever = getTracedRetriever("es检索器", esContentRetriever,
                esRetrieverProperties.getMaxResults(),
                esRetrieverProperties.getMinScore()
        );

        //5.创建自定义查询路由器：默认使用文档检索器
        return query -> {

            //6.返回检索器
            return List.of(chromaStoreTracedRetriever, esStoreTracedRetriever);
        };
    }

    /**
     * 包装检索器，添加追踪日志
     *
     * @param title            检索器标题
     * @param contentRetriever 检索器
     * @return 包装后的检索器
     */
    private static ContentRetriever getTracedRetriever(String title, ContentRetriever contentRetriever) {
        return getTracedRetriever(title, contentRetriever, 0, 0);
    }

    /**
     * 包装检索器，添加追踪日志，并支持应用层过滤
     *
     * @param title            检索器标题
     * @param contentRetriever 检索器
     * @param maxResults       最大返回条数（0表示不限制）
     * @param minScore         最小分数阈值（0表示不过滤）
     * @return 包装后的检索器
     */
    private static ContentRetriever getTracedRetriever(String title, ContentRetriever contentRetriever, int maxResults, double minScore) {
        return query -> {

            //1.检索
            List<Content> contents = contentRetriever.retrieve(query);

            //2.按minScore过滤，将分数小于minScore的文档过滤掉
            if (minScore > 0) {
                contents = contents.stream()
                        .filter(c -> {
                            Object scoreObj = c.metadata().get(ContentMetadata.SCORE);
                            return scoreObj != null && ((Number) scoreObj).doubleValue() >= minScore;
                        })
                        .toList();
            }

            //3.按score降序排列并限制条数
            if (maxResults > 0 && contents.size() > maxResults) {
                contents = contents.stream()
                        .sorted((a, b) -> {
                            double scoreA = ((Number) a.metadata().getOrDefault(ContentMetadata.SCORE, 0.0)).doubleValue();
                            double scoreB = ((Number) b.metadata().getOrDefault(ContentMetadata.SCORE, 0.0)).doubleValue();
                            return Double.compare(scoreB, scoreA);
                        })
                        .limit(maxResults)
                        .toList();
            }

            //4.打印检索日志
            AiTraceLog.logRetrievalQuery(title, query.text());
            AiTraceLog.logRetrievalResults(title, contents);

            //5.返回文档
            return contents;
        };
    }
}
