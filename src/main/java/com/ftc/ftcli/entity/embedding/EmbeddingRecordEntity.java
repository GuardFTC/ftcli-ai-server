package com.ftc.ftcli.entity.embedding;

import lombok.Builder;
import lombok.Data;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-02 20:00:00
 * @describe Embedding文档记录实体
 */
@Data
@Builder
public class EmbeddingRecordEntity {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 文件名（如 api-guide.pdf）
     */
    private String fileName;

    /**
     * 文件源路径（如 /docs/api-guide.pdf）
     */
    private String filePath;

    /**
     * 文件名MD5（用于快速去重判断）
     */
    private String fileNameMd5;

    /**
     * 文件内容MD5（用于判断同名文件内容是否变更）
     */
    private String fileContentMd5;

    /**
     * 首次录入时间
     */
    private String createdAt;

    /**
     * 最近更新时间
     */
    private String updatedAt;
}
