package com.ftc.ftcli.common.util.embedding;

import cn.hutool.core.collection.CollUtil;
import com.ftc.ftcli.properties.embedding.StoreProperties;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.filter.Filter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * @author: 冯铁城 [17615007230@163.com]
 * @date: 2026-07-08 19:17:35
 * @describe: 向量数据库操作
 */
@Component
@EnableConfigurationProperties(StoreProperties.class)
public class VectorUtil {

    private final EmbeddingModel embeddingModel;

    private final EmbeddingStore<TextSegment> embeddingStore;

    private final EmbeddingStore<TextSegment> esEmbeddingStore;

    private final StoreProperties storeProperties;

    /**
     * 构造函数
     *
     * @param storeProperties  向量数据库属性
     * @param embeddingModel   向量模型
     * @param embeddingStore   Chroma向量数据库
     * @param esEmbeddingStore ES向量数据库
     */
    public VectorUtil(
            EmbeddingModel embeddingModel,
            @Qualifier("embeddingStore") EmbeddingStore<TextSegment> embeddingStore,
            @Qualifier("esEmbeddingStore") EmbeddingStore<TextSegment> esEmbeddingStore,
            StoreProperties storeProperties
    ) {
        this.embeddingModel = embeddingModel;
        this.embeddingStore = embeddingStore;
        this.esEmbeddingStore = esEmbeddingStore;
        this.storeProperties = storeProperties;
    }

    /**
     * 根据过滤器删除向量数据库中的数据
     *
     * @param filter 过滤器
     */
    public void removeAll(Filter filter) {

        //1.删除ES中的数据
        esEmbeddingStore.removeAll(filter);

        //2.删除Chroma中的数据
        embeddingStore.removeAll(filter);
    }

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

        //2.按批次进行“向量化”与“落库”
        for (int i = 0; i < chunks.size(); i += batchSize) {

            //3.获取当前批次的 chunk 列表
            List<TextSegment> batchChunks = chunks.subList(i, Math.min(i + batchSize, chunks.size()));

            //4.写入ES（仅文本，用于BM25全文检索，不写向量）
            List<Embedding> emptyEmbeddings = Collections.nCopies(batchChunks.size(), Embedding.from(new float[0]));
            esEmbeddingStore.addAll(emptyEmbeddings, batchChunks);

            //5.向量化当前批次
            Response<List<Embedding>> response = embeddingModel.embedAll(batchChunks);

            //6.即刻分批写入向量数据库（Chroma）
            if (response != null && CollUtil.isNotEmpty(response.content())) {
                embeddingStore.addAll(response.content(), batchChunks);
            }
        }
    }
}
