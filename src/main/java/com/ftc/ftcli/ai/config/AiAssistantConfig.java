package com.ftc.ftcli.config.ai;

import com.ftc.ftcli.ai.service.WebAiService;
import com.ftc.ftcli.infra.RedisChatMemoryStore;
import dev.langchain4j.memory.chat.TokenWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiTokenCountEstimator;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-05-28 14:39:05
 * @describe 智能助手配置类
 */
@Configuration(proxyBeanMethods = false)
public class AiAssistantConfig {

    /**
     * 聊天模型
     */
    private final ChatModel model;

    /**
     * Redis存储
     */
    private final RedisChatMemoryStore redisChatMemoryStore;

    /**
     * 最大Token数
     */
    @Value("${ai.chat-memory.max-tokens:95000}")
    private int maxTokens;

    /**
     * Token计数估算器模型名称
     */
    @Value("${ai.chat-memory.token-estimator-model:gpt-4o}")
    private String tokenEstimatorModel;

    /**
     * 构造方法
     *
     * @param model                聊天模型
     * @param redisChatMemoryStore Redis存储
     */
    public AiAssistantConfig(ChatModel model, RedisChatMemoryStore redisChatMemoryStore) {
        this.model = model;
        this.redisChatMemoryStore = redisChatMemoryStore;
    }

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
                        .maxTokens(maxTokens, new OpenAiTokenCountEstimator(tokenEstimatorModel))
                        .chatMemoryStore(redisChatMemoryStore)
                        .build())
                .build();
    }
}
