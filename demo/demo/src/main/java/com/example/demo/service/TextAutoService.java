package com.example.demo.service;

import com.example.demo.dto.TextAutoRequest;
import com.example.demo.dto.TextAutoResponse;
import com.example.demo.entity.SensitiveWordEntity;
import com.example.demo.utils.SensitiveWordUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TextAutoService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    private final SensitiveWordService sensitiveWordService;

    // 构造法
    public TextAutoService(ChatClient.Builder chatClientBuilder,
                        ObjectMapper objectMapper,
                        SensitiveWordService sensitiveWordService) {
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = new ObjectMapper();
        this.sensitiveWordService = sensitiveWordService;
    }

    // 接收文本
    public TextAutoResponse AiText(TextAutoRequest request) {
        String content = request.getContent();
        log.info("开始文本审核，文本长度: {} 字符", content.length());

        try {
            // 第一步：敏感词过滤
            log.info("执行敏感词过滤检查...");
            TextAutoResponse sensitiveWordCheckResult = checkSensitiveWords(content);
            if (sensitiveWordCheckResult != null) {
                // 发现敏感词，直接返回
                return sensitiveWordCheckResult;
            }

            // 第二步：敏感词检查通过，继续 AI 审核
            log.info("敏感词检查通过，开始 AI 审核...");
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

    // 敏感词检查方法
    private TextAutoResponse checkSensitiveWords(String content) {
        try {
            // 数据库获取所有敏感词
            List<SensitiveWordEntity> sensitiveWords = sensitiveWordService.fetch(new HashMap<>());

            if (sensitiveWords == null || sensitiveWords.isEmpty()) {
                log.warn("敏感词库为空，跳过敏感词检查");
                return null;
            }

            // 提取敏感词列表
            List<String> wordList = sensitiveWords.stream()
                .map(SensitiveWordEntity::getWord)
                .collect(Collectors.toList());

            log.info("加载敏感词库完成，共 {} 个敏感词", wordList.size());

            // 敏感词过滤工具
            SensitiveWordUtil sensitiveWordUtil = new SensitiveWordUtil(wordList);

            // 检查是否包含敏感词
            if (sensitiveWordUtil.containsSensitiveWord(content)) {
                // 查找所有敏感词
                List<SensitiveWordUtil.SensitiveWordResult> foundWords =
                    sensitiveWordUtil.findAllWords(content);

                // 构建敏感词列表字符串
                String foundWordsStr = foundWords.stream()
                    .map(SensitiveWordUtil.SensitiveWordResult::getWord)
                    .distinct()
                    .collect(Collectors.joining(", "));

                log.warn("文本包含敏感词: {}", foundWordsStr);

                // 获取匹配到的敏感词的最高级别
                String maxLevel = getMaxSensitiveWordLevel(foundWords, sensitiveWords);

                // 根据敏感词级别决定审核结果
                String result;
                String riskLevel;
                String reason;

                switch (maxLevel) {
                    case "high":
                        result = "拒绝";
                        riskLevel = "高";
                        reason = "文本包含高风险敏感词: " + foundWordsStr;
                        break;
                    case "medium":
                        result = "需人工审核";
                        riskLevel = "中";
                        reason = "文本包含中等风险敏感词: " + foundWordsStr;
                        break;
                    case "low":
                        result = "需人工审核";
                        riskLevel = "低";
                        reason = "文本包含低风险敏感词: " + foundWordsStr;
                        break;
                    default:
                        result = "需人工审核";
                        riskLevel = "中";
                        reason = "文本包含敏感词: " + foundWordsStr;
                }

                log.info("敏感词匹配结果 - 最高级别: {}, 审核结果: {}, 风险等级: {}", maxLevel, result, riskLevel);

                return new TextAutoResponse(result, reason, riskLevel, content);
            }

            log.info("未检测到敏感词");
            return null; // 未发现敏感词，返回 null 继续 AI 审核

        } catch (Exception e) {
            log.error("敏感词检查异常", e);
            // 发生异常时，为了安全起见，返回需要人工审核
            return new TextAutoResponse(
                "需人工审核",
                "敏感词检查异常: " + e.getMessage(),
                "中",
                content
            );
        }
    }

    // 获取一段文本匹配到的敏感词中的最高级别
    private String getMaxSensitiveWordLevel(List<SensitiveWordUtil.SensitiveWordResult> foundWords,
                                        List<SensitiveWordEntity> sensitiveWords) {
        // 创建敏感词到级别的映射
        HashMap<String, String> wordLevelMap = new HashMap<>();
        for (SensitiveWordEntity entity : sensitiveWords) {
            wordLevelMap.put(entity.getWord(), entity.getLevel());
        }

        // 找出所有匹配敏感词的级别
        boolean hasHigh = false;
        boolean hasMedium = false;
        boolean hasLow = false;

        log.info("----- 敏感词级别匹配调试 -------");
        for (SensitiveWordUtil.SensitiveWordResult result : foundWords) {
            String word = result.getWord();
            String level = wordLevelMap.get(word);
            log.info("匹配到的敏感词: '{}', 风险等级: '{}'", word, level);

            if (level != null) {
                switch (level) {
                    case "high":
                        hasHigh = true;
                        break;
                    case "medium":
                        hasMedium = true;
                        break;
                    case "low":
                        hasLow = true;
                        break;
                    default:
                        log.warn("未识别的级别: '{}'", level);
                }
            } else {
                log.warn("敏感词 '{}' 在数据库中找不到对应的级别", word);
            }
        }

        // 返回最高级别
        String maxLevel;
        if (hasHigh) {
            maxLevel = "high";
        } else if (hasMedium) {
            maxLevel = "medium";
        } else if (hasLow) {
            maxLevel = "low";
        } else {
            maxLevel = "medium"; // 默认中等风险
        }

        log.info("最终判定的最高级别: '{}' (hasHigh={}, hasMedium={}, hasLow={})", maxLevel, hasHigh, hasMedium, hasLow);
        log.info("------------------------------");

        return maxLevel;
    }
}