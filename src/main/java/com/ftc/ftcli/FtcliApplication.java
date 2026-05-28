package com.ftc.ftcli;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-05-28 10:26:05
 * @describe 启动类
 */
@SpringBootApplication
public class FtcliApplication {

    public static void main(String[] args) {
        System.exit(SpringApplication.exit(
                SpringApplication.run(FtcliApplication.class, args)
        ));
    }
}
