package com.ftc.ftcli.service.ai;

import com.ftc.ftcli.entity.payload.ChatPayload;
import reactor.core.publisher.Flux;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-02 11:22:47
 * @describe AI问答Service
 */
public interface AiChatService {

    /**
     * 获取会话ID
     *
     * @return 会话ID
     */
    String getChatId();

    /**
     * 聊天
     *
     * @param payload 聊天参数
     * @return 响应结果
     */
    String chat(ChatPayload payload);

    /**
     * 流式聊天
     *
     * @param payload 聊天参数
     * @return 响应结果
     */
    Flux<String> chatStream(ChatPayload payload);
}
