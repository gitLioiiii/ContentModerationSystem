package com.example.demo.controller;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.TextAutoRequest;
import com.example.demo.dto.TextAutoResponse;
import com.example.demo.entity.ChatMessageEntity;
import com.example.demo.repository.ChatMessageRepository;
import com.example.demo.utils.ResultTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatMessageRepository chatMessageRepository;
    private final TextModerationController textModerationController;
    private final StringRedisTemplate stringRedisTemplate;
    private final ChannelTopic chatTopic;
    private final ObjectMapper objectMapper;

    public ChatController(ChatMessageRepository chatMessageRepository,
                          TextModerationController textModerationController,
                          StringRedisTemplate stringRedisTemplate,
                          ChannelTopic chatTopic,
                          ObjectMapper objectMapper) {
        this.chatMessageRepository = chatMessageRepository;
        this.textModerationController = textModerationController;
        this.stringRedisTemplate = stringRedisTemplate;
        this.chatTopic = chatTopic;
        this.objectMapper = objectMapper;
    }

    // 获取最近50条聊天记录
    @GetMapping("/history")
    public ResultTemplate getChatHistory() {
        ResultTemplate result = new ResultTemplate();
        List<ChatMessageEntity> messages = chatMessageRepository.findTop50ByOrderByCreatedAtDesc();
        // 反转为时间正序
        Collections.reverse(messages);
        result.putPayload("messages", messages);
        return result;
    }

    // 用户发送消息：先做文本合规检测，通过则入库并广播
    @PostMapping("/send")
    public ResultTemplate sendMessage(@RequestBody ChatMessageEntity message) {
        ResultTemplate result = new ResultTemplate();

        if (message == null || message.getContent() == null || message.getContent().trim().isEmpty()) {
            return result.setStatus(false).setMessage("消息内容不能为空");
        }

        // 调用 TextModerationController 完成 Ollama 文本审核
        TextAutoRequest moderationRequest = new TextAutoRequest();
        moderationRequest.setContent(message.getContent());
        // 手动调用 controller 时仍需传入 BindingResult，这里给一个空的占位
        BeanPropertyBindingResult bindingResult =
            new BeanPropertyBindingResult(moderationRequest, "textAutoRequest");

        TextAutoResponse moderation;
        try {
            ResultTemplate moderationResult =
                textModerationController.match_Ai_Text(moderationRequest, bindingResult);
            Map<String, Object> payload = moderationResult.getPayload();
            moderation = (TextAutoResponse) payload.get("moderation");
        } catch (Exception e) {
            log.error("文本审核调用失败", e);
            return result.setStatus(false).setMessage("文本审核服务异常，请稍后再试");
        }

        if (moderation == null || !"通过".equals(moderation.getResult())) {
            // 拦截：返回审核未通过信息
            String reason = moderation == null ? "未知原因" : moderation.getReason();
            String level = moderation == null ? "中" : moderation.getRiskLevel();
            String verdict = moderation == null ? "拦截" : moderation.getResult();
            log.warn("聊天消息被拦截 - 结果: {}, 原因: {}", verdict, reason);
            return result.setStatus(false)
                .setMessage("消息未通过审核：" + reason)
                .putPayload("result", verdict)
                .putPayload("reason", reason)
                .putPayload("riskLevel", level);
        }

        // 审核通过：补全字段并入库
        message.setCreatedAt(LocalDateTime.now());
        if (message.getType() == null) {
            message.setType("text");
        }
        ChatMessageEntity saved = chatMessageRepository.save(message);

        // 通过 Redis Pub/Sub 推送给所有在线节点的订阅者
        try {
            String json = objectMapper.writeValueAsString(saved);
            stringRedisTemplate.convertAndSend(chatTopic.getTopic(), json);
        } catch (Exception e) {
            log.error("Redis 推送聊天消息失败", e);
        }

        return result.setMessage("发送成功").putPayload("message", saved);
    }

}
