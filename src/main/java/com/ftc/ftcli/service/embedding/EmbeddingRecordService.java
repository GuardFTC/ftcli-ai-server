package com.ftc.ftcli.service.embedding;

import com.ftc.ftcli.entity.embedding.EmbeddingChunkRecordEntity;
import com.ftc.ftcli.entity.embedding.EmbeddingRecordEntity;

import java.util.List;
import java.util.Set;

/**
 * @author: 冯铁城 [17615007230@163.com]
 * @date: 2026-07-09 19:47:57
 * @describe: Embedding记录服务
 */
public interface EmbeddingRecordService {

    /**
     * 原子保存文档记录及其Chunk记录
     *
     * @param docRecords   待保存的文档记录集合
     * @param chunkRecords 待保存的文档Chunk记录集合
     */
    void saveRecords(List<EmbeddingRecordEntity> docRecords, List<EmbeddingChunkRecordEntity> chunkRecords);

    /**
     * 原子删除文档记录及其关联的全部Chunk记录
     *
     * @param id          文档记录ID
     * @param fileNameMd5 文件名MD5
     */
    void removeRecords(Long id, String fileNameMd5);

    /**
     * 原子更新文档记录及其关联的Chunk记录
     *
     * @param updateDocRecords     待更新的文档记录集合
     * @param updateChunkRecords   待更新的文档Chunk记录集合
     * @param updateDocsNameMD5Set 待更新的文档记录的文件名MD5集合
     */
    void updateRecords(List<EmbeddingRecordEntity> updateDocRecords, List<EmbeddingChunkRecordEntity> updateChunkRecords, Set<String> updateDocsNameMD5Set);
}
