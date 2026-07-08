package com.ftc.ftcli.service.embedding;

import com.ftc.ftcli.entity.embedding.EmbeddingRecordEntity;
import dev.langchain4j.data.document.Document;

import java.util.List;
import java.util.Map;

/**
 * @author: 冯铁城 [17615007230@163.com]
 * @date: 2026-07-07 20:16:48
 * @describe: 向量上传服务
 */
public interface EmbeddingUploadService {

    /**
     * 将新增文档写入向量数据库，并保存文档记录，以及文档Chunk记录
     *
     * @param newDocsMap 新增 文档名称MD5->文档 Map
     * @return 新增文件路径/URL列表
     */
    List<String> addDocs(Map<String, Document> newDocsMap);

    /**
     * 将更新文档写入向量数据库，并更新文档记录，以及文档Chunk记录
     *
     * @param existDocsMap       已存在 文档名称MD5->文档 Map
     * @param uploadDocRecordMap 上传文档记录 Map
     * @return 更新文件路径/URL列表
     */
    List<String> updateDocs(Map<String, Document> existDocsMap, Map<String, EmbeddingRecordEntity> uploadDocRecordMap);
}