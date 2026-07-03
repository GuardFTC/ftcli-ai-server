package com.ftc.ftcli.properties.embedding;

import cn.hutool.core.util.StrUtil;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-03 10:35:05
 * @describe 向量存储配置属性类
 */
@Data
@Slf4j
@ConfigurationProperties(prefix = "ai.embedding.store.chroma")
public class StoreProperties {

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

        //1.如果未配置 osSuffix，自动检测操作系统
        if (StrUtil.isBlank(osSuffix)) {

            //2.获取系统名称
            String osName = System.getProperty("os.name").toLowerCase();

            //3.根据系统名称，设置系统后缀
            if (osName.contains("mac") || osName.contains("darwin")) {
                osSuffix = "mac";
            } else if (osName.contains("win")) {
                osSuffix = "windows";
            } else if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) {
                osSuffix = "linux";
            } else {
                osSuffix = "unknown";
            }

            //4.打印日志
            log.info("[Chroma配置] 自动检测操作系统: {}, 使用后缀: {}", osName, osSuffix);
        } else {
            log.info("[Chroma配置] 使用手动配置的操作系统后缀: {}", osSuffix);
        }

        //5.为 database 和 collection 追加后缀（避免重复追加）
        String suffix = "_" + osSuffix;
        if (!database.endsWith(suffix)) {
            database = database + suffix;
        }
        if (!collection.endsWith(suffix)) {
            collection = collection + suffix;
        }

        //6.打印日志
        log.info("[Chroma配置] 最终配置 - database: {}, collection: {}", database, collection);
    }
}
