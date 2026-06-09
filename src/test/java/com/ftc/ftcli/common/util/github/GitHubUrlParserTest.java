package com.ftc.ftcli.common.util.github;

import cn.hutool.core.lang.Assert;
import org.junit.jupiter.api.Test;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-09 23:15:00
 * @describe GitHubUrlParser 核心逻辑路径单元测试 (基于 Hutool 断言)
 */
class GitHubUrlParserTest {

    @Test
    void testParse_SuccessPaths() {

        //1.路径 1：最基础标准的 GitHub 文件 URL
        {
            String url = "https://github.com/langchain4j/langchain4j/blob/main/pom.xml";
            GitHubUrlInfo info = GitHubUrlParser.parse(url);

            Assert.notNull(info, "解析结果不应为空");
            Assert.equals("langchain4j", info.getOwner());
            Assert.equals("langchain4j", info.getRepo());
            Assert.equals("main", info.getBranchOrTag());
            Assert.equals("pom.xml", info.getFilePath());
        }

        //2.路径 2：多级深层目录 & 带有 URL 尾部行号锚点 (#Lxx)
        {
            String url = "https://github.com/GuardFTC/ftc-cli/blob/master/src/main/go/main.go#L15-L30";
            GitHubUrlInfo info = GitHubUrlParser.parse(url);

            Assert.notNull(info);
            Assert.equals("GuardFTC", info.getOwner());
            Assert.equals("ftc-cli", info.getRepo());
            Assert.equals("master", info.getBranchOrTag());
            // 验证正则是否完美清洗掉了 #L15-L30
            Assert.equals("src/main/go/main.go", info.getFilePath());
        }

        //3.路径 3：分支位置是一个具体的 Tag 版本号 & 带有 URL 查询参数 (?xxx)
        {
            String url = "https://github.com/langchain4j/langchain4j/blob/1.16.0/code-execution-engines/pom.xml?w=1&auth=none";
            GitHubUrlInfo info = GitHubUrlParser.parse(url);

            Assert.notNull(info);
            Assert.equals("langchain4j", info.getOwner());
            Assert.equals("langchain4j", info.getRepo());
            Assert.equals("1.16.0", info.getBranchOrTag());
            // 验证正则是否完美清洗掉了 ?w=1&auth=none
            Assert.equals("code-execution-engines/pom.xml", info.getFilePath());
        }

        //4.路径 4：首尾带有空格的非标准输入（验证 url.trim() 是否生效）
        {
            String url = "   https://github.com/langchain4j/langchain4j/blob/main/README.md   ";
            GitHubUrlInfo info = GitHubUrlParser.parse(url);

            Assert.notNull(info);
            Assert.equals("main", info.getBranchOrTag());
            Assert.equals("README.md", info.getFilePath());
        }
    }

    @Test
    void testParse_FailurePaths() {

        //1.路径 5：输入为 null（进入 StrUtil.isBlank 分支）
        {
            GitHubUrlInfo info = GitHubUrlParser.parse(null);
            Assert.isNull(info, "输入为 null 时应返回 null");
        }

        //2.路径 6：输入为空字符串或纯空格（进入 StrUtil.isBlank 分支）
        {
            GitHubUrlInfo infoEmpty = GitHubUrlParser.parse("");
            GitHubUrlInfo infoBlank = GitHubUrlParser.parse("     ");
            Assert.isNull(infoEmpty, "输入为空字符串时应返回 null");
            Assert.isNull(infoBlank, "输入为纯空格时应返回 null");
        }

        //3.路径 7：非 GitHub 域名的非法 URL（正则匹配失败）
        {
            String url = "https://gitee.com/langchain4j/langchain4j/blob/main/pom.xml";
            GitHubUrlInfo info = GitHubUrlParser.parse(url);
            Assert.isNull(info, "非GitHub域名应返回 null");
        }

        //4.路径 8：是 GitHub 链接，但属于非 blob 页面（例如仓库主页，没有文件路径，正则匹配失败）
        {
            String url = "https://github.com/langchain4j/langchain4j";
            GitHubUrlInfo info = GitHubUrlParser.parse(url);
            Assert.isNull(info, "非具体文件视角链接应返回 null");
        }

        //5.路径 9：格式残缺的 blob 链接（缺少具体的 branch 或 path 捕获组，正则匹配失败）
        {
            String url = "https://github.com/langchain4j/langchain4j/blob/main/";
            GitHubUrlInfo info = GitHubUrlParser.parse(url);
            Assert.isNull(info, "格式残缺的链接应返回 null");
        }
    }
}