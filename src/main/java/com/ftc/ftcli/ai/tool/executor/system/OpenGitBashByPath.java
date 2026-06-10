package com.ftc.ftcli.ai.tool.executor.system;

import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson2.JSON;
import com.ftc.ftcli.ai.tool.executor.IToolExecutor;
import dev.langchain4j.service.tool.ToolExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-10 15:11:16
 * @describe 打开GitBash
 */
@Slf4j
@Component
public class OpenGitBashByPath implements IToolExecutor {

    /**
     * Git Bash 真实绝对路径
     */
    private static final String GIT_BASH_PATH = "C:\\Users\\Administrator\\base\\git\\Git\\git-bash.exe";

    @Override
    public String getName() {
        return "openGitBashByPath";
    }

    @Override
    public ToolExecutor getToolExecutor() {
        return (toolExecutionRequest, memoryId) -> {

            //1.从request中解析LLM传入的参数
            Map<String, Object> arguments = JSON.parseObject(toolExecutionRequest.arguments());
            String path = arguments.get("path").toString();

            //2.验证文件存在
            if (!FileUtil.exist(path)) {
                return "文件/文件夹: " + path + " 不存在";
            }

            try {
                //3.构建启动命令
                ProcessBuilder pb = new ProcessBuilder(
                        "cmd", "/c", "start", GIT_BASH_PATH, "--cd=" + path
                );

                //4.运行
                pb.start();

                //5.返回
                return "已打开GitBash: " + path;
            } catch (IOException e) {
                log.error("[AI工具]-打开GitBash失败 路径:[{}]", path, e);
                return "打开GitBash失败:[" + path + "]";
            }
        };
    }
}
