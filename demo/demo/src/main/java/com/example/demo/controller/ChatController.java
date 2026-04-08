package com.example.demo.controller;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.ChatMessageEntity;
import com.example.demo.repository.ChatMessageRepository;
import com.example.demo.utils.ResultTemplate;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatMessageRepository chatMessageRepository;

    public ChatController(ChatMessageRepository chatMessageRepository) {
        this.chatMessageRepository = chatMessageRepository;
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

    // 接收STOMP消息并广播到所有订阅者
    @MessageMapping("/chat.send")
    @SendTo("/topic/chat")
    public ChatMessageEntity sendMessage(ChatMessageEntity message) {
        message.setCreatedAt(LocalDateTime.now());
        if (message.getType() == null) {
            message.setType("text");
        }
        // 保存到数据库
        chatMessageRepository.save(message);
        return message;
    }

}
