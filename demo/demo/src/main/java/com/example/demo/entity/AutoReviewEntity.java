package com.example.demo.entity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Data;

// tag AI自动审核表
@Data
@Document(collection = "auto_review")
@CompoundIndexes({
    @CompoundIndex(name = "riskLevel_reviewedAt", def = "{'riskLevel': 1, 'reviewedAt': -1}")
})
public class AutoReviewEntity {

    @Id
    private String id;

    @Indexed
    @Field("contentId")
    private String contentId; // 关联内容ID

    @Field("itemReviews")
    private List<ItemReview> itemReviews; // 每个内容项的审核结果

    @Field("overallScore")
    private Double overallScore; // 整体审核得分（通常取最低分或加权平均）(0-100)

    @Field("overallRiskLevel")
    private String overallRiskLevel; // 整体风险等级: safe(安全)/suspected(疑似)/risky(高风险)

    @Field("totalProcessingTime")
    private Integer totalProcessingTime; // 总处理耗时(毫秒)

    @Indexed
    @Field("reviewedAt")
    private LocalDateTime reviewedAt; // 审核时间

    /**
     * 内容项审核结果
     */
    @Data
    public static class ItemReview {

        @Field("itemOrder")
        private Integer itemOrder; // 对应contentItems中的order

        @Field("itemType")
        private String itemType; // 内容项类型: text(文本)/image(图片)/video(视频)

        @Field("score")
        private Double score; // 该内容项的审核得分 (0-100)

        @Field("riskLevel")
        private String riskLevel; // 该内容项的风险等级：safe(安全)/suspected(疑似)/risky(高风险)

        @Field("keywords")
        private List<String> keywords; // 命中的敏感词（文本类型）

        @Field("violationTypes")
        private List<String> violationTypes; // 违规类型: sensitive_words(敏感词)/porn(色情)/violence(暴力)/political(政治)/spam_mail(垃圾邮件)

        @Field("details")
        private Map<String, Object> details; // 详细检测结果（视频时间戳、图片区域坐标等）

        @Field("processingTime")
        private Integer processingTime; // 该项处理耗时(毫秒)
    }
}
