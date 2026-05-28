package com.ftc.ftcli.ai.tool.spec;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-05-12 20:23:16
 * @describe AI工具
 */
@Data
@Builder
public class ToolSpecEntity {

    /**
     * 工具名称
     */
    private String name;

    /**
     * 工具描述
     */
    private String description;

    /**
     * 工具类型
     */
    private String type;

    /**
     * 工具参数
     */
    private List<ToolSpecParamEntity> params;
}
