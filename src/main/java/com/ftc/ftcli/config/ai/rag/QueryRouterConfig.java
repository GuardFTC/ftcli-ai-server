package com.ftc.ftcli.config.ai.rag;

import com.ftc.ftcli.common.util.ai.AiTraceLog;
import com.ftc.ftcli.properties.rag.ContentRetrieverProperties;
import com.ftc.ftcli.properties.rag.WebSearchProperties;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.content.retriever.WebSearchContentRetriever;
import dev.langchain4j.rag.query.router.LanguageModelQueryRouter;
import dev.langchain4j.rag.query.router.QueryRouter;
import dev.langchain4j.store.embedding.EmbeddingStore;
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
@EnableConfigurationProperties({WebSearchProperties.class, ContentRetrieverProperties.class})
public class QueryRouterConfig {

    private final ChatModel model;

    private final EmbeddingModel embeddingModel;

    private final EmbeddingStore<TextSegment> embeddingStore;

    private final WebSearchProperties webSearchProperties;

    private final ContentRetrieverProperties contentRetrieverProperties;

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
        ContentRetriever tracedRetriever = query -> {

            //4.检索
            List<Content> contents = webSearchContentRetriever.retrieve(query);

            //5.打印检索日志
            AiTraceLog.logRetrievalQuery(query.text());
            AiTraceLog.logRetrievalResults(contents);

            //6.返回文档
            return contents;
        };

        //7.使用LLM路由：由模型自行判断用户问题是否需要联网检索，替代正则匹配
        return new LanguageModelQueryRouter(model, Map.of(
                tracedRetriever, "用于查询实时信息、最新新闻、时事热点、技术框架最新版本、产品价格、赛事结果等需要联网获取的动态内容。不要用于回答编程概念、语法规则、设计模式等稳定的知识性问题。"
        ));
    }

    @Bean
    public QueryRouter localAiQueryRouter() {

        //1.创建文档检索器
        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .maxResults(contentRetrieverProperties.getMaxResults())
                .minScore(contentRetrieverProperties.getMinScore())
                .build();

        //2.包装检索器，添加追踪日志
        ContentRetriever tracedRetriever = query -> {

            //3.检索
            List<Content> contents = contentRetriever.retrieve(query);

            //4.打印检索日志
            AiTraceLog.logRetrievalQuery(query.text());
            AiTraceLog.logRetrievalResults(contents);

            //5.返回文档
            return contents;
        };

        //7.创建自定义查询路由器：默认使用文档检索器
        return query -> {

            //8.返回检索器
            return List.of(tracedRetriever);
        };
    }
}
