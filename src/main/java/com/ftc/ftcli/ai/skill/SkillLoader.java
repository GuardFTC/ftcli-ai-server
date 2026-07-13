package com.ftc.ftcli.ai.skill;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.ftc.ftcli.entity.skill.SkillEntity;
import com.ftc.ftcli.infra.sqlite.repository.SkillRepository;
import dev.langchain4j.skills.ClassPathSkillLoader;
import dev.langchain4j.skills.Skill;
import dev.langchain4j.skills.Skills;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: 冯铁城 [17615007230@163.com]
 * @date: 2026-07-09 19:30:30
 * @describe: 技能加载器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SkillLoader {

    private final SkillRepository skillRepository;

    /**
     * 加载Skill
     *
     * @return Skills {@link Skills}
     */
    public Skills loadSkills() {

        //1.查询Skill列表
        List<SkillEntity> skillBeans = skillRepository.findAll();

        //2.定义最终加载Skill集合
        List<Skill> skills = new ArrayList<>();

        //3.遍历Skill列表
        for (SkillEntity skillBean : skillBeans) {

            //4.获取Skill内容
            String skillMdContent = skillBean.getSkillMdContent();

            //5.定义Skill变量
            Skill skill;

            //6.如果内容不为空，直接构建Skill，否则，从文件中获取
            if (StrUtil.isNotBlank(skillMdContent)) {
                skill = Skill.builder()
                        .name(skillBean.getSkillName())
                        .description(skillBean.getSkillDescription())
                        .content(skillMdContent)
                        .build();
            } else {
                skill = ClassPathSkillLoader.loadSkill(skillBean.getSkillMdPath());
            }

            //7.如果内容为空，跳过
            if (StrUtil.isBlank(skill.content())) {
                log.warn("[Skill] 加载失败,无法获取到内容: [{}]", skillBean.getSkillName());
                continue;
            }

            //8.存入集合
            skills.add(skill);
        }

        //9.打印日志
        log.info("[Skill] 加载完成,共[{}]个技能", skills.size());

        //10.返回
        return CollUtil.isEmpty(skills) ? null : Skills.from(skills);
    }
}
