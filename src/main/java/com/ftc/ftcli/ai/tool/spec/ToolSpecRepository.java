package com.ftc.ftcli.ai.tool.spec;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-02 15:00:00
 * @describe 工具描述数据访问层
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class ToolSpecRepository {

    private final JdbcTemplate jdbcTemplate;

    /**
     * 查询全部工具规格（含参数）
     *
     * @return 工具规格集合
     */
    public List<ToolSpecEntity> findAll() {

        //1.定义SQL
        String sql = "SELECT id, name, description, type FROM tool_spec";

        //2.查询全部工具
        List<Map<String, Object>> toolRows = jdbcTemplate.queryForList(sql);

        //3.判空
        if (CollUtil.isEmpty(toolRows)) {
            return Collections.emptyList();
        }

        //4.遍历构建ToolSpecEntity
        return toolRows.stream().map(row -> {

            //5.获取工具ID
            Long toolId = ((Number) row.get("id")).longValue();

            //6.查询工具参数
            List<ToolSpecParamEntity> params = findParamsByToolId(toolId);

            //7.构建ToolSpecEntity
            return ToolSpecEntity.builder()
                    .name((String) row.get("name"))
                    .description((String) row.get("description"))
                    .type((String) row.get("type"))
                    .params(params)
                    .build();
        }).toList();
    }

    /**
     * 根据工具ID查询工具参数
     *
     * @param toolId 工具ID
     * @return 工具参数集合
     */
    private List<ToolSpecParamEntity> findParamsByToolId(Long toolId) {

        //1.定义SQL
        String sql = "SELECT name, description, required, type, enum_values FROM tool_spec_param WHERE tool_spec_id = ?";

        //2.查询工具参数
        List<Map<String, Object>> paramRows = jdbcTemplate.queryForList(sql, toolId);

        //3.判空
        if (CollUtil.isEmpty(paramRows)) {
            return Collections.emptyList();
        }

        //4.遍历构建ToolSpecParamEntity
        return paramRows.stream().map(paramRow -> {

            //5.解析枚举值
            String enumValuesStr = (String) paramRow.get("enum_values");
            List<String> enumValues = StrUtil.isNotBlank(enumValuesStr)
                    ? Arrays.asList(enumValuesStr.split(","))
                    : null;

            //6.构建ToolSpecParamEntity
            return ToolSpecParamEntity.builder()
                    .name((String) paramRow.get("name"))
                    .description((String) paramRow.get("description"))
                    .required(((Number) paramRow.get("required")).intValue() == 1)
                    .type(ToolParamTypeEnum.valueOf(((String) paramRow.get("type")).toUpperCase()))
                    .enumValues(enumValues)
                    .build();
        }).toList();
    }
}
