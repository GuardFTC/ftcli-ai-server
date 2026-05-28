package com.ftc.ftcli.ai.tool.spec;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-05-12 20:17:43
 * @describe AI工具参数
 */
@Data
@Builder
public class ToolSpecParamEntity {

    /**
     * 参数名称
     */
    private String name;

    /**
     * 参数描述
     */
    private String description;

    /**
     * 是否必填
     */
    private boolean required;

    /**
     * 参数类型
     */
    private ToolParamTypeEnum type;

    /**
     * 参数枚举值：仅当参数类型为枚举时有效
     */
    private List<String> enumValues;
}
