package com.ftc.ftcli.common.util.os;

import lombok.extern.slf4j.Slf4j;

/**
 * @author: 冯铁城 [17615007230@163.com]
 * @date: 2026-07-24 10:46:38
 * @describe: 操作系统工具类
 */
@Slf4j
public class OsUtil {

    /**
     * 操作系统后缀
     */
    public static final String OS_SUFFIX = getOsSuffix();

    /**
     * 获取操作系统后缀
     *
     * @return 操作系统后缀
     */
    public static String getOsSuffix() {

        //1.获取系统名称
        String osName = System.getProperty("os.name").toLowerCase();

        //2.定义操作系统后缀
        String osSuffix;

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
        log.info("[OS工具类] 自动检测操作系统: {}, 使用后缀: {}", osName, osSuffix);

        //5.返回
        return osSuffix;
    }
}
