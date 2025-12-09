package com.example.demo.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Data;

/**
 * 敏感词实体类
 */
@Data
@Document(collection = "sensitive_words")
public class SensitiveWordEntity {

    @Id
    private String id;

    /**
     * 敏感词内容
     */
    @Indexed(unique = true)
    @Field("word")
    private String word;

    /**
     * 分类：政治/色情/暴恐/涉枪涉爆/广告/民生/贪腐/COVID-19/其他
     */
    @Indexed
    @Field("category")
    private String category;

    /**
     * 严重程度：high/medium/low
     * high：直接拒绝
     * medium：需人工审核
     * low：警告但通过
     */
    @Indexed
    @Field("level")
    private String level;

    /**
     * 是否启用
     */
    @Field("enabled")
    private Boolean enabled = true;

    /**
     * 创建时间
     */
    @Field("createdAt")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Field("updatedAt")
    private LocalDateTime updatedAt;

    /**
     * 备注
     */
    @Field("remark")
    private String remark;
}
