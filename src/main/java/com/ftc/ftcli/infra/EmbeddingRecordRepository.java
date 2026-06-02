package com.ftc.ftcli.infra;

import cn.hutool.core.collection.CollUtil;
import com.ftc.ftcli.entity.embedding.EmbeddingRecordEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-02 20:00:00
 * @describe Embedding文档记录数据访问层
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class EmbeddingRecordRepository {

    private final JdbcTemplate jdbcTemplate;

    /**
     * 查询全部记录
     *
     * @return 全部embedding记录
     */
    public List<EmbeddingRecordEntity> findAll() {

        //1.定义SQL
        String sql = "SELECT id, file_name, file_path, file_name_md5, file_content_md5, created_at, updated_at FROM embedding_record";

        //2.查询
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);

        //3.判空
        if (CollUtil.isEmpty(rows)) {
            return Collections.emptyList();
        }

        //4.映射
        return rows.stream().map(this::mapRowToEntity).toList();
    }

    /**
     * 根据文件名MD5查询单条记录
     *
     * @param fileNameMd5 文件名MD5
     * @return 记录实体，不存在则返回null
     */
    public EmbeddingRecordEntity findByFileNameMd5(String fileNameMd5) {

        //1.定义SQL
        String sql = "SELECT id, file_name, file_path, file_name_md5, file_content_md5, created_at, updated_at FROM embedding_record WHERE file_name_md5 = ?";

        //2.查询
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, fileNameMd5);

        //3.判空
        if (CollUtil.isEmpty(rows)) {
            return null;
        }

        //4.返回
        return mapRowToEntity(rows.get(0));
    }

    /**
     * 新增记录
     *
     * @param entity 记录实体
     */
    public void save(EmbeddingRecordEntity entity) {
        String sql = "INSERT INTO embedding_record (file_name, file_path, file_name_md5, file_content_md5) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, entity.getFileName(), entity.getFilePath(), entity.getFileNameMd5(), entity.getFileContentMd5());
    }

    /**
     * 更新文件内容MD5（文档内容变更时调用）
     *
     * @param fileNameMd5    文件名MD5
     * @param newContentMd5  新的文件内容MD5
     */
    public void updateContentMd5(String fileNameMd5, String newContentMd5) {
        String sql = "UPDATE embedding_record SET file_content_md5 = ?, updated_at = datetime('now', 'localtime') WHERE file_name_md5 = ?";
        jdbcTemplate.update(sql, newContentMd5, fileNameMd5);
    }

    /**
     * 批量过滤，返回库中不存在的文件名MD5集合（用于批量embedding时筛出新文档）
     *
     * @param fileNameMd5List 待检查的文件名MD5列表
     * @return 库中不存在的MD5集合（即新文档）
     */
    public Set<String> batchFilterNew(List<String> fileNameMd5List) {

        //1.判空
        if (CollUtil.isEmpty(fileNameMd5List)) {
            return Collections.emptySet();
        }

        //2.构建IN查询的占位符
        String placeholders = fileNameMd5List.stream().map(s -> "?").collect(Collectors.joining(","));
        String sql = "SELECT file_name_md5 FROM embedding_record WHERE file_name_md5 IN (" + placeholders + ")";

        //3.查询已存在的MD5
        List<String> existingMd5List = jdbcTemplate.queryForList(sql, String.class, fileNameMd5List.toArray());

        //4.用Set做差集，返回不存在的部分
        Set<String> existingSet = Set.copyOf(existingMd5List);
        return fileNameMd5List.stream()
                .filter(md5 -> !existingSet.contains(md5))
                .collect(Collectors.toSet());
    }

    /**
     * 行数据映射为实体
     *
     * @param row 行数据
     * @return 实体
     */
    private EmbeddingRecordEntity mapRowToEntity(Map<String, Object> row) {
        return EmbeddingRecordEntity.builder()
                .id(((Number) row.get("id")).longValue())
                .fileName((String) row.get("file_name"))
                .filePath((String) row.get("file_path"))
                .fileNameMd5((String) row.get("file_name_md5"))
                .fileContentMd5((String) row.get("file_content_md5"))
                .createdAt((String) row.get("created_at"))
                .updatedAt((String) row.get("updated_at"))
                .build();
    }
}
