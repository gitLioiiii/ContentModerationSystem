package com.example.demo.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

// 审核阈值配置表
// 只做一条记录，可以修改，但不添加
@Data
@Document(collection = "review_config")
public class ReviewConfigEntity {

    @Id
    private String id;

    // 图片审核阈值
    // 图片通过阈值（matchScore低于此值判定为通过）
    private int imagePassThreshold = 30;
    // 图片拒绝阈值（matchScore高于等于此值判定为拒绝）
    private int imageRejectThreshold = 70;

    // 视频审核阈值
    // 视频拒绝-违规率阈值（%）
    private double videoRejectViolationRate = 30.0;
    // 视频拒绝-最高风险分数阈值
    private double videoRejectMaxScore = 70.0;
    // 视频人工审核-违规率阈值（%）
    private double videoManualViolationRate = 10.0;
    // 视频人工审核-最高风险分数阈值
    private double videoManualMaxScore = 30.0;

    // 视频抽帧配置
    // 抽帧间隔（秒）
    private int videoFrameInterval = 3;

    // 更新时间
    private LocalDateTime updatedAt;

}
