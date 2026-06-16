package com.ftc.ftcli.service.impl;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import com.ftc.ftcli.ai.service.AiServiceHolder;
import com.ftc.ftcli.entity.skill.SkillEntity;
import com.ftc.ftcli.infra.sqlite.SkillRepository;
import com.ftc.ftcli.service.AISkillService;
import dev.langchain4j.skills.Skill;
import dev.langchain4j.skills.Skills;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-15 15:00:00
 * @describe AI Skill技能服务实现类
 */
@Slf4j
@Service
public class AISkillServiceImpl implements AISkillService {

    private final SkillRepository skillRepository;

    private final AiServiceHolder aiServiceHolder;

    public AISkillServiceImpl(SkillRepository skillRepository, @Lazy AiServiceHolder aiServiceHolder) {
        this.skillRepository = skillRepository;
        this.aiServiceHolder = aiServiceHolder;
    }

    @Override
    public List<SkillEntity> getSkills() {
        return skillRepository.findAll();
    }

    @Override
    public SkillEntity getSkillById(Long id) {
        return skillRepository.findById(id);
    }

    @Override
    public boolean addSkill(SkillEntity payload) {

        //1.检查payload
        if (isPayloadIllegal(payload, StrUtil.EMPTY, "新增")) {
            return false;
        }

        //2.保存
        boolean success = skillRepository.save(payload);

        //3.重建AI服务
        if (success) {
            aiServiceHolder.buildAiService();
        }

        //4.返回
        return success;
    }

    @Override
    public boolean updateSkill(String oldName, SkillEntity payload) {

        //1.判断是否存在
        SkillEntity existSkill = skillRepository.findById(payload.getId());
        if (existSkill == null) {
            log.warn("[Skill] 更新失败,技能不存在: [{}]", payload.getId());
            return false;
        }

        //2.检查payload
        if (isPayloadIllegal(payload, oldName, "更新")) {
            return false;
        }

        //3.更新
        boolean success = skillRepository.update(payload);

        //4.重建AI服务
        if (success) {
            aiServiceHolder.buildAiService();
        }

        //5.返回
        return success;
    }

    @Override
    public boolean removeSkill(Long id) {

        //1.判断是否存在
        SkillEntity existSkill = skillRepository.findById(id);
        if (existSkill == null) {
            log.warn("[Skill] 删除失败,技能不存在: [{}]", id);
            return false;
        }

        //2.删除
        boolean success = skillRepository.deleteById(id);

        //3.重建AI服务
        if (success) {
            aiServiceHolder.buildAiService();
        }

        //4.返回
        return success;
    }

    @Override
    public Skills loadSkills() {

        //1.查询Skill列表
        List<SkillEntity> skillBeans = getSkills();

        //2.定义最终加载Skill集合
        List<Skill> skills = new ArrayList<>();

        //3.遍历Skill列表
        for (SkillEntity skillBean : skillBeans) {

            //4.获取内容,如果内容为空,从文件中获取
            String content = StrUtil.isNotBlank(skillBean.getSkillMdContent()) ?
                    skillBean.getSkillMdContent() :
                    ResourceUtil.readUtf8Str(skillBean.getSkillMdPath());

            //5.如果内容为空，跳过
            if (StrUtil.isBlank(content)) {
                log.warn("[Skill] 加载失败,无法获取到内容: [{}]", skillBean.getSkillName());
                continue;
            }

            //6.创建 Skill
            Skill skill = Skill.builder()
                    .name(skillBean.getSkillName())
                    .description(skillBean.getSkillDescription())
                    .content(content)
                    .build();

            //7.存入集合
            skills.add(skill);
        }

        //8.打印日志
        log.info("[Skill] 加载完成,共[{}]个技能", skills.size());

        //9.返回
        return Skills.from(skills);
    }

    /**
     * payload是否不合法
     *
     * @param payload   payload
     * @param oldName   旧名称
     * @param logAction 日志操作
     * @return payload是否不合法
     */
    private boolean isPayloadIllegal(SkillEntity payload, String oldName, String logAction) {

        //1.判断是否已存在同名Skill
        if (!oldName.equals(payload.getSkillName()) && skillRepository.existsByName(payload.getSkillName())) {
            log.warn("[Skill] {}失败 技能名称已存在: [{}]", logAction, payload.getSkillName());
            return true;
        }

        //2.如果md和path都为空，则不保存
        if (StrUtil.isBlank(payload.getSkillMdContent()) && StrUtil.isBlank(payload.getSkillMdPath())) {
            log.warn("[Skill] {}失败 SKILL.md文件内容和文件路径不能同时为空: [{}]", logAction, payload.getSkillName());
            return true;
        }

        //3.如果path不为空，
        if (StrUtil.isNotBlank(payload.getSkillMdPath())) {

            //4.判断文件在resource文件夹下是否存在
            URL resource = ResourceUtil.getResource(payload.getSkillMdPath());
            if (resource == null) {
                log.warn("[Skill] {}失败 SKILL.md文件在资源路径下不存在: [{}] [{}]", logAction, payload.getSkillName(), payload.getSkillMdPath());
                return true;
            }
        }

        //5.返回
        return false;
    }
}
