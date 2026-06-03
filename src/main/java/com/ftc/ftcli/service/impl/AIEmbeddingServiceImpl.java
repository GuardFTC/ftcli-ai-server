package com.ftc.ftcli.service.impl;

import com.ftc.ftcli.entity.embedding.EmbeddingFileUploadPayload;
import com.ftc.ftcli.entity.embedding.EmbeddingFileUploadResult;
import com.ftc.ftcli.infra.sqlite.EmbeddingRecordRepository;
import com.ftc.ftcli.service.AIEmbeddingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-02 11:23:55
 * @describe AI向量嵌入Service实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIEmbeddingServiceImpl implements AIEmbeddingService {

    private final EmbeddingRecordRepository embeddingRecordRepository;

    @Override
    public EmbeddingFileUploadResult upload(EmbeddingFileUploadPayload payload) {
        return null;
    }
}
