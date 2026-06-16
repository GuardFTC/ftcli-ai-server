package com.ftc.ftcli.service.impl;

import cn.hutool.core.util.IdUtil;
import com.ftc.ftcli.ai.service.AiServiceHolder;
import com.ftc.ftcli.common.util.ai.AiTraceLog;
import com.ftc.ftcli.entity.payload.ChatPayload;
import com.ftc.ftcli.service.AiChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-02 11:23:55
 * @describe AI问答Service实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIChatServiceImpl implements AiChatService {

    private final AiServiceHolder aiServiceHolder;

    @Override
    public String getChatId() {
        return IdUtil.fastSimpleUUID();
    }

    @Override
    public String chat(ChatPayload payload) {

        //1.获取开始时间戳
        long start = System.currentTimeMillis();

        //2.基于是否为本地问答进行LLM链路调用
        String result;
        if (payload.getIsLocal()) {
            result = aiServiceHolder.getLocalAiService().chat(payload.getChatId(), payload.getUserMessage()).content();
        } else {
            result = aiServiceHolder.getWebAiService().chat(payload.getChatId(), payload.getUserMessage()).content();
        }

        //3.打印总耗时
        AiTraceLog.logTotalTime(start);

        //4.返回结果
        return result;
    }

    @Override
    public Flux<String> chatStream(ChatPayload payload) {

        //1.获取开始时间戳
        long start = System.currentTimeMillis();

        //2.基于是否为本地问答进行LLM链路调用
        Flux<String> flux;
        if (payload.getIsLocal()) {
            flux = aiServiceHolder.getLocalAiService().chatStream(payload.getChatId(), payload.getUserMessage());
        } else {
            flux = aiServiceHolder.getWebAiService().chatStream(payload.getChatId(), payload.getUserMessage());
        }

        //3.打印总耗时,并返回结果
        return flux.doOnComplete(() -> AiTraceLog.logTotalTime(start));
    }
}
