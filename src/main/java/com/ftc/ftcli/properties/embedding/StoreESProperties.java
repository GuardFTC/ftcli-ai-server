package com.ftc.ftcli.properties.embedding;

import cn.hutool.core.util.StrUtil;
import com.ftc.ftcli.common.util.os.OsUtil;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-03 10:35:05
 * @describe ElasticSearch向量存储配置属性类
 */
@Data
@Slf4j
@ConfigurationProperties(prefix = "ai.embedding.store.elasticsearch")
public class StoreESProperties {

    /**
     * ElasticSearch服务URL
     */
    private String url;

    /**
     * 索引名称
     */
    private String index;

    /**
     * API-KEY
     */
    private String apiKey;

    /**
     * 初始化后自动根据操作系统调整索引名称
     */
    @PostConstruct
    public void initOsSpecificConfig() {

        //1.根据操作系统，获取索引后缀
        String suffix = StrUtil.UNDERLINE + OsUtil.OS_SUFFIX;

        //2.打印日志
        log.info("[ElasticSearch配置] 初始化索引后缀:[{}]", suffix);

        //3.拼接索引后缀
        if (!index.endsWith(suffix)) {
            index = index + suffix;
        }

        //4.打印日志
        log.info("[ElasticSearch配置] 最终配置 - index:[{}]", index);
    }
}
