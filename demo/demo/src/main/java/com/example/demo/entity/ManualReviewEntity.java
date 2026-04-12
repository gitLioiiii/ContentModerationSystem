package com.example.demo.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

// 人工审核
@Data
@Document(collection = "manual_review")
@CompoundIndex(name = "contentId_reviewedAt", def = "{'contentId': 1, 'reviewedAt': -1}")
@CompoundIndex(name = "reviewerId_reviewedAt", def = "{'reviewerId': 1, 'reviewedAt': -1}")
public class ManualReviewEntity {

    @Id
    private String id;

    @Indexed
    private String contentId; // 关联作品ID

    private String workTitle; // 作品标题
    private String username; // 发布者用户名

    @Indexed
    private String reviewerId; // 审核员ID

    private String reviewerName; // 审核员姓名

    @Indexed
    private String decision; // 审核决定：pass通过/reject驳回

    private String reason; // 审核意见或驳回理由

    private String reviewType; // 审核类型: first_review首次审核/appeal_review申诉审核

    @Indexed
    private LocalDateTime reviewedAt; // 审核完成时间
}
