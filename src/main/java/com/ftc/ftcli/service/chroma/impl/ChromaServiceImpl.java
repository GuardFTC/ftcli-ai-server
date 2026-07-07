package com.ftc.ftcli.service.chroma.impl;

import cn.hutool.core.util.StrUtil;
import com.ftc.ftcli.entity.embedding.EmbeddingRecordEntity;
import com.ftc.ftcli.infra.chroma.CollectionRepository;
import com.ftc.ftcli.infra.chroma.RecordRepository;
import com.ftc.ftcli.infra.sqlite.repository.EmbeddingRecordRepository;
import com.ftc.ftcli.service.chroma.ChromaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-02 11:23:55
 * @describe Chroma向量数据库服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChromaServiceImpl implements ChromaService {

    private final EmbeddingRecordRepository embeddingRecordRepository;

    private final CollectionRepository chromaCollectionRepository;

    private final RecordRepository chromaRecordRepository;

    @Override
    public int getVectorCount() {

        //1.查询集合ID
        String collectionId = chromaCollectionRepository.getCollectionId();
        if (StrUtil.isBlank(collectionId)) {
            return 0;
        }

        //2.查询向量数量，返回结果
        return chromaCollectionRepository.getVectorCount(collectionId);
    }

    @Override
    public List<Map<String, Object>> getChunks(Long id, int offset, int size) {

        //1.获取集合ID
        String collectionId = chromaCollectionRepository.getCollectionId();
        if (StrUtil.isBlank(collectionId)) {
            return List.of();
        }

        //2.查询文档记录
        EmbeddingRecordEntity docRecord = embeddingRecordRepository.findById(id);
        if (null == docRecord) {
            log.error("[AI] 获取文档片段 文档不存在:[{}]", id);
            return List.of();
        }

        //3.获取文件名MD5
        String fileNameMd5 = docRecord.getFileNameMd5();

        //4.获取文档片段，返回结果
        return chromaRecordRepository.getChunks(collectionId, fileNameMd5, offset, size);
    }

    @Override
    public int getChunkCount(Long id) {

        //1.获取集合ID
        String collectionId = chromaCollectionRepository.getCollectionId();
        if (StrUtil.isBlank(collectionId)) {
            return 0;
        }

        //2.查询文档记录
        EmbeddingRecordEntity docRecord = embeddingRecordRepository.findById(id);
        if (null == docRecord) {
            log.error("[AI] 获取文档片段数 文档不存在:[{}]", id);
            return 0;
        }

        //3.获取文件名MD5
        String fileNameMd5 = docRecord.getFileNameMd5();

        //4.查询片段数，返回
        return chromaRecordRepository.getChunkCount(collectionId, fileNameMd5);
    }
}
