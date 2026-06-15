package com.ftc.ftcli.config.ai;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import dev.langchain4j.skills.FileSystemSkill;
import dev.langchain4j.skills.FileSystemSkillLoader;
import dev.langchain4j.skills.Skills;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-15 14:00:00
 * @describe AI Skills 配置类
 */
@Slf4j
@Configuration
public class SkillConfig {

    @Bean
    public Skills skills() throws IOException {

        //1.扫描 classpath 下所有 skills/*/SKILL.md
        Resource[] resources = new PathMatchingResourcePatternResolver().getResources("classpath:skills/*/SKILL.md");

        //2.一行代码创建动态唯一的临时目录（带 UUID 绝对防止多线程/重名冲突）
        File tempSkillsDir = FileUtil.mkdir(FileUtil.getTmpDir() + "/ftcli-skills-" + IdUtil.fastSimpleUUID());

        //3.遍历并复制
        for (Resource resource : resources) {

            //4.获取Skill名称
            String skillName = ReUtil.get("skills/([^/]+)/SKILL.md$", resource.getURL().toString(), 1);
            if (StrUtil.isBlank(skillName)) {
                continue;
            }

            //5.复制Skill文件到临时目录
            File targetFile = FileUtil.file(tempSkillsDir, skillName, "SKILL.md");
            FileUtil.writeFromStream(resource.getInputStream(), targetFile);
        }

        //6.从临时目录加载全部 Skill
        List<FileSystemSkill> skillList = FileSystemSkillLoader.loadSkills(tempSkillsDir.toPath());
        log.info("[Skills] 加载完成, 共[{}]个 Skill", skillList.size());

        //7.注册 JVM 退出时自动清理临时目录（从watch dog的角度保证不留垃圾）
        FileUtil.del(tempSkillsDir);

        //8.创建 Skills
        return Skills.from(skillList);
    }
}
