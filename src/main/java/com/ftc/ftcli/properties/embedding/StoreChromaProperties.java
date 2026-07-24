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
 * @describe Chroma向量存储配置属性类
 */
@Data
@Slf4j
@ConfigurationProperties(prefix = "ai.embedding.store.chroma")
public class StoreChromaProperties {

    /**
     * Chroma服务URL
     */
    private String url;

    /**
     * 租户名称
     */
    private String tenant;

    /**
     * 数据库名称（基础名称，会自动追加操作系统后缀）
     */
    private String database;

    /**
     * 集合名称（基础名称，会自动追加操作系统后缀）
     */
    private String collection;

    /**
     * 操作系统后缀（可选，不配置则自动检测）
     * 可选值: mac, windows, linux 或自定义
     */
    private String osSuffix;

    /**
     * 初始化后自动根据操作系统调整数据库和集合名称
     */
    @PostConstruct
    public void initOsSpecificConfig() {

        //1.定义完整后缀
        String suffix;

        //2.如果未配置 osSuffix，根据操作系统设置后缀，否使用配置的后缀
        if (StrUtil.isBlank(osSuffix)) {
            suffix = StrUtil.UNDERLINE + OsUtil.OS_SUFFIX;
        } else {
            suffix = StrUtil.UNDERLINE + osSuffix;
        }

        //3.打印日志
        log.info("[Chroma配置] 初始化数据库和集合名称后缀:[{}]", suffix);

        //3.拼接数据库后缀
        if (!database.endsWith(suffix)) {
            database = database + suffix;
        }

        //5.拼接集合后缀
        if (!collection.endsWith(suffix)) {
            collection = collection + suffix;
        }

        //6.打印日志
        log.info("[Chroma配置] 最终配置 - database:[{}], collection:[{}]", database, collection);
    }
}
