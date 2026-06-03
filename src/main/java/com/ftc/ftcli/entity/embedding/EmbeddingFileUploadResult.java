package com.ftc.ftcli.entity.embedding;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-03 10:23:26
 * @describe 文件上传结果
 */
@Data
public class EmbeddingFileUploadResult {

    @Schema(description = "新增文件列表")
    List<String> addFiles;

    @Schema(description = "更新文件列表")
    List<String> updateFiles;
}
