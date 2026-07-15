package com.ftc.ftcli.config.ai.rag;

import com.ftc.ftcli.common.util.ai.AiTraceLog;
import com.ftc.ftcli.properties.rag.RerankProperties;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.jina.JinaScoringModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.scoring.ScoringModel;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.aggregator.ContentAggregator;
import dev.langchain4j.rag.content.aggregator.ReRankingContentAggregator;
import dev.langchain4j.rag.query.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.*;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-17 10:52:56
 * @describe 内容聚合器配置
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties({RerankProperties.class})
public class ContentAggregatorConfig {

    private final RerankProperties rerankProperties;

    @Bean
    public ContentAggregator contentAggregator() {

        //1.创建Jina评分模型
        JinaScoringModel scoringModel = JinaScoringModel.builder()
                .apiKey(rerankProperties.getApiKey())
                .modelName(rerankProperties.getModel())
                .build();

        //2.包装评分模型，添加追踪日志
        ScoringModel tracedScoringModel = (segments, query) -> {

            //3.调用评分
            Response<List<Double>> response = scoringModel.scoreAll(segments, query);

            //4.打印重排日志
            AiTraceLog.logRerank(query, segments, response.content(), rerankProperties.getMaxResults(), rerankProperties.getMinScore());

            //5.返回
            return response;
        };

        //6.创建ReRanking聚合器
        ReRankingContentAggregator reRankingAggregator = ReRankingContentAggregator.builder()
                .scoringModel(tracedScoringModel)
                .minScore(rerankProperties.getMinScore())
                .maxResults(rerankProperties.getMaxResults())
                .build();

        //7.返回自定义聚合器：先去重再调用ReRanking聚合器
        return (queryToContents) -> {

            //8.合并所有检索器的结果
            List<Content> allContents = queryToContents.values().stream()
                    .flatMap(Collection::stream)
                    .flatMap(List::stream)
                    .toList();

            //9.对于合并结果进行去重
            List<Content> deduplicated = deduplicateContents(allContents);
            AiTraceLog.logRerankDedup(allContents.size(), deduplicated.size());

            //10.将去重后的结果重新包装
            Query query = queryToContents.keySet().iterator().next();

            //11.交给ReRanking聚合器处理
            return reRankingAggregator.aggregate(Map.of(query, List.of(deduplicated)));
        };
    }

    /**
     * 基于 file_name + chunk_content_md5 对Content列表去重
     *
     * @param contents 原始Content列表
     * @return 去重后的Content列表
     */
    private static List<Content> deduplicateContents(List<Content> contents) {

        //1.定义去重集合
        Set<String> seen = new LinkedHashSet<>();
        List<Content> result = new ArrayList<>();

        //2.遍历Content，基于file_name + chunk_content_md5去重
        for (Content content : contents) {

            //3.获取文件名和内容MD5
            TextSegment segment = content.textSegment();
            String fileName = segment.metadata().getString("file_name");
            String contentMd5 = segment.metadata().getString("chunk_content_md5");

            //4.拼接Key
            String key = (fileName == null ? "" : fileName) + "|" + (contentMd5 == null ? "" : contentMd5);

            //5.如果去重集合中添加成功，则添加到结果列表
            if (seen.add(key)) {
                result.add(content);
            }
        }

        //6.返回去重后的Content列表
        return result;
    }
}
