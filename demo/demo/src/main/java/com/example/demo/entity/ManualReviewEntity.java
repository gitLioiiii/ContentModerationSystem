package com.example.demo.entity;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Data;

// tag 人工审核表
@Data
@Document(collection = "manual_review")
@CompoundIndexes({
    @CompoundIndex(name = "contentId_reviewedAt", def = "{'contentId': 1, 'reviewedAt': -1}"),
    @CompoundIndex(name = "reviewerId_reviewedAt", def = "{'reviewerId': 1, 'reviewedAt': -1}")
})
public class ManualReviewEntity {

    @Id
    private String id;

    @Field("contentId")
    private String contentId; // 关联内容ID

    @Field("reviewerId")
    private String reviewerId; // 审核员ID

    @Indexed
    @Field("decision")
    private String decision; // 审核决定: pass(通过)/reject(驳回)

    @Field("reason")
    private String reason; // 驳回理由

    @Field("violatedItems")
    private List<ViolatedItem> violatedItems; // 违规的内容项（如果驳回）

    @Field("reviewType")
    private String reviewType; // 审核类型: first_review(首次审核)/appeal_review(申诉审核)

    @Field("reviewedAt")
    private LocalDateTime reviewedAt; // 审核完成时间

    /**
     * 违规的内容项
     */
    @Data
    public static class ViolatedItem {

        @Field("itemOrder")
        private Integer itemOrder; // 对应contentItems中的order

        @Field("itemType")
        private String itemType; // 内容项类型: text(文本)/image(图片)/video(视频)

        @Field("violationTypes")
        private List<String> violationTypes; // 该项的违规类型: sensitive_words(敏感词)/porn(色情)/violence(暴力)/political(政治)/spam_mail(垃圾邮件)/other(其他)

        @Field("note")
        private String note; // 针对该项的备注
    }
}
