package com.ftc.ftcli.infra.embedding;

import cn.hutool.core.collection.CollUtil;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.EmbeddingStore;
import com.ftc.ftcli.properties.embedding.StoreProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: 冯铁城 [17615007230@163.com]
 * @date: 2026-07-08 19:17:35
 * @describe: 向量数据库操作
 */
@Component
@RequiredArgsConstructor
@EnableConfigurationProperties(StoreProperties.class)
public class VectorRepository {

    private final StoreProperties storeProperties;

    private final EmbeddingModel embeddingModel;

    private final EmbeddingStore<TextSegment> embeddingStore;

    /**
     * 批量向量化并写入向量数据库
     *
     * @param chunks 待写入的chunk列表
     */
    public void batchAddAll(List<TextSegment> chunks) {
        batchAddAll(chunks, storeProperties.getBatchSize());
    }

    /**
     * 批量向量化并写入向量数据库
     *
     * @param chunks    待写入的chunk列表
     * @param batchSize 批次大小
     */
    public void batchAddAll(List<TextSegment> chunks, int batchSize) {

        //1.为空直接返回
        if (CollUtil.isEmpty(chunks)) {
            return;
        }

        //2.定义所有向量列表
        List<Embedding> allEmbeddings = new ArrayList<>();

        //3.按批次向量化
        for (int i = 0; i < chunks.size(); i += batchSize) {

            //4.获取当前批次的chunk列表
            List<TextSegment> batch = chunks.subList(i, Math.min(i + batchSize, chunks.size()));

            //5.向量化当前批次
            Response<List<Embedding>> response = embeddingModel.embedAll(batch);

            //6.将当前批次向量写入列表
            allEmbeddings.addAll(response.content());
        }

        //7.写入向量数据库
        embeddingStore.addAll(allEmbeddings, chunks);
    }
}
