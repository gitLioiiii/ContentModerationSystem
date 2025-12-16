package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// 存储单个关键帧的审核信息
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoFrameInfo {

    // 帧的序号
    private Integer frameNumber;

    // 时间戳（秒）
    private Double timestamp;

    // 审核结果：通过、不通过、人工审核
    private String result;

    // 审核理由
    private String reason;

    // 帧图片文件名（相对路径，如 /ai-videos/xxx.jpg）
    private String filename;

    // 匹配分数：分数越高表示涉及色情/暴力/政治敏感的可能性越大
    private Double matchScore;
}
