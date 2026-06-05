package com.ftc.ftcli;

import cn.hutool.core.io.FileUtil;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.UrlDocumentLoader;
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
    void test() {

        //1.定义URL
        String url = "https://yeasy.gitbook.io/prompt_engineering_guide/di-yi-bu-fen-ji-chu-pian/01_introduction/1.1_what_is_prompt_engineering";

        //2.加载文档
        Document document = UrlDocumentLoader.load(url, new TextDocumentParser());

        //3.输出文档内容
        System.out.println(document.text());
        System.out.println(document.metadata());
    }

    @Test
    void openFolder() {
        String folderPath = "C:\\Users\\Administrator\\doc\\临时文件.md";

        // 验证文件夹存在
        if (!FileUtil.exist(folderPath)) {
            System.err.println("❌ 文件夹不存在：" + folderPath);
            return;
        }

        // 获取绝对路径
        String absolutePath = FileUtil.getAbsolutePath(folderPath);

        // 方案2：使用原生 Runtime
        try {
            String osName = System.getProperty("os.name").toLowerCase();

            if (osName.contains("win")) {
                // Windows - 关键：用 cmd /c start
                Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start", absolutePath});
            } else if (osName.contains("mac")) {
                // macOS
                Runtime.getRuntime().exec(new String[]{"open", absolutePath});
            } else {
                // Linux
                Runtime.getRuntime().exec(new String[]{"xdg-open", absolutePath});
            }

            System.out.println("✓ 已打开文件夹：" + absolutePath);
        } catch (Exception e) {
            System.err.println("❌ 无法打开文件夹：" + e.getMessage());
        }
    }
}
