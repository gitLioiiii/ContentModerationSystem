package com.example.demo.service;

import com.example.demo.dto.TextAutoRequest;
import com.example.demo.dto.TextAutoResponse;

import lombok.extern.slf4j.Slf4j;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TextAutoService {

    private final ChatClient chatClient;

    public TextAutoService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public TextAutoResponse match_Ai_Text(TextAutoRequest request) {
        String content = request.getContent();

        log.info("开始 AI 文本审核，文本长度: {} 字符", content.length());

        try {
            // 构建审核提示词
            String systemPrompt = buildSystemPrompt();
            String userPrompt = buildUserPrompt(content);

            // 调用 Spring AI ChatClient 进行审核
            String aiResponse = this.chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .content();

            log.debug("AI 审核原始响应: {}", aiResponse);

            // 解析 AI 响应
            TextAutoResponse response = parseAiResponse(aiResponse, content);

            log.info("AI 审核完成 - 结果: {}, 风险等级: {}", response.getResult(), response.getRiskLevel());

            return response;

        } catch (Exception e) {
            log.error("AI 文本审核失败", e);
            // 发生异常时返回需要人工审核
            return new TextAutoResponse(
                "需人工审核",
                "AI 审核异常: " + e.getMessage(),
                "中",
                content
            );
        }
    }

    /**
     * 构建系统提示词 - 定义 AI 的角色和任务
     */
    private String buildSystemPrompt() {
        return """
            你是一个专业的内容审核专家，负责审核用户提交的文本内容。

            你的任务是识别以下违规内容：
            1. 政治敏感信息：涉及政治人物、政治事件、政治观点等
            2. 色情低俗内容：包含色情、性暗示、低俗内容
            3. 暴力血腥内容：描述暴力、血腥、恐怖场景
            4. 违法信息：涉及毒品、赌博、诈骗等违法犯罪内容
            5. 侮辱谩骂：人身攻击、侮辱、歧视性言论
            6. 虚假信息：明显的谣言、虚假宣传
            7. 广告营销：未经授权的广告、垃圾营销信息
            8. 其他不当内容：其他可能引起不良影响的内容

            请严格按照以下 JSON 格式返回审核结果，不要添加任何其他说明文字：
            {
                "result": "通过" 或 "拒绝" 或 "需人工审核",
                "reason": "简要说明审核理由",
                "riskLevel": "低" 或 "中" 或 "高"
            }

            判断标准：
            - "通过"：内容完全合规，无任何违规风险
            - "拒绝"：明确存在违规内容，风险等级为高
            - "需人工审核"：存在可疑内容但不确定，或风险等级为中

            风险等级判断：
            - "低"：内容合规，无风险
            - "中"：存在轻微敏感或可疑内容，建议人工复审
            - "高"：明确违规，必须拒绝
            """;
    }

    /**
     * 构建用户提示词 - 待审核的内容
     */
    private String buildUserPrompt(String content) {
        return "请审核以下文本内容：\n\n" + content;
    }

    /**
     * 解析 AI 响应，提取审核结果
     */
    private TextAutoResponse parseAiResponse(String aiResponse, String originalText) {
        try {
            // 清理响应内容，去除可能的 markdown 代码块标记
            String cleanResponse = aiResponse
                .replaceAll("```json\\s*", "")
                .replaceAll("```\\s*", "")
                .trim();

            // 简单的 JSON 解析（生产环境建议使用 Jackson 或 Gson）
            String result = extractJsonValue(cleanResponse, "result");
            String reason = extractJsonValue(cleanResponse, "reason");
            String riskLevel = extractJsonValue(cleanResponse, "riskLevel");

            return new TextAutoResponse(result, reason, riskLevel, originalText);

        } catch (Exception e) {
            log.error("解析 AI 响应失败: {}", aiResponse, e);
            // 解析失败时，返回需要人工审核
            return new TextAutoResponse(
                "需人工审核",
                "AI 响应解析失败，建议人工审核",
                "中",
                originalText
            );
        }
    }

    /**
     * 简单的 JSON 值提取（从 JSON 字符串中提取指定字段的值）
     */
    private String extractJsonValue(String json, String key) {
        String pattern = "\"" + key + "\"\\s*:\\s*\"([^\"]+)\"";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(json);
        if (m.find()) {
            return m.group(1);
        }
        return "未知";
    }
}