package com.ftc.ftcli.service.base;

import java.util.List;
import java.util.Map;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-02 19:32:11
 * @describe Chroma向量数据库服务
 */
public interface ChromaService {

    /**
     * 获取向量记录数
     *
     * @return 向量记录数
     */
    int getVectorCount();

    /**
     * 获取文档片段列表
     *
     * @param id     文档ID
     * @param offset 偏移量
     * @param size   每页条数
     * @return 文档片段列表
     */
    List<Map<String, Object>> getChunks(Long id, int offset, int size);

    /**
     * 获取文档片段总数
     *
     * @param id 文档ID
     * @return 片段总数
     */
    int getChunkCount(Long id);
}
