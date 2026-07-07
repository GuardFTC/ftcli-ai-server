package com.ftc.ftcli.service.embedding;

import com.ftc.ftcli.entity.payload.EmbeddingFileUploadPayload;
import com.ftc.ftcli.entity.result.EmbeddingFileUploadResult;
import com.ftc.ftcli.entity.embedding.EmbeddingRecordEntity;

import java.util.List;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-02 19:32:11
 * @describe AI向量嵌入服务
 */
public interface EmbeddingService {

    /**
     * 获取文档列表
     *
     * @return 文档列表
     */
    List<EmbeddingRecordEntity> getDocs();

    /**
     * 删除文档
     *
     * @param id 文档ID
     */
    void remove(Long id);

    /**
     * 上传文档
     *
     * @param payload 文档上传参数
     * @return 文档上传结果
     */
    EmbeddingFileUploadResult upload(EmbeddingFileUploadPayload payload);
}
