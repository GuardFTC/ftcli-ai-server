package com.ftc.ftcli.ai.tool.executor;

import dev.langchain4j.service.tool.ToolExecutor;
import dev.langchain4j.service.tool.ToolProviderRequest;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-11 20:10:59
 * @describe 工具执行器提供者
 */
public interface IToolExecutor {

    /**
     * 获取工具处理器名称
     *
     * @return 工具处理器名称
     */
    String getName();

    /**
     * 是否匹配 匹配时返回工具
     *
     * @param request 工具提供请求 通过获取用户消息等属性进行匹配判定
     * @return 是否匹配
     */
    boolean isMatch(ToolProviderRequest request);

    /**
     * 获取工具处理器
     *
     * @return 工具处理器
     */
    ToolExecutor getToolExecutor();
}
