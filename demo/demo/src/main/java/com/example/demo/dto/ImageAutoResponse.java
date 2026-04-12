package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// 图片审核响应DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageAutoResponse {

    // 审核结果：通过、不通过、人工审核
    private String result;

    // 审核理由
    private String reason;

    // 匹配分数：分数越高表示涉及色情/暴力/政治敏感的可能性越大，越不能通过审核
    private Double matchScore;
}
