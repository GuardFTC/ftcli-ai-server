package com.ftc.ftcli.service.embedding.impl;

import cn.hutool.core.collection.CollUtil;
import com.ftc.ftcli.common.enums.doc.DocMetaDataKeyEnum;
import com.ftc.ftcli.common.enums.doc.SegmentMetaDataKeyEnum;
import com.ftc.ftcli.common.util.doc.DocUtil;
import com.ftc.ftcli.common.util.doc.SegmentUtil;
import com.ftc.ftcli.entity.embedding.EmbeddingChunkRecordEntity;
import com.ftc.ftcli.entity.embedding.EmbeddingRecordEntity;
import com.ftc.ftcli.infra.sqlite.repository.EmbeddingChunkRecordRepository;
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
import java.util.Set;
import java.util.stream.Collectors;

import static dev.langchain4j.store.embedding.filter.Filter.and;
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

    private final EmbeddingChunkRecordRepository chunkRecordRepository;

    @Override
    public List<String> addDocs(Map<String, Document> newDocsMap) {

        //1.为空直接返回
        if (CollUtil.isEmpty(newDocsMap)) {
            return List.of();
        }

        //2.解析出新增文档列表
        List<Document> newDocs = newDocsMap.values().stream().toList();

        //3.切分新增文档
        List<TextSegment> newChunks = splitDocs(newDocs);

        //4.将文档chunk列表解析为Chunk记录列表
        List<EmbeddingChunkRecordEntity> newChunkRecords = newChunks.stream().map(SegmentUtil::chunk2Record).toList();

        //5.将新增文档列表 转换为 新增文档记录列表
        List<EmbeddingRecordEntity> newDocRecords = newDocsMap.entrySet().stream()
                .map(DocUtil::doc2Record)
                .toList();

        //6.解析出新增文件路径列表
        List<String> newFiles = newDocRecords.stream()
                .map(EmbeddingRecordEntity::getFullPath)
                .toList();

        //7.进行向量写入
        try {

            //8.先删除该文档的向量，避免数据库写入失败导致的孤儿向量，确保幂等
            Filter filter = metadataKey(DocMetaDataKeyEnum.FILE_NAME_MD5.getKey()).isIn(newDocsMap.keySet());
            embeddingStore.removeAll(filter);

            //9.向量化
            Response<List<Embedding>> embeddingsResponse = embeddingModel.embedAll(newChunks);

            //10.写入向量数据库
            embeddingStore.addAll(embeddingsResponse.content(), newChunks);

            //11.原子保存文档记录及Chunk记录
            embeddingRecordStore.saveRecords(newDocRecords, newChunkRecords);
        } catch (Exception e) {
            log.error("[AI] 新增文档 向量写入失败，本次不写入文档记录，可重新上传重试。文件:[{}]", newFiles, e);
            throw e;
        }

        //12.解析出新增文件列表，返回
        return newFiles;
    }

    @Override
    public List<String> updateDocs(Map<String, Document> existDocsMap, Map<String, EmbeddingRecordEntity> uploadDocRecordMap) {

        //1.过滤出文档内容发生变更的文档名称MD5
        Set<String> updateDocsNameMD5Set = existDocsMap.keySet()
                .stream()
                .filter(key -> isDocContentChange(existDocsMap, uploadDocRecordMap, key))
                .collect(Collectors.toSet());
        log.info("[AI] 新增文档 内容更新文档数量:[{}]", updateDocsNameMD5Set.size());

        //2.为空直接返回
        if (CollUtil.isEmpty(updateDocsNameMD5Set)) {
            return List.of();
        }

        //3.过滤出文档内容发生更新的文档
        List<Document> updateDocs = existDocsMap.keySet().stream()
                .filter(updateDocsNameMD5Set::contains)
                .map(existDocsMap::get)
                .toList();

        //4.切分更新文档
        List<TextSegment> updateChunks = splitDocs(updateDocs);

        //5.将文档chunk列表解析为chunk记录列表
        List<EmbeddingChunkRecordEntity> updateChunkRecords = updateChunks.stream().map(SegmentUtil::chunk2Record).toList();

        //6.获取最终更新的chunk列表
        List<TextSegment> finalUpdateChunks = getFinalUpdateChunks(updateDocsNameMD5Set, updateChunks);
        log.info("[AI] 新增文档 内容更新文档chunk数量:[{}]", finalUpdateChunks.size());

        //7.为空直接返回
        if (CollUtil.isEmpty(finalUpdateChunks)) {
            return List.of();
        }

        //8.过滤出文档内容发生更新的文档记录
        List<EmbeddingRecordEntity> updateDocRecords = uploadDocRecordMap.keySet().stream()
                .filter(updateDocsNameMD5Set::contains)
                .map(uploadDocRecordMap::get)
                .toList();

        //9.解析出更新文件列表
        List<String> updateFiles = updateDocRecords.stream()
                .map(EmbeddingRecordEntity::getFullPath)
                .toList();

        //10.进行向量写入
        try {

            //11.构建删除条件
            Filter filter = buildDelteChunkFilter(finalUpdateChunks);

            //12.先删除该文档的向量，避免数据库写入失败导致的孤儿向量，确保幂等
            embeddingStore.removeAll(filter);

            //13.向量化
            Response<List<Embedding>> embeddingsResponse = embeddingModel.embedAll(finalUpdateChunks);

            //14.写入向量数据库
            embeddingStore.addAll(embeddingsResponse.content(), finalUpdateChunks);

            //15.原子性更新文档记录以及文档chunk记录
            embeddingRecordStore.updateRecords(updateDocRecords, updateChunkRecords, updateDocsNameMD5Set);
        } catch (Exception e) {
            log.error("[AI] 新增文档 向量更新失败，本次不更新文档记录，可重新上传重试。文件:[{}]", updateFiles, e);
            throw e;
        }

        //16.返回
        return updateFiles;
    }

    /**
     * 切分文档
     *
     * @param docs 文档列表
     * @return 文档chunk列表
     */
    private List<TextSegment> splitDocs(List<Document> docs) {

        //1.定义chunk列表
        List<TextSegment> allSegments = new ArrayList<>();

        //2.循环切分文档
        for (Document doc : docs) {

            //3.切分文档
            List<TextSegment> segments = documentSplitter.split(doc);

            //4.循环为每个chunk添加元数据
            int i = 0;
            for (TextSegment segment : segments) {

                //5.写入文档切片索引、文档切片内容MD5 元数据
                segment.metadata().put(SegmentMetaDataKeyEnum.CHUNK_INDEX.getKey(), i++);
                segment.metadata().put(SegmentMetaDataKeyEnum.CHUNK_CONTENT_MD5.getKey(), SegmentUtil.getSegmentTextMD5(segment));

                //6.添加chunk到文档chunk列表
                allSegments.add(segment);
            }
        }

        //7.返回文档chunk列表
        return allSegments;
    }

    /**
     * 判断文档内容是否发生变更
     *
     * @param existDocsMap       存在文档Map
     * @param uploadDocRecordMap 上传文档记录Map
     * @param docNameMD5         文档名MD5
     * @return true-变更，false-未变更
     */
    private static boolean isDocContentChange(Map<String, Document> existDocsMap, Map<String, EmbeddingRecordEntity> uploadDocRecordMap, String docNameMD5) {

        //1.获取文档以及文档记录
        Document doc = existDocsMap.get(docNameMD5);
        EmbeddingRecordEntity docRecord = uploadDocRecordMap.get(docNameMD5);

        //2.判断文档内容是否发生变更
        if (DocUtil.isDocContentChange(doc, docRecord)) {

            //3.变更记录表MD5
            docRecord.setFileContentMd5(DocUtil.getFileContentMD5(doc));

            //4.返回true
            return true;
        } else {
            return false;
        }
    }

    /**
     * 获取更新的chunk列表
     *
     * @param updateDocsNameMD5Set 更新的文档名MD5集合
     * @param chunks               chunk列表
     * @return 更新的chunk列表
     */
    private List<TextSegment> getFinalUpdateChunks(Set<String> updateDocsNameMD5Set, List<TextSegment> chunks) {

        //1.定义最终更新chunk集合
        List<TextSegment> updateChunks = new ArrayList<>();

        //2.查找更新文档对应的chunk记录
        List<EmbeddingChunkRecordEntity> chunkRecords = chunkRecordRepository.findAllByFileNameMd5In(updateDocsNameMD5Set);

        //3.将chunk记录解析为 文件名MD5->chunk索引->chunk Map
        Map<String, Map<Integer, EmbeddingChunkRecordEntity>> chunkRecordMap = chunkRecords.stream()
                .collect(Collectors.groupingBy(
                        EmbeddingChunkRecordEntity::getFileNameMd5,
                        Collectors.toMap(
                                EmbeddingChunkRecordEntity::getChunkIndex,
                                chunk -> chunk
                        )
                ));

        //4.循环chunk列表
        for (TextSegment chunk : chunks) {

            //5.获取文件名MD5，chunk索引，以及chunk内容MD5
            String fileNameMd5 = chunk.metadata().getString(DocMetaDataKeyEnum.FILE_NAME_MD5.getKey());
            Integer chunkIndex = chunk.metadata().getInteger(SegmentMetaDataKeyEnum.CHUNK_INDEX.getKey());
            String chunkContentMd5 = chunk.metadata().getString(SegmentMetaDataKeyEnum.CHUNK_CONTENT_MD5.getKey());

            //6.获取对应的chunk记录
            Map<Integer, EmbeddingChunkRecordEntity> indexMap = chunkRecordMap.getOrDefault(fileNameMd5, Map.of());
            EmbeddingChunkRecordEntity chunkRecord = indexMap.get(chunkIndex);

            //7.如果为空，直接存入结果更新列表
            if (chunkRecord == null) {
                updateChunks.add(chunk);
                continue;
            }

            //8.不为空，比较chunk内容MD5是否一致，不一致，存入结果更新列表
            if (!chunkRecord.getChunkContentMd5().equals(chunkContentMd5)) {
                updateChunks.add(chunk);
            }
        }

        //9.返回更新的chunk列表
        return updateChunks;
    }

    /**
     * 构建删除chunk的过滤条件
     *
     * @param finalUpdateChunks 最终更新的chunk列表
     * @return 删除chunk的过滤条件
     */
    private Filter buildDelteChunkFilter(List<TextSegment> finalUpdateChunks) {

        //1.定义文件过滤条件列表
        List<Filter> fileFilters = new ArrayList<>();

        //2.按照文件名MD5和chunk索引分组
        Map<String, List<Integer>> groupedMap = finalUpdateChunks.stream()
                .collect(Collectors.groupingBy(
                        segment -> segment.metadata().getString(DocMetaDataKeyEnum.FILE_NAME_MD5.getKey()),
                        Collectors.mapping(
                                segment -> segment.metadata().getInteger(SegmentMetaDataKeyEnum.CHUNK_INDEX.getKey()),
                                Collectors.toList()
                        )
                ));

        //3.遍历聚合后的 Map，构建每个文件的条件：(file_md5 == 'xxx' AND chunk_index IN (1, 2, 3))
        groupedMap.forEach((fileMd5, chunkIndices) -> {

            //4.构建删除条件
            Filter filter = and(
                    metadataKey(DocMetaDataKeyEnum.FILE_NAME_MD5.getKey()).isEqualTo(fileMd5),
                    metadataKey(SegmentMetaDataKeyEnum.CHUNK_INDEX.getKey()).isIn(chunkIndices)
            );

            //5.添加过滤条件到列表
            fileFilters.add(filter);
        });

        //6.如果只有一个文件的过滤条件，直接返回
        if (fileFilters.size() == 1) {
            return fileFilters.getFirst();
        }

        //7.用OR串联所有文件的过滤树
        return fileFilters.stream().reduce((a, b) -> Filter.or(a, b)).orElseThrow();
    }
}