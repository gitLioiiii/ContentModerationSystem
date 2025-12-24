package com.example.demo.entity;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

// AI自动审核作品
@Data
@Document(collection = "auto_review_forworks")
public class WorkAutoReviewEntity {

    @Id
    private String id;

    @Indexed(unique = true)
    private String contentId; // 作品的ID

    private ReviewResults reviewResults; // 审核结果

    @Indexed
    private String Status; // 状态: approved通过/rejected驳回/reviewing需人工审核

    private Integer finalProcessingTime; // 作品审核处理耗时(秒)

    @Indexed
    private LocalDateTime reviewedAt; // 审核完成时间

    @Data
    public static class ReviewResults {
        private TextReview textReview;
        private ImageReview imageReview;
        private VideoReview videoReview;
    }

    // 文本审核（标题+描述）
    @Data
    public static class TextReview {
        private String result; // 通过/拒绝/人工审核
        private String reason; // 审核理由
        private String riskLevel; // 风险：低/中/高
        private List<String> sensitiveWords; // 命中的敏感词列表
        private Integer processingTime; // 处理耗时(毫秒)
    }

    // 封面审核
    @Data
    public static class ImageReview {
        private String result; // 通过/不通过/人工审核
        private String reason; // 审核理由
        private Double matchScore; // 违规匹配分数
        private Integer processingTime; // 处理耗时(毫秒)
    }

    // 视频审核
    @Data
    public static class VideoReview {
        private String result; // 通过/不通过/人工审核
        private String reason; // 审核理由
        private Integer Count; // 视频抽帧总数
        private Integer riskyCount; // 检测到的风险帧数量
        private Double maxScore; // 所有帧中的最高风险分数
        private List<RiskyFrame> riskyList; // 风险帧详情列表
        private Integer processingTime; // 处理耗时(秒)
    }

    // 抽出来的风险帧
    @Data
    public static class RiskyFrame {
        private Integer frameIndex; // 帧索引
        private Double timestamp; // 时间戳(秒)
        private Double score; // 该帧的风险分数
        private String reason; // 该帧的风险理由
    }
}
