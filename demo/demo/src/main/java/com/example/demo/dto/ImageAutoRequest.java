package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

// 图片审核请求DTO
@Data
public class ImageAutoRequest {

    // 图片URL地址（用于前端显示）
    @NotBlank(message = "图片URL不能为空")
    private String imageUrl;

    // 图片本地文件路径（用于AI服务读取）
    private String filePath;

    // 图片类型（.png, .jpg, .jpeg, .webp）
    private String mimeType;
}
