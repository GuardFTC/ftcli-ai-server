package com.ftc.ftcli.infra.sqlite.store;

import com.ftc.ftcli.entity.embedding.EmbeddingChunkRecordEntity;
import com.ftc.ftcli.entity.embedding.EmbeddingRecordEntity;
import com.ftc.ftcli.infra.sqlite.repository.EmbeddingChunkRecordRepository;
import com.ftc.ftcli.infra.sqlite.repository.EmbeddingRecordRepository;
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
public class EmbeddingRecordStore {

    private final EmbeddingRecordRepository recordRepository;

    private final EmbeddingChunkRecordRepository chunkRecordRepository;

    /**
     * 原子保存文档记录及其Chunk记录
     *
     * @param docRecords   待保存的文档记录集合
     * @param chunkRecords 待保存的文档Chunk记录集合
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveRecords(List<EmbeddingRecordEntity> docRecords, List<EmbeddingChunkRecordEntity> chunkRecords) {

        //1.保存文档记录
        recordRepository.saveBatch(docRecords);

        //2.保存文档Chunk记录
        chunkRecordRepository.saveBatch(chunkRecords);
    }

    /**
     * 原子删除文档记录及其关联的全部Chunk记录
     *
     * @param id          文档记录ID
     * @param fileNameMd5 文件名MD5
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeRecords(Long id, String fileNameMd5) {

        //1.删除文档记录
        recordRepository.deleteById(id);

        //2.删除该文档下全部Chunk记录
        chunkRecordRepository.deleteByFileNameMd5(fileNameMd5);
    }

    /**
     * 原子更新文档记录及其关联的Chunk记录
     *
     * @param updateDocRecords     待更新的文档记录集合
     * @param updateChunkRecords   待更新的文档Chunk记录集合
     * @param updateDocsNameMD5Set 待更新的文档记录的文件名MD5集合
     */
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
