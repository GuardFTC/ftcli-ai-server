package com.ftc.ftcli.config.ai.base;

import com.ftc.ftcli.common.util.ai.AiTraceLog;
import dev.langchain4j.model.chat.listener.ChatModelErrorContext;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.listener.ChatModelRequestContext;
import dev.langchain4j.model.chat.listener.ChatModelResponseContext;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.output.TokenUsage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-04-23 14:17:22
 * @describe AI链路追踪配置
 */
@Slf4j
@Configuration
public class AIRequestLogConfig {

    @Bean
    public ChatModelListener chatModelListener() {
        return new ChatModelListener() {

            @Override
            public void onRequest(ChatModelRequestContext requestContext) {
               //TODO 后续有需要时，添加追踪日志
            }

            @Override
            public void onResponse(ChatModelResponseContext responseContext) {

                //1.获取响应
                ChatResponse response = responseContext.chatResponse();

                //2.打印Token使用情况
                TokenUsage usage = response.metadata().tokenUsage();
                AiTraceLog.logTokenUsage(usage);
            }

            @Override
            public void onError(ChatModelErrorContext errorContext) {
                AiTraceLog.logError(errorContext.error().getMessage());
            }
        };
    }
}
