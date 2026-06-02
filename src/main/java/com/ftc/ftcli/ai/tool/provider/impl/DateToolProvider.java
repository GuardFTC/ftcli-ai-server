package com.ftc.ftcli.ai.tool.provider.impl;

import cn.hutool.core.util.StrUtil;
import com.ftc.ftcli.ai.tool.ToolTypeEnum;
import com.ftc.ftcli.ai.tool.provider.IToolProvider;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.service.tool.ToolExecutor;
import dev.langchain4j.service.tool.ToolProviderRequest;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-05-14 10:32:59
 * @describe 时间工具提供者
 */
@Component
public class DateToolProvider implements IToolProvider {

    /**
     * 时间关键词
     */
    private static final String[] TIME_KEYWORDS = {
            "时间", "日期", "时刻", "日历", "今日", "具体时间", "当前时间", "现在几点",
            "几点", "几分", "几号", "几时", "何时", "什么时候", "哪天", "哪年", "哪月",
            "今天", "明天", "昨天", "后天", "前天", "大后天", "大前天",
            "今年", "明年", "去年", "后年", "前年",
            "星期", "礼拜", "周一", "周二", "周三", "周四", "周五", "周六", "周日", "周末",
            "本周", "上周", "下周", "本月", "上月", "下月",
            "现在", "当前", "刚才", "刚刚", "此刻", "目前",
            "早上", "上午", "中午", "下午", "晚上", "凌晨", "半夜", "夜里",
            "小时", "分钟", "秒钟", "半小时",
            "天", "日", "号", "年", "月", "点", "秒", "分", "时", "周",
            "time", "date", "now"
    };

    /**
     * 预编译正则模式，用于匹配类似 "2026-05-14" 或 "14:30" 这样的纯数字符号时间格式，提升正则匹配性能
     */
    private static final Pattern TIME_PATTERN = Pattern.compile(".*(\\d{4}[-/年]\\d{1,2}[-/月]\\d{1,2}|\\d{1,2}[:点]\\d{1,2}).*");

    @Override
    public boolean isMatch(ToolProviderRequest request) {

        //1.获取用户消息
        String userMessage = request.userMessage().singleText();

        //2.为空直接返回false
        if (StrUtil.isBlank(userMessage)) {
            return false;
        }

        //3.转换为小写，防止英文大小写或全角半角符号干扰匹配
        String lowerMessage = userMessage.toLowerCase();

        //4.使用 Hutool 的 containsAny 进行最高效的数组遍历匹配（底层为for循环，性能远优于Stream）
        if (StrUtil.containsAny(lowerMessage, TIME_KEYWORDS)) {
            return true;
        }

        //5.正则兜底匹配：提取隐式的纯数字日期时间格式（如：2026-05-14、2026/05/14、14:30）
        return TIME_PATTERN.matcher(lowerMessage).matches();
    }

    @Override
    public Map<ToolSpecification, ToolExecutor> getTools(Map<String, Map<ToolSpecification, ToolExecutor>> typeToolSpecToolExecutorMap) {
        return typeToolSpecToolExecutorMap.get(ToolTypeEnum.DATE.getType());
    }
}