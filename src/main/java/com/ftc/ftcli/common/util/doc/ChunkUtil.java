package com.ftc.ftcli.common.util.doc;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.ftc.ftcli.common.enums.doc.DocMetaDataKeyEnum;
import com.ftc.ftcli.common.enums.doc.ChunkMetaDataKeyEnum;
import com.ftc.ftcli.entity.embedding.EmbeddingChunkRecordEntity;
import dev.langchain4j.data.segment.TextSegment;

/**
 * @author: 冯铁城 [17615007230@163.com]
 * @date: 2026-07-07 17:47:06
 * @describe: chunk工具类
 */
public class ChunkUtil {

    /**
     * 文档切片转换为EmbeddingChunkRecordEntity
     *
     * @param segment     文档切片
     * @return EmbeddingChunkRecordEntity
     */
    public static EmbeddingChunkRecordEntity chunk2Record(TextSegment segment) {
        return EmbeddingChunkRecordEntity.builder()
                .fileNameMd5(segment.metadata().getString(DocMetaDataKeyEnum.FILE_NAME_MD5.getKey()))
                .chunkIndex(segment.metadata().getInteger(ChunkMetaDataKeyEnum.CHUNK_INDEX.getKey()))
                .chunkContentMd5(segment.metadata().getString(ChunkMetaDataKeyEnum.CHUNK_CONTENT_MD5.getKey()))
                .build();
    }

    /**
     * 获取文档切片内容MD5
     *
     * @param segment 文档切片
     * @return 文档切片内容MD5
     */
    public static String getSegmentTextMD5(TextSegment segment) {

        //1.获取文件内容
        String text = StrUtil.isBlank(segment.text()) ? "" : segment.text().trim();

        //2.获取文件内容MD5，返回
        return DigestUtil.md5Hex(text);
    }
}
