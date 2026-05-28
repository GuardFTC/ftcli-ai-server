package com.ftc.ftcli;

import com.ftc.ftcli.command.RootCommand;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-05-28 10:32:17
 * @describe 应用启动入口，将命令行参数委托给 Picocli 执行
 */
@Component
public class AppRunner implements CommandLineRunner, ExitCodeGenerator {

    /**
     * 根命令
     */
    private final RootCommand rootCommand;

    /**
     * Spring Picocli工厂
     */
    private final CommandLine.IFactory factory;

    /**
     * 退出码
     */
    private int exitCode;

    /**
     * 构造方法
     *
     * @param rootCommand 根命令
     * @param factory     Spring Picocli工厂
     */
    public AppRunner(RootCommand rootCommand, CommandLine.IFactory factory) {
        this.rootCommand = rootCommand;
        this.factory = factory;
    }

    @Override
    public void run(String... args) {
        exitCode = new CommandLine(rootCommand, factory).execute(args);
    }

    @Override
    public int getExitCode() {
        return exitCode;
    }
}
