package com.ftc.ftcli.service.embedding.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.ftc.ftcli.common.enums.doc.DocLoaderEnum;
import com.ftc.ftcli.common.enums.doc.DocMetaDataKeyEnum;
import com.ftc.ftcli.common.util.doc.DocUtil;
import com.ftc.ftcli.common.util.doc.doc_loader.DocLoaderFactory;
import com.ftc.ftcli.common.util.doc.doc_loader.IDocLoader;
import com.ftc.ftcli.entity.embedding.EmbeddingRecordEntity;
import com.ftc.ftcli.entity.payload.EmbeddingFileUploadPayload;
import com.ftc.ftcli.entity.result.EmbeddingFileUploadResult;
import com.ftc.ftcli.infra.sqlite.repository.EmbeddingChunkRecordRepository;
import com.ftc.ftcli.infra.sqlite.repository.EmbeddingRecordRepository;
import com.ftc.ftcli.infra.sqlite.store.EmbeddingRecordStore;
import com.ftc.ftcli.service.embedding.EmbeddingService;
import com.ftc.ftcli.service.embedding.EmbeddingUploadService;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.filter.Filter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static dev.langchain4j.store.embedding.filter.MetadataFilterBuilder.metadataKey;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-02 11:23:55
 * @describe AI向量嵌入Service实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingServiceImpl implements EmbeddingService {

    private final DocumentSplitter documentSplitter;

    private final EmbeddingModel embeddingModel;

    private final EmbeddingStore<TextSegment> embeddingStore;

    private final EmbeddingRecordRepository embeddingRecordRepository;

    private final EmbeddingChunkRecordRepository embeddingChunkRecordRepository;

    private final EmbeddingRecordStore embeddingRecordStore;

    private final EmbeddingUploadService embeddingUploadService;

    @Override
    public List<EmbeddingRecordEntity> getDocs() {
        return embeddingRecordRepository.findAll();
    }

    @Override
    public void remove(Long id) {

        //1.查询文档记录
        EmbeddingRecordEntity docRecord = embeddingRecordRepository.findById(id);
        if (null == docRecord) {
            log.error("[AI] 删除文档 文档不存在:[{}]", id);
            return;
        }

        //2.删除向量数据库向量
        Filter filter = metadataKey(DocMetaDataKeyEnum.FILE_NAME_MD5.getKey()).isEqualTo(docRecord.getFileNameMd5());
        embeddingStore.removeAll(filter);

        //3.原子删除文档记录及其关联Chunk记录
        embeddingRecordStore.removeRecord(id, docRecord.getFileNameMd5());
    }

    @Override
    public EmbeddingFileUploadResult upload(EmbeddingFileUploadPayload payload) {

        //1.获取文档路径
        String path = payload.getPath();
        if (StrUtil.isBlank(path)) {
            log.error("[AI] 新增文档 文档路径不能为空");
            return new EmbeddingFileUploadResult();
        }

        //2.获取上传文档名MD5-文档Map
        Map<String, Document> uploadDocMap = getUploadDocMap(path);
        if (CollUtil.isEmpty(uploadDocMap)) {
            log.error("[AI] 新增文档 文档不存在:[{}]", path);
        } else {
            log.info("[AI] 新增文档 加载文档数量:[{}]", uploadDocMap.size());
        }

        //3.获取已存在文档记录Map
        Map<String, EmbeddingRecordEntity> uploadDocRecordMap = getUploadDocRecordMap(uploadDocMap);

        //4.按是否已存在分组文档
        Map<Boolean, Map<String, Document>> partitionedDocsMap = getPartitionedDocsMap(uploadDocRecordMap, uploadDocMap);

        //5.获取新增文档
        Map<String, Document> newDocsMap = partitionedDocsMap.getOrDefault(false, Map.of());
        log.info("[AI] 新增文档 新增文档数量:[{}]", newDocsMap.size());

        //6.新增文档，写入文档记录
        List<String> newFiles = embeddingUploadService.addDocs(newDocsMap);

        //7.获取已存在文档
        Map<String, Document> existDocsMap = partitionedDocsMap.getOrDefault(true, Map.of());
        log.info("[AI] 新增文档 已存在文档数量:[{}]", existDocsMap.size());

        //10.过滤出文档内容发生更新的文档名称MD5
        Set<String> updateDocsNameSet = existDocsMap.entrySet()
                .stream()
                .filter(entry -> DocUtil.isDocContentChange(entry, uploadDocRecordMap))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        log.info("[AI] 新增文档 内容更新文档数量:[{}]", updateDocsNameSet.size());

        //11.已存在文档，如果文档内容发生更新，写入文档记录
        List<String> updateFiles = updateChangeDocs(updateDocsNameSet, existDocsMap, uploadDocRecordMap);

        //12.构建结果返回
        return new EmbeddingFileUploadResult(newFiles, updateFiles);
    }

    /**
     * 获取上传文档名MD5-文档Map
     *
     * @param path 文件路径/URL
     * @return 上传文档名MD5-文档Map
     */
    private static Map<String, Document> getUploadDocMap(String path) {

        //1.通过path获取文档加载类型
        DocLoaderEnum docLoadEnum = IDocLoader.getTypeByPath(path);

        //2.通过类型获取文档加载器
        IDocLoader docLoader = DocLoaderFactory.getDocLoader(docLoadEnum);

        //3.加载文档
        return docLoader.loadDocs(path);
    }

    /**
     * 获取上传文档记录Map
     *
     * @param uploadDocMap 上传文档名MD5-文档Map
     * @return 上传文档名MD5-文档记录Map
     */
    private Map<String, EmbeddingRecordEntity> getUploadDocRecordMap(Map<String, Document> uploadDocMap) {

        //1.获取上传文档名MD5 Set
        Set<String> uploadDocNameMD5Set = uploadDocMap.keySet();

        //2.根据上传文档名MD5 Set，查询已写入的文档记录
        Set<EmbeddingRecordEntity> uploadDocRecords = embeddingRecordRepository.findAllByMd5(uploadDocNameMD5Set);

        //3.解析为已写入文档名MD5-文档记录 Map，返回
        return uploadDocRecords.stream()
                .collect(Collectors.toMap(
                        EmbeddingRecordEntity::getFileNameMd5,
                        doc -> doc
                ));
    }

    /**
     * 按是否已存在分组文档
     *
     * @param uploadDocRecordMap 上传文档名MD5-文档记录Map
     * @param uploadDocMap       上传文档名MD5-文档Map
     * @return 是否已存在-文档名称MD5-文档Map
     */
    private static Map<Boolean, Map<String, Document>> getPartitionedDocsMap(Map<String, EmbeddingRecordEntity> uploadDocRecordMap, Map<String, Document> uploadDocMap) {

        //1.获取已存在文档记录KeySet
        Set<String> uploadDocNameMD5Set = uploadDocRecordMap.keySet();

        //2.按是否已存在分组文档，返回
        return uploadDocMap.entrySet().stream()
                .collect(Collectors.partitioningBy(
                        entry -> uploadDocNameMD5Set.contains(entry.getKey()),
                        Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)
                ));
    }

    /**
     * 更新内容发生变更的文档
     *
     * @param updateDocsNameSet  文档内容发生更新的文档名称MD5 Set
     * @param existDocsMap       已存在文档Map
     * @param existDocRecordsMap 已存在文档记录Map
     * @return 更新文件列表
     */
    private List<String> updateChangeDocs(Set<String> updateDocsNameSet, Map<String, Document> existDocsMap, Map<String, EmbeddingRecordEntity> existDocRecordsMap) {

        //1.为空直接返回
        if (CollUtil.isEmpty(updateDocsNameSet)) {
            return List.of();
        }

        //2.过滤出文档内容发生更新的文档记录
        List<EmbeddingRecordEntity> updateDocRecords = existDocRecordsMap.keySet().stream()
                .filter(updateDocsNameSet::contains)
                .map(existDocRecordsMap::get)
                .toList();

        //3.解析出更新文件列表，返回
        List<String> updateFiles = updateDocRecords.stream()
                .map(EmbeddingRecordEntity::getFullPath)
                .toList();

        //4.过滤出文档内容发生更新的文档
        List<Document> updateDocs = existDocsMap.keySet().stream()
                .filter(updateDocsNameSet::contains)
                .map(existDocsMap::get)
                .toList();

        //5.先按file_name_md5批量删除旧向量，再写入更新后的向量
        try {
            Filter filter = metadataKey(DocMetaDataKeyEnum.FILE_NAME_MD5.getKey()).isIn(updateDocsNameSet);
            embeddingStore.removeAll(filter);
//            ingestor.ingest(updateDocs);
        } catch (Exception e) {
            log.error("[AI] 新增文档 向量更新失败，本次不更新文档记录，可重新上传重试。文件:[{}]", updateFiles, e);
            throw e;
        }

        //6.向量更新成功后，最后更新文档记录（SQLite作为唯一事实源，失败可靠下次上传自愈）
        embeddingRecordRepository.updateBatch(updateDocRecords);

        //7.解析出更新文件列表，返回
        return updateFiles;
    }
}
