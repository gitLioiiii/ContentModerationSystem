package com.example.demo.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.example.demo.entity.ChatMessageEntity;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

// 监听 Redis 聊天频道，把消息广播给在线的 WebSocket 客户端
@Slf4j
@Component
public class ChatMessageSubscriber {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    public ChatMessageSubscriber(SimpMessagingTemplate messagingTemplate,
                                 ObjectMapper objectMapper) {
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = objectMapper;
    }

    // Redis 发布的消息回调（序列化时已带类型信息，这里收到的是 JSON 字符串）
    public void onMessage(String body) {
        try {
            // GenericJackson2JsonRedisSerializer 写出的是带类型的 JSON
            ChatMessageEntity message = objectMapper.readValue(body, ChatMessageEntity.class);
            messagingTemplate.convertAndSend("/topic/chat", message);
        } catch (Exception e) {
            log.error("Redis 聊天消息反序列化或广播失败, body={}", body, e);
        }
    }
}
