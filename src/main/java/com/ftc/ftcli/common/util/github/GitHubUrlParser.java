package com.ftc.ftcli.common.util.github;

import cn.hutool.core.util.StrUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-09 10:54:20
 * @describe GitHubUrl解析器
 */
public class GitHubUrlParser {

    /**
     * GitHub 文件URL正则表达式
     * 匹配格式: https://github.com/{owner}/{repo}/blob/{branch}/{path}
     */
    private static final String GITHUB_URL_PATTERN = "^https://github\\.com/(?<owner>[^/]+)/(?<repo>[^/]+)/blob/(?<branch>[^/]+)/(?<path>[^?#]+)";

    /**
     * GitHub URL 正则表达式编译器
     */
    private static final Pattern GITHUB_URL_PATTERN_COMPILED = Pattern.compile(GITHUB_URL_PATTERN);

    /**
     * 解析GitHub URL
     *
     * @param url GitHub URL
     * @return GithubUrl详情 {@link GitHubUrlInfo}
     */
    public static GitHubUrlInfo parse(String url) {

        //1.判断URL是否为空
        if (StrUtil.isBlank(url)) {
            return null;
        }

        //2.匹配GitHub URL
        Matcher matcher = GITHUB_URL_PATTERN_COMPILED.matcher(url.trim());

        //3.如果匹配成功，解析结果返回
        if (matcher.find()) {
            return new GitHubUrlInfo(
                    matcher.group("owner"),
                    matcher.group("repo"),
                    matcher.group("branch"),
                    matcher.group("path")
            );
        }

        //4.默认返回
        return null;
    }
}