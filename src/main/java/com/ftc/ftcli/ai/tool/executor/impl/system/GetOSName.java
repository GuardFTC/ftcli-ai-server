package com.ftc.ftcli.ai.tool.executor.impl.system;

import com.ftc.ftcli.ai.tool.executor.IToolExecutor;
import dev.langchain4j.service.tool.ToolExecutor;
import dev.langchain4j.service.tool.ToolProviderRequest;
import org.springframework.stereotype.Component;

import java.beans.Introspector;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-11 10:28:35
 * @describe 获取系统名称
 */
@Component
public class GetOSName implements IToolExecutor {

    @Override
    public String getName() {
        return Introspector.decapitalize(this.getClass().getSimpleName());
    }

    @Override
    public boolean isMatch(ToolProviderRequest request) {
        return true;
    }

    @Override
    public ToolExecutor getToolExecutor() {
        return (toolExecutionRequest, memoryId) -> System.getProperty("os.name").toLowerCase();
    }
}
