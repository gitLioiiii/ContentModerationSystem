package com.example.demo.service;

import com.example.demo.dto.ImageAutoRequest;
import com.example.demo.dto.ImageAutoResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;

import java.io.File;

@Slf4j
@Service
public class ImageAutoService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    // 构造方法
    public ImageAutoService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = new ObjectMapper();
    }

    // 图片审核方法
    public ImageAutoResponse AiImage(ImageAutoRequest request) {
        String imageUrl = request.getImageUrl();  // 前端显示用的URL
        String filePath = request.getFilePath();  // AI读取用的本地文件路径
        String mimeType = request.getMimeType();
        log.info("开始图片审核 - 显示URL: {}, 文件路径: {}, MIME类型: {}", imageUrl, filePath, mimeType);

        try {
            // 直接使用本地文件路径，转换为 Spring Resource
            File imageFile = new File(filePath);

            // 使用 FileSystemResource 包装文件（Spring AI 需要 Resource 对象）
            Resource imageResource = new FileSystemResource(imageFile);
            log.info("使用本地文件资源: {}", imageFile.getAbsolutePath());

            // 根据MIME类型解析为MimeType对象
            org.springframework.util.MimeType imageMimeType;
            if (mimeType != null && !mimeType.isEmpty()) {
                imageMimeType = org.springframework.util.MimeType.valueOf(mimeType);
            } else {
                // 如果没有提供MIME类型，默认使用PNG
                imageMimeType = MimeTypeUtils.IMAGE_PNG;
            }

            // 调用 Spring AI ChatClient 进行图片审核
            // 使用支持视觉的模型（如 llava）
            String aiResponse = this.chatClient.prompt()
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
                        "matchScore": 0.0到1.0之间的数值
                    }

                    判断标准：
                    - "通过"：图片内容健康，无违规内容，matchScore < 0.3
                    - "不通过"：明确存在违规内容，matchScore >= 0.7
                    - "人工审核"：存在可疑内容但不确定，0.3 <= matchScore < 0.7

                    matchScore 匹配分数说明：
                    - 0.0-0.3：内容安全，风险低
                    - 0.3-0.5：存在轻微敏感内容，建议人工复审
                    - 0.5-0.7：存在明显敏感内容，需要人工确认
                    - 0.7-1.0：明确违规，必须拒绝

                    注意：分数越高表示越危险，越不能通过审核。
                    """)
                .user(userSpec -> userSpec
                    .text("请审核这张图片的内容，判断是否包含色情、暴力或政治敏感内容。")
                    .media(imageMimeType, imageResource))
                .call()
                .content();

            log.debug("AI 审核原始响应: {}", aiResponse);

            // 解析 AI 响应
            ImageAutoResponse response = parseAiResponse(aiResponse);

            log.info("AI 图片审核完成 - 结果: {}, 匹配分数: {}", response.getResult(), response.getMatchScore());

            return response;

        } catch (Exception e) {
            log.error("AI 图片审核失败", e);
            return new ImageAutoResponse(
                "人工审核",
                "AI 审核异常: " + e.getMessage(),
                0.5
            );
        }
    }

    // 解析 AI 响应
    private ImageAutoResponse parseAiResponse(String aiResponse) {
        log.info("----- 后台打印 - AI 响应内容 ------");
        log.info("AI响应: {}", aiResponse);
        log.info("---------------------------------");

        try {
            // 使用 Jackson 解析 JSON
            JsonNode jsonNode = objectMapper.readTree(aiResponse);

            String result = jsonNode.has("result") ? jsonNode.get("result").asText("未知") : "人工审核";
            String reason = jsonNode.has("reason") ? jsonNode.get("reason").asText("未知原因") : "未知原因";
            Double matchScore = jsonNode.has("matchScore") ? jsonNode.get("matchScore").asDouble(0.5) : 0.5;

            // 验证匹配分数范围
            if (matchScore < 0.0) matchScore = 0.0;
            if (matchScore > 1.0) matchScore = 1.0;

            return new ImageAutoResponse(result, reason, matchScore);

        } catch (Exception e) {
            log.error("解析 AI 响应失败: {}", aiResponse, e);
            // 解析失败时，返回需要人工审核
            return new ImageAutoResponse(
                "人工审核",
                "AI 响应解析失败，建议人工审核",
                0.5
            );
        }
    }
}
