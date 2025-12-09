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

// tag 内容表
// mark 支持5种场景：1.纯文本 2.纯图片 3.纯视频 4.文本+图片 5.文本+视频

@Data
@Document(collection = "content")
@CompoundIndexes({
    @CompoundIndex(name = "userId_createdAt", def = "{'userId': 1, 'createdAt': -1}"),
    @CompoundIndex(name = "status_createdAt", def = "{'status': 1, 'createdAt': -1}")
})
public class ContentEntity {

    @Id
    private String id;

    @Field("userId")
    private String userId; // 发布者的用户ID

    @Indexed
    @Field("categories")
    private List<String> categories; // 内容类别（技术、生活、娱乐、新闻等），支持多选

    @Field("contentItems")
    private List<ContentItem> contentItems; // 内容项数组

    @Indexed
    @Field("status")
    private String status; // 审核状态: pending(待审核)/passed(已通过)/rejected(已驳回)/reviewing(人工审核中)

    @Field("appealStatus")
    private String appealStatus = "none"; // 申诉状态: none(未申诉)/appealing(申诉中)/approved(申诉通过)/rejected(申诉驳回)

    @Field("violationCount")
    private Integer violationCount = 0; // 累计违规次数

    @Field("createdAt")
    private LocalDateTime createdAt;

    @Field("publishedAt")
    private LocalDateTime publishedAt;

    @Field("deletedAt")
    private LocalDateTime deletedAt;

    /**
     * 内容项
     */
    @Data
    public static class ContentItem {

        @Field("type")
        private String type; // 内容项类型: text(文本)/image(图片)/video(视频)

        @Field("order")
        private Integer order; // 内容项的显示顺序

        @Field("textContent")
        private String textContent; // 文本内容（当type为text时必填）

        @Field("mediaUrl")
        private String mediaUrl; // 媒体文件URL（当type为image或video时必填）

        @Field("thumbnailUrl")
        private String thumbnailUrl; // 缩略图URL（视频时可选）

        @Field("duration")
        private Integer duration; // 视频时长（秒，视频时可选）

        @Field("width")
        private Integer width; // 媒体宽度（像素，图片/视频时可选）

        @Field("height")
        private Integer height; // 媒体高度（像素，图片/视频时可选）
    }
}
