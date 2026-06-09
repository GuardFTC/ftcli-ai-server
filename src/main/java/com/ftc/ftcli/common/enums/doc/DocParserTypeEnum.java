package com.ftc.ftcli.common.enums.doc;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-08 20:22:35
 * @describe 文档解析器类型枚举
 */
@Getter
@AllArgsConstructor
public enum DocParserTypeEnum {

    /**
     * markdown文档
     */
    MARKDOWN("md"),

    /**
     * pdf文档
     */
    PDF("pdf"),

    /**
     * yaml文档
     */
    YAML("yaml"),

    /**
     * yml文档
     */
    YML("yml"),

    /**
     * 默认类型文档文档
     */
    TXT("txt"),

    /**
     * 未知类型文档文档
     */
    UNKNOWN("unknown"),
    ;

    /**
     * 文档类型
     */
    private final String type;

    /**
     * 根据类型字符串获取枚举
     *
     * @param type 类型字符串
     * @return 枚举，未匹配返回DEFAULT
     */
    public static DocParserTypeEnum fromType(String type) {

        //1.遍历枚举，如果匹配，返回
        for (DocParserTypeEnum docParserTypeEnum : values()) {
            if (docParserTypeEnum.getType().equalsIgnoreCase(type)) {
                return docParserTypeEnum;
            }
        }

        //2.未匹配返回未知
        return UNKNOWN;
    }
}
