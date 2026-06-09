package com.ftc.ftcli.common.util.github;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-09 10:55:16
 * @describe GitHubUrl信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GitHubUrlInfo {

    @Schema(description = "用户")
    private String owner;

    @Schema(description = "仓库")
    private String repo;

    @Schema(description = "分支、标签、Commit")
    private String branchOrTag;

    @Schema(description = "文件路径")
    private String filePath;
}
