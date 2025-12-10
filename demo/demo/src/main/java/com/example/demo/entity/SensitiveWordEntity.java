package com.example.demo.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Data;

@Data
@Document(collection = "sensitive_words")
public class SensitiveWordEntity {

    @Id
    private String id;

    @Indexed(unique = true)
    @Field("word")
    private String word;

    @Indexed
    @Field("category")
    private String category;

    @Indexed
    @Field("level")
    private String level;

    @Field("createdAt")
    private LocalDateTime createdAt;
}
