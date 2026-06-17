package com.ftc.ftcli.config.ai.rag;

import com.ftc.ftcli.common.util.ai.AiTraceLog;
import com.ftc.ftcli.properties.rag.RerankProperties;
import dev.langchain4j.model.jina.JinaScoringModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.scoring.ScoringModel;
import dev.langchain4j.rag.content.aggregator.ReRankingContentAggregator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

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
    public ReRankingContentAggregator contentAggregator() {

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

        //6.定义内容聚合器，基于重排模型进行文档二次过滤，并按最小分数过滤低相关文档
        return ReRankingContentAggregator.builder()
                .scoringModel(tracedScoringModel)
                .minScore(rerankProperties.getMinScore())
                .maxResults(rerankProperties.getMaxResults())
                .build();
    }
}
