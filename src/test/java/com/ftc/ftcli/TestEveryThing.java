package com.ftc.ftcli;

import cn.hutool.core.io.FileUtil;
import com.ftc.ftcli.ai.embedding.doc_parser.DocParserFactory;
import com.ftc.ftcli.common.util.github.GitHubUrlInfo;
import com.ftc.ftcli.common.util.github.GitHubUrlParser;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.loader.UrlDocumentLoader;
import dev.langchain4j.data.document.loader.github.GitHubDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-04 14:47:23
 * @describe 测试URL文档Loader
 */
@SpringBootTest
public class TestEveryThing {

    @Test
    void testURLLoader() {

        //-------------------------------------URL有类型后缀------------------------------------//
        //1.定义URL
        String url = "https://gitee.com/ztmz/ztmz_pacenote/raw/master/src/ZTMZ.PacenoteTool.WpfGUI/%E6%9B%B4%E6%96%B0%E8%AE%B0%E5%BD%95.txt";

        //2.加载文档
        Document document = UrlDocumentLoader.load(url, new TextDocumentParser());

        //3.输出文档内容
        System.out.println(document.text());
        System.out.println(document.metadata());

        //-------------------------------------URL无类型后缀------------------------------------//
        //4.定义URL
        url = "https://httpbin.org/anything";

        //5.加载文档
        document = UrlDocumentLoader.load(url, new TextDocumentParser());

        //6.输出文档内容
        System.out.println(document.text());
        System.out.println(document.metadata());
    }

    @Test
    void testGithubLoader() {

        //1.定义GitHub文件URL
//        String url = "https://github.com/GuardFTC/ftc-cli/blob/master/README.md";
        String url = "https://github.com/langchain4j/langchain4j/blob/main/document-loaders/langchain4j-document-loader-github/src/test/java/dev/langchain4j/data/document/loader/github/GitHubDocumentLoaderIT.java";

        //2.定义githubToken
        String githubToken = "xxx";

        //3.定义GitHubDocLoader
        GitHubDocumentLoader loader = GitHubDocumentLoader.builder()
                .gitHubToken(githubToken)
                .build();

        //4.解析github信息
        GitHubUrlInfo urlInfo = GitHubUrlParser.parse(url);
        if (urlInfo == null) {
            return;
        }

        //5.获取文档后缀
        String fileType = FileUtil.extName(urlInfo.getFilePath());

        //6.获取DocParser
        DocumentParser docParser = DocParserFactory.getDocParser(fileType);

        //7.加载文档
        Document document = loader.loadDocument(
                urlInfo.getOwner(),
                urlInfo.getRepo(),
                urlInfo.getBranchOrTag(),
                urlInfo.getFilePath(),
                docParser
        );

        //8.输出文档内容
        System.out.println(document.text());
        System.out.println(document.metadata());
    }
}