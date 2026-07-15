package com.ftc.ftcli.service.embedding.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.ftc.ftcli.ai.embedding.doc_loader.DocLoaderFactory;
import com.ftc.ftcli.ai.embedding.doc_loader.IDocLoader;
import com.ftc.ftcli.common.enums.doc.DocLoaderEnum;
import com.ftc.ftcli.common.enums.doc.DocMetaDataKeyEnum;
import com.ftc.ftcli.common.payload.EmbeddingFileUploadPayload;
import com.ftc.ftcli.common.result.EmbeddingFileUploadResult;
import com.ftc.ftcli.common.util.embedding.VectorUtil;
import com.ftc.ftcli.entity.embedding.EmbeddingRecordEntity;
import com.ftc.ftcli.infra.sqlite.repository.EmbeddingRecordRepository;
import com.ftc.ftcli.service.embedding.EmbeddingRecordService;
import com.ftc.ftcli.service.embedding.EmbeddingService;
import com.ftc.ftcli.service.embedding.EmbeddingUploadService;
import dev.langchain4j.data.document.Document;
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

    private final VectorUtil vectorUtil;

    private final EmbeddingRecordRepository recordRepository;

    private final EmbeddingRecordService recordService;

    private final EmbeddingUploadService embeddingUploadService;

    @Override
    public List<EmbeddingRecordEntity> getDocs() {
        return recordRepository.findAll();
    }

    @Override
    public void remove(Long id) {

        //1.查询文档记录
        EmbeddingRecordEntity docRecord = recordRepository.findById(id);
        if (null == docRecord) {
            log.error("[AI] 删除文档 文档不存在:[{}]", id);
            return;
        }

        //2.删除向量数据库向量
        Filter filter = metadataKey(DocMetaDataKeyEnum.FILE_NAME_MD5.getKey()).isEqualTo(docRecord.getFileNameMd5());
        vectorUtil.removeAll(filter);

        //3.原子删除文档记录及其关联Chunk记录
        recordService.removeRecords(id, docRecord.getFileNameMd5());
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

        //6.新增文档，写入文档记录，chunk记录
        List<String> newFiles = embeddingUploadService.addDocs(newDocsMap);

        //7.获取已存在文档
        Map<String, Document> existDocsMap = partitionedDocsMap.getOrDefault(true, Map.of());
        log.info("[AI] 新增文档 已存在文档数量:[{}]", existDocsMap.size());

        //8.更新文档，更新文档记录，chunk记录
        List<String> updateFiles = embeddingUploadService.updateDocs(existDocsMap, uploadDocRecordMap);

        //9.构建结果返回
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
        Set<EmbeddingRecordEntity> uploadDocRecords = recordRepository.findAllByMd5(uploadDocNameMD5Set);

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
}
