package com.ftc.ftcli.command.ai;

import com.ftc.ftcli.service.GreetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

import java.util.concurrent.Callable;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-05-28 10:30:26
 * @describe AI命令
 */
@Component
@RequiredArgsConstructor
@CommandLine.Command(name = "ai", description = "基于DeepSeek进行AI问答", mixinStandardHelpOptions = true)
public class LocalAICommand implements Callable<Integer> {

    @CommandLine.Option(names = {"-l", "--local"}, description = "基于本地文库进行回答")
    private String localUserMessage;

    @CommandLine.Option(names = {"-w", "--web"}, description = "基于web内容进行回答")
    private String webUserMessage;

    @CommandLine.Option(names = {"-u", "--user"}, description = "用户ID", defaultValue = "ftc")
    private String userId;

    @CommandLine.Option(names = {"-uf", "--update-file"}, description = "更新文库文件路径")
    private String updateFilePath;

    private GreetService greetService;

    @Override
    public Integer call() {
        System.out.println("localUserMessage:" + localUserMessage);
        System.out.println("webUserMessage:" + webUserMessage);
        return 0;
    }
}
