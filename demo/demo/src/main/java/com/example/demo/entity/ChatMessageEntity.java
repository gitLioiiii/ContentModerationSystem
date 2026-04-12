package com.example.demo.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

// 聊天消息表
@Data
@Document(collection = "chat_messages")
public class ChatMessageEntity {

    @Id
    private String id;

    // 发送者用户ID
    @Indexed
    private String userId;

    // 发送者用户名
    private String username;

    // 发送者昵称
    private String name;

    // 发送者头像
    private String avatar;

    // 消息内容
    private String content;

    // 消息类型: text(普通消息) / system(系统消息)
    private String type = "text";

    // 发送时间
    @Indexed
    private LocalDateTime createdAt;

}
