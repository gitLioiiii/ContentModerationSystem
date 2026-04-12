package com.example.demo.dto;

import lombok.Data;

// 视频帧提取结果
@Data
public class FrameExtractionResult {

    // 帧序号
    private Integer frameNumber;

    // 时间戳
    private Double timestamp;

    // 文件名
    private String filename;

    // 文件绝对路径
    private String filePath;
}
