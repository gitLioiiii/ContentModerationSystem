package com.example.demo.service;

import com.example.demo.dto.ImageAutoRequest;
import com.example.demo.dto.ImageAutoResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;

import java.io.File;
import java.util.concurrent.CompletableFuture;

// 异步视频帧审核（仅用于视频审核）
// 使用云端 ChatClient + Spring Boot 线程池并行处理
@Slf4j
@Service
public class AsyncImageModerationService {

    private final ChatClient cloudChatClient;
    private final ObjectMapper objectMapper;

    // 注入云端 ChatClient（视频抽帧审核使用云端）
    public AsyncImageModerationService(@Qualifier("cloudChatClient") ChatClient cloudChatClient) {
        this.cloudChatClient = cloudChatClient;
        this.objectMapper = new ObjectMapper();
    }

    @Async
    public CompletableFuture<ImageAutoResponse> moderateImageAsync(ImageAutoRequest imageRequest) {
        try {
            log.debug("线程 {} 开始审核图片: {}",
                Thread.currentThread().getName(),
                imageRequest.getImageUrl());

            // 使用云端 ChatClient 审核视频帧
            ImageAutoResponse response = moderateWithCloud(imageRequest);

            log.debug("线程 {} 完成审核图片: {} - 结果: {}",
                Thread.currentThread().getName(),
                imageRequest.getImageUrl(),
                response.getResult());

            return CompletableFuture.completedFuture(response);

        } catch (Exception e) {
            log.error("异步审核图片失败: {}", imageRequest.getImageUrl(), e);
            CompletableFuture<ImageAutoResponse> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(e);
            return failedFuture;
        }
    }

    // 使用云端 ChatClient 进行图片审核
    private ImageAutoResponse moderateWithCloud(ImageAutoRequest request) {
        String filePath = request.getFilePath();
        String mimeType = request.getMimeType();

        try {
            File imageFile = new File(filePath);
            Resource imageResource = new FileSystemResource(imageFile);

            org.springframework.util.MimeType imageMimeType;
            if (mimeType != null && !mimeType.isEmpty()) {
                imageMimeType = org.springframework.util.MimeType.valueOf(mimeType);
            } else {
                imageMimeType = MimeTypeUtils.IMAGE_PNG;
            }

            String aiResponse = this.cloudChatClient.prompt()
                .system("""
                    你是一个专业的图片内容审核专家，负责审核用户提交的图片内容。

                    你的任务是识别以下违规内容：
                    1. 色情内容：裸露、性行为、性暗示等色情低俗图片
                    2. 暴力血腥：暴力场景、血腥画面、恐怖图片
                    3. 政治敏感：政治人物、政治标语、敏感事件等

                    请仔细分析图片内容，并严格按照以下 JSON 格式返回审核结果（不要添加任何其他说明文字）：
                    {
                        "result": "通过" 或 "不通过" 或 "人工审核",
                        "reason": "简要说明审核理由，描述图片中发现的问题",
                        "matchScore": 0到100之间的数值
                    }

                    判断标准：
                    - "通过"：图片内容健康，无违规内容，matchScore < 30
                    - "不通过"：明确存在违规内容，matchScore >= 70
                    - "人工审核"：存在可疑内容但不确定，30 <= matchScore < 70

                    matchScore 匹配分数说明：
                    - 0-30：内容安全，风险低
                    - 30-50：存在轻微敏感内容，建议人工复审
                    - 50-70：存在明显敏感内容，需要人工确认
                    - 70-100：明确违规，必须拒绝

                    注意：分数越高表示越危险，越不能通过审核。
                    """)
                .user(userSpec -> userSpec
                    .text("请审核这张图片的内容，判断是否包含色情、暴力或政治敏感内容。")
                    .media(imageMimeType, imageResource))
                .call()
                .content();

            return parseAiResponse(aiResponse);

        } catch (Exception e) {
            log.error("云端图片审核失败", e);
            return new ImageAutoResponse(
                "人工审核",
                "AI 审核异常: " + e.getMessage(),
                50.0
            );
        }
    }

    // 解析 AI 响应
    private ImageAutoResponse parseAiResponse(String aiResponse) {
        log.info("----- 云端视频帧审核 - AI 响应 ------");
        log.info("AI响应: {}", aiResponse);
        log.info("***********************************");

        try {
            JsonNode jsonNode = objectMapper.readTree(aiResponse);

            String result = jsonNode.has("result") ? jsonNode.get("result").asText("未知") : "人工审核";
            String reason = jsonNode.has("reason") ? jsonNode.get("reason").asText("未知原因") : "未知原因";
            Double matchScore = jsonNode.has("matchScore") ? jsonNode.get("matchScore").asDouble(50.0) : 50.0;

            if (matchScore < 0.0) matchScore = 0.0;
            if (matchScore > 100.0) matchScore = 100.0;

            return new ImageAutoResponse(result, reason, matchScore);

        } catch (Exception e) {
            log.error("解析 AI 响应失败: {}", aiResponse, e);
            return new ImageAutoResponse(
                "人工审核",
                "AI 响应解析失败，建议人工审核",
                50.0
            );
        }
    }
}
