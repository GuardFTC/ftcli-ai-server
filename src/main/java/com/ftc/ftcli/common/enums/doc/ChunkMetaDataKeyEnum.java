package com.ftc.ftcli.common.enums.doc;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-09 11:38:03
 * @describe 文档切片元数据Key枚举
 */
@Getter
@AllArgsConstructor
public enum ChunkMetaDataKeyEnum {

    /**
     * 文档切片索引
     */
    CHUNK_INDEX("chunk_index"),

    /**
     * 文档切片内容MD5
     */
    CHUNK_CONTENT_MD5("chunk_content_md5"),
    ;

    /**
     * 文档切片元数据Key
     */
    private final String key;
}
