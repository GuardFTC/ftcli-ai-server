package com.ftc.ftcli.config.ai.rag;

import com.ftc.ftcli.common.enums.doc.DocMetaDataKeyEnum;
import dev.langchain4j.rag.content.injector.ContentInjector;
import dev.langchain4j.rag.content.injector.DefaultContentInjector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static java.util.Arrays.asList;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-17 10:52:13
 * @describe 内容注入器配置
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class ContentInjectorConfig {

    @Bean
    public ContentInjector contentInjector() {
        return DefaultContentInjector.builder()
                .metadataKeysToInclude(asList(
                        DocMetaDataKeyEnum.ABSOLUTE_DIRECTORY_PATH.getKey(),
                        DocMetaDataKeyEnum.FILE_NAME.getKey(),
                        DocMetaDataKeyEnum.FULL_PATH.getKey()
                ))
                .build();
    }
}
