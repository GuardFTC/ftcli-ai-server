package com.ftc.ftcli.config.ai.rag;

import com.ftc.ftcli.common.util.ai.AiTraceLog;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.rag.query.transformer.CompressingQueryTransformer;
import dev.langchain4j.rag.query.transformer.QueryTransformer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-17 10:49:49
 * @describe 查询转换器配置
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class QueryTransformerConfig {

    private final ChatModel model;

    @Bean
    public QueryTransformer queryTransformer() {

        //1.创建压缩查询转换器
        CompressingQueryTransformer compressingTransformer = new CompressingQueryTransformer(model);

        //2.包装为带追踪日志的转换器
        return query -> {

            //3.获取压缩后的查询
            var transformedQueries = compressingTransformer.transform(query);

            //4.打印转换日志
            AiTraceLog.logQueryTransform(query, transformedQueries);

            //5.返回压缩后的查询
            return transformedQueries;
        };
    }
}
