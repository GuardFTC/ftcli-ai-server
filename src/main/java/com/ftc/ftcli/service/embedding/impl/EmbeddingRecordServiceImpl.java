package com.ftc.ftcli.service.embedding.impl;

import com.ftc.ftcli.entity.embedding.EmbeddingChunkRecordEntity;
import com.ftc.ftcli.entity.embedding.EmbeddingRecordEntity;
import com.ftc.ftcli.infra.sqlite.repository.EmbeddingChunkRecordRepository;
import com.ftc.ftcli.infra.sqlite.repository.EmbeddingRecordRepository;
import com.ftc.ftcli.service.embedding.EmbeddingRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-07-07 00:00:00
 * @describe Embedding文档聚合持久化（跨embedding_record与embedding_chunk_record的原子写操作）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmbeddingRecordServiceImpl implements EmbeddingRecordService {

    private final EmbeddingRecordRepository recordRepository;

    private final EmbeddingChunkRecordRepository chunkRecordRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveRecords(List<EmbeddingRecordEntity> docRecords, List<EmbeddingChunkRecordEntity> chunkRecords) {

        //1.保存文档记录
        recordRepository.saveBatch(docRecords);

        //2.保存文档Chunk记录
        chunkRecordRepository.saveBatch(chunkRecords);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeRecords(Long id, String fileNameMd5) {

        //1.删除文档记录
        recordRepository.deleteById(id);

        //2.删除该文档下全部Chunk记录
        chunkRecordRepository.deleteByFileNameMd5(fileNameMd5);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRecords(List<EmbeddingRecordEntity> updateDocRecords, List<EmbeddingChunkRecordEntity> updateChunkRecords, Set<String> updateDocsNameMD5Set) {

        //1.更新文档记录
        recordRepository.updateBatch(updateDocRecords);

        //2.删除内容发生变更的文档chunk记录
        chunkRecordRepository.deleteByFileNameMd5In(updateDocsNameMD5Set);

        //3.保存更新的文档chunk记录
        chunkRecordRepository.saveBatch(updateChunkRecords);
    }
}
