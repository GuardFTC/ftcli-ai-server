package com.ftc.ftcli.ai.tool.spec;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-05-12 20:19:46
 * @describe 工具参数类型枚举
 */
@Getter
@AllArgsConstructor
public enum ToolParamTypeEnum {

    /**
     * 字符串
     */
    STRING("string"),

    /**
     * 整数
     */
    INTEGER("integer"),

    /**
     * 数字
     */
    NUMBER("number"),

    /**
     * 布尔
     */
    BOOLEAN("boolean"),

    /**
     * 枚举
     */
    ENUMS("enums"),
    ;

    /**
     * 参数类型
     */
    private final String type;
}
