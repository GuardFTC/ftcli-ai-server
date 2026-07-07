package com.ftc.ftcli.infra.sqlite.repository;

import cn.hutool.core.collection.CollUtil;
import com.ftc.ftcli.entity.embedding.EmbeddingChunkRecordEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-07-07 00:00:00
 * @describe Embedding文档Chunk记录数据访问层
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class EmbeddingChunkRecordRepository {

    private final JdbcTemplate jdbcTemplate;

    /**
     * 查询全部记录
     *
     * @return 全部Chunk记录
     */
    public List<EmbeddingChunkRecordEntity> findAll() {

        //1.定义SQL
        String sql = "SELECT id, file_name_md5, chunk_index, chunk_content_md5, created_at, updated_at FROM embedding_chunk_record ORDER BY file_name_md5 asc, chunk_index asc";

        //2.查询
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);

        //3.判空
        if (CollUtil.isEmpty(rows)) {
            return Collections.emptyList();
        }

        //4.映射返回
        return rows.stream().map(this::mapRowToEntity).toList();
    }

    /**
     * 根据文件名MD5查询该文件下全部Chunk记录
     *
     * @param fileNameMd5 文件名MD5
     * @return 该文件下全部Chunk记录
     */
    public List<EmbeddingChunkRecordEntity> findAllByFileNameMd5(String fileNameMd5) {

        //1.定义SQL
        String sql = "SELECT id, file_name_md5, chunk_index, chunk_content_md5, created_at, updated_at FROM embedding_chunk_record WHERE file_name_md5 = ? ORDER BY chunk_index asc";

        //2.查询
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, fileNameMd5);

        //3.判空
        if (CollUtil.isEmpty(rows)) {
            return Collections.emptyList();
        }

        //4.映射返回
        return rows.stream().map(this::mapRowToEntity).toList();
    }

    /**
     * 根据文件名MD5集合批量查询Chunk记录
     *
     * @param fileNameMd5Set 文件名MD5集合
     * @return Chunk记录列表
     */
    public List<EmbeddingChunkRecordEntity> findAllByFileNameMd5In(Set<String> fileNameMd5Set) {

        //1.判空
        if (CollUtil.isEmpty(fileNameMd5Set)) {
            return Collections.emptyList();
        }

        //2.构建IN查询占位符
        String placeholders = fileNameMd5Set.stream().map(s -> "?").collect(Collectors.joining(","));
        String sql = "SELECT id, file_name_md5, chunk_index, chunk_content_md5, created_at, updated_at FROM embedding_chunk_record WHERE file_name_md5 IN (" + placeholders + ") ORDER BY file_name_md5 asc, chunk_index asc";

        //3.查询
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, fileNameMd5Set.toArray());

        //4.判空
        if (CollUtil.isEmpty(rows)) {
            return Collections.emptyList();
        }

        //5.映射返回
        return rows.stream().map(this::mapRowToEntity).toList();
    }

    /**
     * 根据ID查询记录
     *
     * @param id 记录ID
     * @return Chunk记录，不存在返回null
     */
    public EmbeddingChunkRecordEntity findById(Long id) {

        //1.定义SQL
        String sql = "SELECT id, file_name_md5, chunk_index, chunk_content_md5, created_at, updated_at FROM embedding_chunk_record WHERE id = ?";

        //2.查询
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, id);

        //3.判空
        if (CollUtil.isEmpty(rows)) {
            return null;
        }

        //4.映射返回
        return mapRowToEntity(rows.get(0));
    }

    /**
     * 批量保存新增的Chunk记录
     *
     * @param entities 待保存的Chunk记录集合
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveBatch(List<EmbeddingChunkRecordEntity> entities) {

        //1.判空
        if (CollUtil.isEmpty(entities)) {
            return;
        }

        //2.构建多行VALUES的批量INSERT SQL
        StringBuilder sql = new StringBuilder("INSERT INTO embedding_chunk_record (file_name_md5, chunk_index, chunk_content_md5) VALUES ");
        List<Object> params = new ArrayList<>();

        //3.遍历拼接
        for (int i = 0; i < entities.size(); i++) {

            //4.获取待保存的记录
            EmbeddingChunkRecordEntity entity = entities.get(i);

            //5.如果不是第一行,拼接,
            if (i > 0) {
                sql.append(",");
            }

            //6.拼接参数
            sql.append("(?, ?, ?)");
            params.add(entity.getFileNameMd5());
            params.add(entity.getChunkIndex());
            params.add(entity.getChunkContentMd5());
        }

        //7.执行
        jdbcTemplate.update(sql.toString(), params.toArray());
    }

    /**
     * 根据文件名MD5删除该文件下全部Chunk记录
     *
     * @param fileNameMd5 文件名MD5
     * @return 删除的记录数
     */
    public int deleteByFileNameMd5(String fileNameMd5) {

        //1.定义SQL
        String sql = "DELETE FROM embedding_chunk_record WHERE file_name_md5 = ?";

        //2.执行返回
        return jdbcTemplate.update(sql, fileNameMd5);
    }

    /**
     * 根据文件名MD5集合批量删除Chunk记录
     *
     * @param fileNameMd5Set 文件名MD5集合
     * @return 删除的记录数
     */
    public int deleteByFileNameMd5In(Set<String> fileNameMd5Set) {

        //1.判空
        if (CollUtil.isEmpty(fileNameMd5Set)) {
            return 0;
        }

        //2.构建IN删除占位符
        String placeholders = fileNameMd5Set.stream().map(s -> "?").collect(Collectors.joining(","));
        String sql = "DELETE FROM embedding_chunk_record WHERE file_name_md5 IN (" + placeholders + ")";

        //3.执行返回
        return jdbcTemplate.update(sql, fileNameMd5Set.toArray());
    }

    /**
     * 根据ID删除记录
     *
     * @param id 记录ID
     * @return 是否删除成功
     */
    public boolean deleteById(Long id) {

        //1.定义SQL
        String sql = "DELETE FROM embedding_chunk_record WHERE id = ?";

        //2.执行
        int affected = jdbcTemplate.update(sql, id);

        //3.返回
        return affected > 0;
    }

    /**
     * 行数据映射为实体
     *
     * @param row 行数据
     * @return 实体
     */
    private EmbeddingChunkRecordEntity mapRowToEntity(Map<String, Object> row) {
        return EmbeddingChunkRecordEntity.builder()
                .id(((Number) row.get("id")).longValue())
                .fileNameMd5((String) row.get("file_name_md5"))
                .chunkIndex(((Number) row.get("chunk_index")).intValue())
                .chunkContentMd5((String) row.get("chunk_content_md5"))
                .createdAt((String) row.get("created_at"))
                .updatedAt((String) row.get("updated_at"))
                .build();
    }
}
