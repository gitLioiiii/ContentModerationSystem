package com.example.demo.service;

import com.example.demo.dto.TextAutoRequest;
import com.example.demo.dto.TextAutoResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TextAutoService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    // 构造法
    public TextAutoService(ChatClient.Builder chatClientBuilder, ObjectMapper objectMapper) {
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = new ObjectMapper();
    }

    // 接收文本
    public TextAutoResponse AiText(TextAutoRequest request) {
        String content = request.getContent();
        log.info("开始 AI 文本审核，文本长度: {} 字符", content.length());

        try {
            // 调用 Spring AI ChatClient 改成内联式审核
            String aiResponse = this.chatClient.prompt()
                .system("""
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

                    请严格按照以下 JSON 格式返回审核结果(不要添加任何其他说明文字)：
                    {
                        "result": "通过" 或 "拒绝" 或 "需人工审核",
                        "reason": "简要说明审核理由",
                        "riskLevel": "低" 或 "中" 或 "高"
                    }

                    判断标准：
                        "通过"：内容合规，无违规风险
                        "拒绝"：明确存在违规，风险等级为高
                        "需人工审核"：存在可疑内容但不确定，或风险等级为中

                    风险等级判断：
                        "低"：内容合规，无风险
                        "中"：存在轻微敏感或可疑内容，建议人工复审
                        "高"：明确违规，必须拒绝
                    """)
                .user("请审核以下文本内容：\n\n" + content)
                .call()
                .content();

            log.debug("AI 审核原始响应: {}", aiResponse);

            // 解析 AI 响应
            TextAutoResponse response = parseAiResponse(aiResponse, content);

            log.info("AI 审核完成 - 结果: {}, 风险等级: {}", response.getResult(), response.getRiskLevel());

            return response;

        } catch (Exception e) {
            log.error("AI 文本审核失败", e);
            return new TextAutoResponse(
                "需人工审核",
                "AI 审核异常: " + e.getMessage(),
                "中",
                content
            );
        }
    }

// 解析 AI 响应
    private TextAutoResponse parseAiResponse(String aiResponse, String content) {
        // 打印 AI 响应和原始内容文本
        log.info("----- 后台打印 - AI 响应内容 ------");
        log.info("AI响应: {}", aiResponse);
        log.info("原内容文本: {}", content);
        log.info("---------------------------------");

        try {
            // 清理响应内容，去除可能的 markdown 代码块标记
            // String cleanResponse = aiResponse
            //     .replaceAll("```json\\s*", "")
            //     .replaceAll("```\\s*", "")
            //     .trim();

            // 使用 Jackson 解析 JSON
            JsonNode jsonNode = objectMapper.readTree(aiResponse);

            String result = jsonNode.has("result") ? jsonNode.get("result").asText("未知") : "未知";
            String reason = jsonNode.has("reason") ? jsonNode.get("reason").asText("未知") : "未知";
            String riskLevel = jsonNode.has("riskLevel") ? jsonNode.get("riskLevel").asText("未知") : "未知";

            return new TextAutoResponse(result, reason, riskLevel, content);

        } catch (Exception e) {
            log.error("解析 AI 响应失败: {}", aiResponse, e);
            // 解析失败时，返回需要人工审核
            return new TextAutoResponse(
                "需人工审核",
                "AI 响应解析失败，建议人工审核",
                "中",
                content
            );
        }
    }
}