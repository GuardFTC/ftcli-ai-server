package com.ftc.ftcli.ai.tool.executor.date;

import cn.hutool.core.date.DateUtil;
import com.ftc.ftcli.ai.tool.executor.IToolExecutor;
import dev.langchain4j.service.tool.ToolExecutor;
import org.springframework.stereotype.Component;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-05-13 10:20:33
 * @describe 获取当前时间
 */
@Component
public class GetNowTimeExecutor implements IToolExecutor {

    @Override
    public String getName() {
        return "getNowTime";
    }

    @Override
    public ToolExecutor getToolExecutor() {
        return (toolExecutionRequest, memoryId) -> DateUtil.now();
    }
}
