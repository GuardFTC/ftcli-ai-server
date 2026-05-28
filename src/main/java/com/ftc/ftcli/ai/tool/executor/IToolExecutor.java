package com.ftc.ftcli.ai.tool.executor;

import dev.langchain4j.service.tool.ToolExecutor;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-05-13 10:18:15
 * @describe 工具执行器接口
 */
public interface IToolExecutor {

    /**
     * 获取工具处理器名称
     *
     * @return 工具处理器名称
     */
    String getName();

    /**
     * 获取工具处理器
     *
     * @return 工具处理器
     */
    ToolExecutor getToolExecutor();
}
