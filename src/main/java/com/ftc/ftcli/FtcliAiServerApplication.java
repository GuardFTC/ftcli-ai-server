package com.ftc.ftcli;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchClientAutoConfiguration;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-05-28 10:26:05
 * @describe 启动类
 */
@SpringBootApplication(exclude = {
        ElasticsearchClientAutoConfiguration.class,
        ElasticsearchRestClientAutoConfiguration.class
})
public class FtcliAiServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(FtcliAiServerApplication.class, args);
    }
}
