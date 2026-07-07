package com.ftc.ftcli.entity.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-03 10:23:26
 * @describe 文件上传结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmbeddingFileUploadResult {

    @Schema(description = "新增文件列表")
    List<String> addFiles = new ArrayList<>();

    @Schema(description = "更新文件列表")
    List<String> updateFiles = new ArrayList<>();
}
