package com.ftc.ftcli.controller;

import com.ftc.ftcli.entity.embedding.EmbeddingFileUploadPayload;
import com.ftc.ftcli.entity.embedding.EmbeddingFileUploadResult;
import com.ftc.ftcli.entity.result.RestfulResult;
import com.ftc.ftcli.service.AIEmbeddingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-02 11:19:28
 * @describe AI向量嵌入控制器
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "AI向量嵌入", description = "AI向量嵌入控制器")
@RequestMapping("/api/rest/v1/ai/embedding")
public class AIEmbeddingController {

    private final AIEmbeddingService aiEmbeddingService;

    @PostMapping
    @Operation(summary = "新增文档")
    public RestfulResult<EmbeddingFileUploadResult> upload(@RequestBody EmbeddingFileUploadPayload payload) {

        //1.打印日志
        log.info("[AI] 新增文档 入参:[{}]", payload);

        //2.新增文档
        EmbeddingFileUploadResult fileUploadResult = aiEmbeddingService.upload(payload);
        log.info("[AI] 新增文档 出参:[{}]", fileUploadResult);

        //3.返回
        return RestfulResult.Success.addData(fileUploadResult);
    }
}
