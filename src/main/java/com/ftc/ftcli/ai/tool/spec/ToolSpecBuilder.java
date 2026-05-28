package com.ftc.ftcli.ai.tool.spec;

import cn.hutool.core.collection.CollUtil;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-05-12 20:24:46
 * @describe AI工具 工具类
 */
@Slf4j
public class ToolSpecBuilder {

    /**
     * 构建AI工具集合
     *
     * @param toolSpecEntities AI工具构建参数集合
     * @return AI工具集合
     */
    public static List<ToolSpecification> buildToolSpecifications(List<ToolSpecEntity> toolSpecEntities) {
        return toolSpecEntities.stream().map(ToolSpecBuilder::buildToolSpecification).toList();
    }

    /**
     * 构建AI工具
     *
     * @param toolSpecEntity AI工具构建参数
     * @return AI工具
     */
    public static ToolSpecification buildToolSpecification(ToolSpecEntity toolSpecEntity) {

        //1.定义builder
        ToolSpecification.Builder builder = ToolSpecification.builder();

        //2.设置工具名称和描述
        builder.name(toolSpecEntity.getName());
        builder.description(toolSpecEntity.getDescription());

        //3.获取参数
        List<ToolSpecParamEntity> toolSpecParams = toolSpecEntity.getParams();

        //4.如果参数不为空
        if(CollUtil.isNotEmpty(toolSpecParams)){

            //5.构建AI工具参数
            JsonObjectSchema jsonObjectSchema = buildJsonObjectSchema(toolSpecParams);

            //6.设置工具参数
            builder.parameters(jsonObjectSchema);
        }

        //7.构建工具，返回
        return builder.build();
    }

    /**
     * 构建AI工具参数
     *
     * @param params AI工具参数构建参数
     * @return AI工具参数
     */
    private static JsonObjectSchema buildJsonObjectSchema(List<ToolSpecParamEntity> params) {

        //1.创建builder
        JsonObjectSchema.Builder builder = JsonObjectSchema.builder();

        //2.遍历工具参数,根据类型设置参数名称以及描述
        for (ToolSpecParamEntity param : params) {
            switch (param.getType()) {
                case STRING:
                    builder.addStringProperty(param.getName(), param.getDescription());
                    break;
                case INTEGER:
                    builder.addIntegerProperty(param.getName(), param.getDescription());
                    break;
                case NUMBER:
                    builder.addNumberProperty(param.getName(), param.getDescription());
                    break;
                case BOOLEAN:
                    builder.addBooleanProperty(param.getName(), param.getDescription());
                    break;
                case ENUMS:
                    builder.addEnumProperty(param.getName(), param.getEnumValues(), param.getDescription());
                    break;
                default:
                    log.warn("[AI工具构建] 参数:[{}]类型:[{}]异常 跳过", param.getName(), param.getType());
                    break;
            }
        }

        //3.解析必填参数
        List<String> required = params.stream().filter(ToolSpecParamEntity::isRequired).map(ToolSpecParamEntity::getName).toList();

        //4.设置必填参数
        builder.required(required);

        //5.构建AI工具参数返回
        return builder.build();
    }
}
