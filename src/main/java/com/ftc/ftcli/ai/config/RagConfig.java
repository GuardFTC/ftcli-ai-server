package com.ftc.ftcli.ai.config;

import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.WebSearchContentRetriever;
import dev.langchain4j.rag.query.router.QueryRouter;
import dev.langchain4j.web.search.WebSearchEngine;
import dev.langchain4j.web.search.tavily.TavilyWebSearchEngine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-02 17:31:20
 * @describe RAG配置类
 */
@Slf4j
@Configuration
public class RagConfig {

    @Bean
    public QueryRouter webAiQueryRouter() {

        //1.创建 Web 搜索引擎
        WebSearchEngine webSearchEngine = TavilyWebSearchEngine.builder()
                .apiKey("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")
                .build();

        //2.创建网络内容检索器
        ContentRetriever webSearchContentRetriever = WebSearchContentRetriever.builder()
                .webSearchEngine(webSearchEngine)
                .maxResults(3)
                .build();

        //3.预编译“必须联网”的特征词正则表达式（放入外部或静态预编译性能更佳）
        //包含了：时效性词汇、实时数据词汇、以及近几年的年份（动态追踪最新事件）
        Pattern webSearchPattern = Pattern.compile(
                ".*(最新|新闻|今天|今年|时事|天气|股价|股票|实时|上线|发布|开源|官网|怎么下载|哪里买|发生什么).*|.*(\\b(202[4-6])\\b).*",
                Pattern.CASE_INSENSITIVE
        );

        //4.返回确定性的路由逻辑
        return query -> {

            //5.获取查询文本
            String queryText = query.text().trim();

            //6.如果用户在前端输入了 `/search`、`/web`，或者话里带有“上网查查”，直接无条件触发联网
            if (queryText.startsWith("/search") || queryText.startsWith("/web") || queryText.contains("上网查")) {
                log.info("[Router] 匹配到 /search 或 /web，开启网络检索");
                return List.of(webSearchContentRetriever);
            }

            //7.策略B：时效性与网络特征词自动匹配
            if (webSearchPattern.matcher(queryText).matches()) {
                log.info("[Router] 匹配到时效性或网络特征词，开启网络检索");
                return List.of(webSearchContentRetriever);
            }

            //8.策略C：不满足以上条件（如：日常闲聊、写个Java单例、翻译句子、纯文本创作等）
            log.info("[Router] 无需联网：直接让 LLM 自身能力回答（零延迟/零API消耗）");
            return Collections.emptyList();
        };
    }
}
