package com.ftc.ftcli.config.ai.embedding;

import com.ftc.ftcli.common.enums.doc.DocMetaDataKeyEnum;
import com.ftc.ftcli.common.util.doc.doc_parser.impl.HtmlDocumentParser;
import com.ftc.ftcli.properties.embedding.IngestorProperties;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiTokenCountEstimator;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-17 10:42:09
 * @describe 切分器配置
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties({IngestorProperties.class})
public class IngestorConfig {

    private final EmbeddingModel embeddingModel;

    private final EmbeddingStore<TextSegment> embeddingStore;

    private final IngestorProperties ingestorProperties;

    @Bean
    public EmbeddingStoreIngestor ingestor() {

        //1.定义通用文档切分规则
        DocumentSplitter recursiveSplitter = DocumentSplitters.recursive(
                ingestorProperties.getMaxSegmentSize(),
                ingestorProperties.getOverlap(),
                new OpenAiTokenCountEstimator(ingestorProperties.getTokenEstimatorModel())
        );

        //2.定义复合切分器：书签文档按行切分(一条书签一个片段)，其余文档走通用规则
        DocumentSplitter splitter = document -> {

            //3.判断是否为书签文档
            String docType = document.metadata().getString(DocMetaDataKeyEnum.DOC_TYPE.getKey());
            if (HtmlDocumentParser.DOC_TYPE_BOOKMARK.equals(docType)) {
                return splitBookmarkByLine(document);
            }

            //4.非书签文档使用通用切分规则
            return recursiveSplitter.split(document);
        };

        //5.创建入库器，返回
        return EmbeddingStoreIngestor.builder()
                .documentSplitter(splitter)
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();
    }

    /**
     * 书签文档按行切分：一行(一条书签)生成一个独立片段，保证每个向量语义干净
     *
     * @param document 书签文档
     * @return 文档片段集合
     */
    private List<TextSegment> splitBookmarkByLine(Document document) {

        //1.定义片段集合
        List<TextSegment> segments = new ArrayList<>();

        //2.按换行符拆分
        String[] lines = document.text().split("\n");

        //3.遍历每行，非空行生成独立片段(携带文档元数据)
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                segments.add(TextSegment.from(trimmed, document.metadata().copy()));
            }
        }

        //4.返回
        return segments;
    }
}
