package com.ftc.ftcli.ai.config;

import com.ftc.ftcli.ai.assistant.WebAiService;
import com.ftc.ftcli.ai.infra.RedisChatMemoryStore;
import com.ftc.ftcli.ai.properties.ChatMemoryProperties;
import dev.langchain4j.memory.chat.TokenWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiTokenCountEstimator;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.tool.ToolProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-05-28 14:39:05
 * @describe 智能助手配置类
 */
@RequiredArgsConstructor
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(ChatMemoryProperties.class)
public class AiAssistantConfig {

    private final ChatModel model;

    private final ChatMemoryProperties chatMemoryProperties;

    private final RedisChatMemoryStore redisChatMemoryStore;

    private final ToolProvider toolProvider;

    /**
     * 创建Web问答服务
     *
     * @return webAiService
     */
    @Bean
    public WebAiService webAiService() {
        return AiServices.builder(WebAiService.class)
                .chatModel(model)
                .chatMemoryProvider(memoryId -> TokenWindowChatMemory.builder()
                        .id(memoryId)
                        .maxTokens(chatMemoryProperties.getMaxTokens(), new OpenAiTokenCountEstimator(chatMemoryProperties.getTokenEstimatorModel()))
                        .chatMemoryStore(redisChatMemoryStore)
                        .build())
                .toolProvider(toolProvider)
                .build();
    }
}
