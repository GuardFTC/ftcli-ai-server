package com.ftc.ftcli.ai.tool.provider;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-05-13 10:36:34
 * @describe 工具提供者工厂
 */
@Slf4j
@Component
public class ToolProviderFactory implements ApplicationContextAware {

    /**
     * 工具提供者集合
     */
    private static final CopyOnWriteArrayList<IToolProvider> TOOL_PROVIDER_LIST = new CopyOnWriteArrayList<>();

    /**
     * 获取工具提供者集合
     *
     * @return 工具提供者集合
     */
    public static List<IToolProvider> getToolProviders() {
        return TOOL_PROVIDER_LIST;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        loadToolProviderList(applicationContext);
    }

    /**
     * 加载工具提供者集合
     *
     * @param applicationContext 上下文
     */
    public static void loadToolProviderList(ApplicationContext applicationContext) {

        //1.获取全部实现类
        final Map<String, IToolProvider> beansOfType = applicationContext.getBeansOfType(IToolProvider.class);

        //2.循环
        for (String className : beansOfType.keySet()) {

            //3.获取实现类
            IToolProvider provider = beansOfType.get(className);

            //4.写入集合
            TOOL_PROVIDER_LIST.add(provider);
        }
    }
}
