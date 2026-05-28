package com.ftc.ftcli.ai.tool.executor.date;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.ftc.ftcli.ai.tool.executor.IToolExecutor;
import dev.langchain4j.service.tool.ToolExecutor;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-05-13 10:22:16
 * @describe 计算当前时间距离当天结束还有多少时间
 */
@Component
public class GetRemainingTimeExecutor implements IToolExecutor {

    @Override
    public String getName() {
        return "getRemainingTimeToDayEnd";
    }

    @Override
    public ToolExecutor getToolExecutor() {
        return (toolExecutionRequest, memoryId) -> {

            //1.从request中解析LLM传入的参数
            Map<String, Object> arguments = JSON.parseObject(toolExecutionRequest.arguments());
            String nowDateStr = arguments.get("nowDateStr").toString();
            Object unitObj = arguments.get("unit");

            //2.单位判定
            DateUnit unit = (unitObj != null && StrUtil.isNotBlank(unitObj.toString()))
                    ? DateUnit.valueOf(unitObj.toString())
                    : DateUnit.HOUR;

            //3.当前时间解析为Date
            Date now = DateUtil.parse(nowDateStr, DatePattern.NORM_DATETIME_PATTERN);

            //4.获取明天的开始时间
            Date dayEnd = DateUtil.beginOfDay(DateUtil.tomorrow());

            //5.计算时间间隔
            return StrUtil.toString(DateUtil.between(now, dayEnd, unit));
        };
    }
}
