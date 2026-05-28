package com.ftc.ftcli.ai.tool.executor;

import dev.langchain4j.service.tool.ToolExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-05-13 10:36:34
 * @describe 工具执行器工厂
 */
@Slf4j
@Component
public class ToolExecutorFactory implements ApplicationContextAware {

    /**
     * 工具执行器缓存
     */
    private static final ConcurrentHashMap<String, ToolExecutor> TOOL_EXECUTOR_MAP = new ConcurrentHashMap<>();

    /**
     * 获取工具执行器
     *
     * @param toolName 工具名称
     * @return 工具执行器
     */
    public static ToolExecutor getToolExecutor(String toolName) {

        //1.获取工具执行器
        ToolExecutor toolExecutor = TOOL_EXECUTOR_MAP.get(toolName);

        //2.判空
        if (null == toolExecutor) {
            log.warn("[工具执行器工厂] 工具执行器不存在：{}", toolName);
        }

        //3.返回
        return toolExecutor;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

        //1.获取全部实现类
        final Map<String, IToolExecutor> beansOfType = applicationContext.getBeansOfType(IToolExecutor.class);

        //2.循环
        for (String className : beansOfType.keySet()) {

            //3.获取实现类
            IToolExecutor executor = beansOfType.get(className);

            //4.封装Map
            TOOL_EXECUTOR_MAP.put(executor.getName(), executor.getToolExecutor());
        }
    }
}
