package com.ftc.ftcli.entity.embedding;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-07-07 00:00:00
 * @describe Embedding文档Chunk记录实体
 */
@Data
@Builder
public class EmbeddingChunkRecordEntity {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "文件名MD5（关联embedding_record表，用于定位所属文件）")
    private String fileNameMd5;

    @Schema(description = "Chunk在文件中的顺序索引（从0开始）")
    private Integer chunkIndex;

    @Schema(description = "Chunk内容MD5（与file_name_md5联合唯一，防止同一文件下同一Chunk重复录入）")
    private String chunkContentMd5;

    @Schema(description = "首次录入时间")
    private String createdAt;

    @Schema(description = "最近更新时间")
    private String updatedAt;
}
