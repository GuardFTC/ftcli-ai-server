package com.ftc.ftcli.config.ai;

import cn.hutool.core.io.resource.ResourceUtil;
import com.ftc.ftcli.ai.assistant.LocalAiService;
import com.ftc.ftcli.ai.assistant.WebAiService;
import com.ftc.ftcli.ai.store.SqliteChatMemoryStore;
import com.ftc.ftcli.properties.chat.ChatMemoryProperties;
import dev.langchain4j.memory.chat.TokenWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiTokenCountEstimator;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.content.injector.ContentInjector;
import dev.langchain4j.rag.query.router.QueryRouter;
import dev.langchain4j.rag.query.transformer.QueryTransformer;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.tool.ToolProvider;
import dev.langchain4j.skills.Skills;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-05-28 14:39:05
 * @describe 智能助手配置类
 */
@Slf4j
@RequiredArgsConstructor
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(ChatMemoryProperties.class)
public class AiAssistantConfig {

    private final ChatModel model;

    private final StreamingChatModel streamingModel;

    private final SqliteChatMemoryStore chatMemoryStore;

    private final ToolProvider toolProvider;

    private final QueryTransformer queryTransformer;

    private final QueryRouter webAiQueryRouter;

    private final QueryRouter localAiQueryRouter;

    public final ContentInjector contentInjector;

    private final ChatMemoryProperties chatMemoryProperties;

    private final Skills skills;

    @Bean
    public WebAiService webAiService() {

        //1.加载 prompt 文件并拼接 Skills 清单
        String systemMessage = buildSystemMessage("prompt/web-service.markdown");

        //2.构建 WebAiService
        return AiServices.builder(WebAiService.class)
                .chatModel(model)
                .streamingChatModel(streamingModel)
                .systemMessageProvider(memoryId -> systemMessage)
                .chatMemoryProvider(memoryId -> TokenWindowChatMemory.builder()
                        .id(memoryId)
                        .maxTokens(chatMemoryProperties.getMaxTokens(), new OpenAiTokenCountEstimator(chatMemoryProperties.getTokenEstimatorModel()))
                        .chatMemoryStore(chatMemoryStore)
                        .build())
                .toolProviders(toolProvider, skills.toolProvider())
                .retrievalAugmentor(DefaultRetrievalAugmentor.builder()
                        .queryTransformer(queryTransformer)
                        .queryRouter(webAiQueryRouter)
                        .build())
                .build();
    }

    @Bean
    public LocalAiService localAiService() {

        //1.加载 prompt 文件并拼接 Skills 清单
        String systemMessage = buildSystemMessage("prompt/local-service.markdown");

        //2.构建 LocalAiService
        return AiServices.builder(LocalAiService.class)
                .chatModel(model)
                .streamingChatModel(streamingModel)
                .systemMessageProvider(memoryId -> systemMessage)
                .chatMemoryProvider(memoryId -> TokenWindowChatMemory.builder()
                        .id(memoryId)
                        .maxTokens(chatMemoryProperties.getMaxTokens(), new OpenAiTokenCountEstimator(chatMemoryProperties.getTokenEstimatorModel()))
                        .chatMemoryStore(chatMemoryStore)
                        .build())
                .toolProviders(toolProvider, skills.toolProvider())
                .retrievalAugmentor(DefaultRetrievalAugmentor.builder()
                        .queryTransformer(queryTransformer)
                        .queryRouter(localAiQueryRouter)
                        .contentInjector(contentInjector)
                        .build())
                .build();
    }

    /**
     * 加载 classpath 下的 prompt 文件,并在末尾拼接可用 Skills 清单
     *
     * @param promptResource classpath 下的 prompt 资源路径
     * @return 完整的 system message
     */
    private String buildSystemMessage(String promptResource) {

        //1.读取 prompt 文件内容 (直接读取为 UTF-8 字符串)
        String promptContent = ResourceUtil.readUtf8Str(promptResource);

        //2.获取 Skills 清单
        String skillsList = skills.formatAvailableSkills();

        //3.拼接并返回
        return promptContent + "\n\n---\n\n"
                + "你拥有以下可按需激活的技能,当用户请求与某个技能相关时,先调用 activate_skill 工具激活它:\n"
                + skillsList;
    }
}
