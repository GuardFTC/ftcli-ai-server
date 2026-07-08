package com.ftc.ftcli.service.embedding.impl;

import cn.hutool.core.collection.CollUtil;
import com.ftc.ftcli.common.enums.doc.DocMetaDataKeyEnum;
import com.ftc.ftcli.common.enums.doc.SegmentMetaDataKeyEnum;
import com.ftc.ftcli.common.util.doc.DocUtil;
import com.ftc.ftcli.common.util.doc.SegmentUtil;
import com.ftc.ftcli.entity.embedding.EmbeddingChunkRecordEntity;
import com.ftc.ftcli.entity.embedding.EmbeddingRecordEntity;
import com.ftc.ftcli.infra.sqlite.store.EmbeddingRecordStore;
import com.ftc.ftcli.service.embedding.EmbeddingUploadService;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.filter.Filter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static dev.langchain4j.store.embedding.filter.MetadataFilterBuilder.metadataKey;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-02 11:23:55
 * @describe AI向量嵌入Service实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingUploadServiceImpl implements EmbeddingUploadService {

    private final DocumentSplitter documentSplitter;

    private final EmbeddingModel embeddingModel;

    private final EmbeddingStore<TextSegment> embeddingStore;

    private final EmbeddingRecordStore embeddingRecordStore;

    @Override
    public List<String> addDocs(Map<String, Document> newDocsMap) {

        //1.为空直接返回
        if (CollUtil.isEmpty(newDocsMap)) {
            return List.of();
        }

        //2.解析出新增文档列表
        List<Document> newDocs = newDocsMap.values().stream().toList();

        //3.将新增文档列表 转换为 新增文档记录列表
        List<EmbeddingRecordEntity> newRecords = newDocsMap.entrySet().stream()
                .map(DocUtil::doc2Record)
                .toList();

        //4.解析出新增文件路径列表
        List<String> newFiles = newRecords.stream()
                .map(EmbeddingRecordEntity::getFullPath)
                .toList();

        //5.定义文档chunk列表
        List<TextSegment> allSegments = new ArrayList<>();
        try {

            //6.先删除该文档的向量，避免数据库写入失败导致的孤儿向量，确保幂等
            Filter filter = metadataKey(DocMetaDataKeyEnum.FILE_NAME_MD5.getKey()).isIn(newDocsMap.keySet());
            embeddingStore.removeAll(filter);

            //7.切分新增文档
            splitNewDocs(newDocs, allSegments);

            //8.向量化
            Response<List<Embedding>> embeddingsResponse = embeddingModel.embedAll(allSegments);

            //9.写入向量数据库
            embeddingStore.addAll(embeddingsResponse.content(), allSegments);
        } catch (Exception e) {
            log.error("[AI] 新增文档 向量写入失败，本次不写入文档记录，可重新上传重试。文件:[{}]", newFiles, e);
            throw e;
        }

        //10.将文档chunk列表解析为Chunk记录列表
        List<EmbeddingChunkRecordEntity> chunkRecords = allSegments.stream().map(SegmentUtil::chunk2Record).toList();

        //11.原子保存文档记录及Chunk记录
        embeddingRecordStore.saveRecords(newRecords, chunkRecords);

        //12.解析出新增文件列表，返回
        return newFiles;
    }

    /**
     * 切分新增文档
     *
     * @param newDocs     新增文档列表
     * @param allSegments 文档chunk列表
     */
    private void splitNewDocs(List<Document> newDocs, List<TextSegment> allSegments) {
        for (Document newDoc : newDocs) {

            //1.切分文档
            List<TextSegment> segments = documentSplitter.split(newDoc);

            //2.循环为每个chunk添加元数据
            int i = 0;
            for (TextSegment segment : segments) {

                //3.写入文档切片索引、文档切片内容MD5 元数据
                segment.metadata().put(SegmentMetaDataKeyEnum.CHUNK_INDEX.getKey(), String.valueOf(i++));
                segment.metadata().put(SegmentMetaDataKeyEnum.CHUNK_CONTENT_MD5.getKey(), SegmentUtil.getSegmentTextMD5(segment));

                //4.添加chunk到文档chunk列表
                allSegments.add(segment);
            }
        }
    }
}