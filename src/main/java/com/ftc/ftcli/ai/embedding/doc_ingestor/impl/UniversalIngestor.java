package com.ftc.ftcli.ai.embedding.doc_ingestor.impl;

import com.ftc.ftcli.ai.embedding.doc_ingestor.IIngestor;
import com.ftc.ftcli.common.enums.doc.DocIngestorTypeEnum;
import com.ftc.ftcli.properties.embedding.IngestorProperties;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.openai.OpenAiTokenCountEstimator;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-22 14:22:32
 * @describe 通用文档切分器
 */
@Component
public class UniversalIngestor implements IIngestor {

    /**
     * 通用文档切分器（构造时初始化，复用实例避免重复构建 tokenizer）
     */
    private final DocumentSplitter recursiveSplitter;

    /**
     * 构造方法
     *
     * @param ingestorProperties 配置属性
     */
    public UniversalIngestor(IngestorProperties ingestorProperties) {
        this.recursiveSplitter = DocumentSplitters.recursive(
                ingestorProperties.getMaxSegmentSize(),
                ingestorProperties.getOverlap(),
                new OpenAiTokenCountEstimator(ingestorProperties.getTokenEstimatorModel())
        );
    }

    @Override
    public String getDocIngestorType() {
        return DocIngestorTypeEnum.UNIVERSAL.getType();
    }

    @Override
    public List<TextSegment> split(Document document) {
        return recursiveSplitter.split(document);
    }
}
