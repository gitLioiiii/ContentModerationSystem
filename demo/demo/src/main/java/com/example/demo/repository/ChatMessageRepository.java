package com.example.demo.repository;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
// import org.springframework.stereotype.Repository;

import com.example.demo.entity.ChatMessageEntity;

// @Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessageEntity, String> {

    // 查询最近的聊天记录（按时间排序）
    List<ChatMessageEntity> findTop50ByOrderByCreatedAtDesc();

    // 查询所有消息按时间排序
    List<ChatMessageEntity> findAll(Sort sort);

}
