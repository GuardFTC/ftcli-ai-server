package com.ftc.ftcli.ai.tool.provider;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.service.tool.ToolExecutor;
import dev.langchain4j.service.tool.ToolProviderRequest;

import java.util.Map;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-05-14 10:28:41
 * @describe 工具提供者接口
 */
public interface IToolProvider {

    /**
     * 是否匹配 匹配时返回工具
     *
     * @param request 工具提供请求 通过获取用户消息等属性进行匹配判定
     * @return 是否匹配
     */
    boolean isMatch(ToolProviderRequest request);

    /**
     * 获取工具
     * 可以从入参获取，也可以自行生成
     *
     * @param typeToolSpecToolExecutorMap 工具类型工具规格工具执行器映射
     * @return 工具
     */
    Map<ToolSpecification, ToolExecutor> getTools(Map<String, Map<ToolSpecification, ToolExecutor>> typeToolSpecToolExecutorMap);
}
