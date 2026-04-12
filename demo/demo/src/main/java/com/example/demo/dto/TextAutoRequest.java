package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

// 文本审核请求DTO
@Data
public class TextAutoRequest {

// 待审核的文本内容
    @NotBlank(message = "文本内容不能为空")
    private String content;
}
