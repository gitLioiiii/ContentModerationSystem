package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// 视频审核的总体结论和关键帧信息
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoModerationResponse {

    // 总体审核结论：通过、不通过、人工审核
    private String overallResult;

    // 总帧数（抽取的帧总数）
    private Integer totalFrames;

    // 违规帧数
    private Integer violationFrames;

    // 违规率（百分比）
    private Double violationRate;

    // 审核原因（总体原因）
    private String reason;

    // 关键帧列表（包含违规帧和部分正常帧）
    private List<VideoFrameInfo> keyFrames;
}
