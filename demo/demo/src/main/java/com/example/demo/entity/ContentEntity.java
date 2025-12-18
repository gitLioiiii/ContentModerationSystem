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

    private String userId; // 用户ID
    private String title; // 标题
    private String coverUrl; // 封面图片
    private String videoUrl; // 视频文件URL
    private String description; // 作品描述
    private String authorAvatar; // 作者头像
    private String author; // 用户名

    private Location location; // 用户地理位置

    @Indexed
    private String status; // 审核状态: pending(待审核)/approved(已通过)/rejected(已驳回)
    private String appealStatus = "none"; // 申诉状态: none(未申诉)/appealing(申诉中)/approved(申诉通过)/rejected(申诉驳回)
    private Integer violationCount = 0; // 累计违规次数
    private LocalDateTime createdAt;
    private LocalDateTime deletedAt;

//  地理位置
    @Data
    public static class Location {
        private String province; // 省份
        private String city; // 城市
    }
}
