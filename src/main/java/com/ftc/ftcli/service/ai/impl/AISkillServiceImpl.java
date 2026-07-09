package com.ftc.ftcli.service.ai.impl;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import com.ftc.ftcli.ai.service.AiServiceHolder;
import com.ftc.ftcli.entity.skill.SkillEntity;
import com.ftc.ftcli.infra.sqlite.repository.SkillRepository;
import com.ftc.ftcli.service.ai.AISkillService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URL;
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
    @Transactional(rollbackFor = Exception.class)
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
    @Transactional(rollbackFor = Exception.class)
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
    @Transactional(rollbackFor = Exception.class)
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
