package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// 视频审核
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoModerationResponse {

    // 审核状态：通过、不通过、人工审核
    private String overallResult;

    // 总帧数（抽取的帧总数）
    private Integer totalFrames;

    // 违规帧数
    private Integer violationFrames;

    // 平均风险分数（所有帧的平均分，0-100）
    private Double avgScore;

    // 最高风险分数（所有帧中的最大分，0-100）
    private Double maxScore;

    // 审核原因（总体原因）
    private String reason;

    // 关键帧列表（包含违规帧和正常帧）
    private List<VideoFrameInfo> keyFrames;
}
