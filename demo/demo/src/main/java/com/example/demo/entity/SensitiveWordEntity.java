package com.example.demo.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.example.demo.validator.SensitiveWordValidateGroup;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Document(collection = "sensitive_words")
public class SensitiveWordEntity {

    @Id
    @NotBlank(message = "ID不能为空", groups = {
        SensitiveWordValidateGroup.Update.class,
        SensitiveWordValidateGroup.Remove.class
    })
    private String id;

    @Indexed(unique = true)
    @NotBlank(message = "敏感词不能为空", groups = {
        SensitiveWordValidateGroup.Create.class,
        SensitiveWordValidateGroup.Update.class
    })
    private String word;

    @NotBlank(message = "分类不能为空", groups = {
        SensitiveWordValidateGroup.Create.class
    })
    private String category;

    @NotBlank(message = "级别不能为空", groups = {
        SensitiveWordValidateGroup.Create.class
    })
    private String level;

    private LocalDateTime createdAt;
}
