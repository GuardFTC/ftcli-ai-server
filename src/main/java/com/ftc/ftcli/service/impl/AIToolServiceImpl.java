package com.ftc.ftcli.service.impl;

import com.ftc.ftcli.ai.tool.ToolRegistry;
import com.ftc.ftcli.ai.tool.spec.ToolSpecEntity;
import com.ftc.ftcli.infra.sqlite.ToolSpecRepository;
import com.ftc.ftcli.service.AIToolService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
    public Long addTool(ToolSpecEntity entity) {

        //1.校验工具名称是否已存在
        if (toolSpecRepository.existsByName(entity.getName())) {
            throw new IllegalArgumentException("工具名称已存在: " + entity.getName());
        }

        //2.保存工具
        Long toolId = toolSpecRepository.save(entity);

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
    public void updateTool(ToolSpecEntity entity) {

        //1.校验工具是否存在
        if (!toolSpecRepository.existsByName(entity.getName())) {
            throw new IllegalArgumentException("工具不存在: " + entity.getName());
        }

        //2.先删除旧工具
        toolSpecRepository.deleteByName(entity.getName());

        //3.再新增工具
        toolSpecRepository.save(entity);

        //4.刷新工具缓存
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
