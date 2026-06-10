package com.ftc.ftcli.common.util.doc.doc_parser.impl;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import dev.langchain4j.data.document.Document;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

class HtmlDocumentParserTest {

    @Test
    void parse() {

        //1.创建解析器
        HtmlDocumentParser parser = new HtmlDocumentParser();

        //2.读取文档为输入流
        InputStream stream = ResourceUtil.getStream("favorites_2026_6_10.html");

        //3.解析为文档
        Document document = parser.parse(stream);

        //4.验证
        Assert.isTrue(StrUtil.isNotBlank(document.text()));
        System.out.println(document.text());
    }
}