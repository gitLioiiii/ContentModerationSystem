package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// 文本审核响应DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TextAutoResponse {

// 审核结果：通过、拒绝、需人工审核
    private String result;

// 审核理由
    private String reason;

// 风险等级：低、中、高
    private String riskLevel;

// 用户输入文本
    private String originalText;
}
