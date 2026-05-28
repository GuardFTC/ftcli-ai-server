package com.ftc.ftcli.ai.tool.provider.impl;

import cn.hutool.core.util.StrUtil;
import com.ftc.ftcli.ai.tool.ToolTypeEnum;
import com.ftc.ftcli.ai.tool.provider.IToolProvider;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.service.tool.ToolExecutor;
import dev.langchain4j.service.tool.ToolProviderRequest;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-05-14 10:32:59
 * @describe 时间工具提供者
 */
@Component
public class DateToolProvider implements IToolProvider {

    @Override
    public boolean isMatch(ToolProviderRequest request) {

        //1.获取用户消息
        String userMessage = request.userMessage().singleText();

        //2.为空直接返回false
        if (StrUtil.isBlank(userMessage)) {
            return false;
        }

        //3.包含时间关键字则返回true
        return userMessage.contains("时间") || userMessage.contains("天");
    }

    @Override
    public Map<ToolSpecification, ToolExecutor> getTools(Map<String, Map<ToolSpecification, ToolExecutor>> typeToolSpecToolExecutorMap) {
        return typeToolSpecToolExecutorMap.get(ToolTypeEnum.DATE.getType());
    }
}
