package com.example.demo.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "content")
// 倒序
@CompoundIndex(name = "userId_createdAt", def = "{'userId': 1, 'createdAt': -1}")
public class ContentEntity {

    @Id
    private String id;

    private String userId;

    private String title; 
    private String coverUrl; 
    private String videoUrl; 
    private String description;
    private String authorAvatar;
    private String author;

    private Location location; 

    @Indexed
    private String status; // 审核状态: pending(待审核)/approved(已通过)/rejected(已驳回)/appealing(申诉中)
    private String appealStatus = "none"; // 申诉状态: none(未申诉)/appealing(申诉中)/approved(申诉通过)/rejected(申诉驳回)
    private Integer violationCount = 0; // 累计违规的次数
    private LocalDateTime createdAt;
    private LocalDateTime deletedAt;

//  地理位置
    @Data
    public static class Location {
        private String province; // 省份
        private String city; // 城市
    }
}
