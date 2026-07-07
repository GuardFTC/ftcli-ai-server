package com.ftc.ftcli.service.ai.impl;

import com.ftc.ftcli.ai.tool.ToolRegistry;
import com.ftc.ftcli.ai.tool.spec.ToolSpecEntity;
import com.ftc.ftcli.infra.sqlite.repository.ToolSpecRepository;
import com.ftc.ftcli.service.ai.AIToolService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-02 11:23:55
 * @describe AI工具Service实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIToolServiceImpl implements AIToolService {

    private final ToolSpecRepository toolSpecRepository;

    @Override
    public List<ToolSpecEntity> getTools() {
        return toolSpecRepository.findAll();
    }

    @Override
    public Long addTool(ToolSpecEntity toolSpec) {

        //1.校验工具名称是否已存在
        if (toolSpecRepository.existsByName(toolSpec.getName())) {
            throw new IllegalArgumentException("工具名称已存在: " + toolSpec.getName());
        }

        //2.保存工具
        Long toolId = toolSpecRepository.save(toolSpec);

        //3.刷新工具缓存
        refreshToolCache();

        //4.返回
        return toolId;
    }

    @Override
    public boolean removeTool(String name) {

        //1.删除工具
        boolean result = toolSpecRepository.deleteByName(name);

        //2.刷新工具缓存
        if (result) {
            refreshToolCache();
        }

        //3.返回
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTool(String oldName, ToolSpecEntity toolSpec) {

        //1.校验旧工具是否存在
        if (!toolSpecRepository.existsByName(oldName)) {
            throw new IllegalArgumentException("工具不存在: " + oldName);
        }

        //2.如果名称发生变更，校验新名称是否已被占用
        if (!oldName.equals(toolSpec.getName()) && toolSpecRepository.existsByName(toolSpec.getName())) {
            throw new IllegalArgumentException("工具名称已存在: " + toolSpec.getName());
        }

        //3.删除旧工具
        toolSpecRepository.deleteByName(oldName);

        //4.保存新工具
        toolSpecRepository.save(toolSpec);

        //5.刷新工具缓存
        refreshToolCache();
    }

    /**
     * 刷新工具缓存
     */
    private void refreshToolCache() {

        //1.查询所有工具描述
        List<ToolSpecEntity> toolSpecEntities = toolSpecRepository.findAll();

        //2.刷新工具缓存
        ToolRegistry.loadToolCache(toolSpecEntities);
    }
}
