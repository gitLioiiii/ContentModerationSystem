package com.example.demo.controller;

import com.example.demo.dto.ImageAutoRequest;
import com.example.demo.dto.ImageAutoResponse;
import com.example.demo.service.ImageAutoService;
import com.example.demo.utils.ResultTemplate;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

// 图像内容审核控制器
// 上传图片后AI识别色情/暴力/政治敏感内容
@Slf4j
@RestController
@RequestMapping("/moderation")
public class ImageModerationController {

    // AI图像审核服务
    private final ImageAutoService imageAutoService;

    // 获取配置好的上传根目录
    @Value("${application.upload-root}")
    private String uploadRoot;

    // 构造法
    public ImageModerationController(ImageAutoService imageAutoService) {
        this.imageAutoService = imageAutoService;
    }

    /**
     * 图片审核接口
     * 请求路径：POST /moderation/image
     * 参数：file（图片文件）
     * 返回：审核结果（包含文件信息和AI审核结论）
     */
    @PostMapping("/image")
    public ResultTemplate moderateImage(
        @RequestParam("file") MultipartFile multipartFile
    ) throws IllegalStateException, IOException {

        log.info("收到图片审核请求，原始文件名: {}", multipartFile.getOriginalFilename());
        
        // 创建存储目录（如果不存在）
        File path = new File(this.uploadRoot + File.separator + "ai-images");
        if (!path.exists()) {
            path.mkdirs();
        }

        // 生成UUID文件名
        String filename = UUID.randomUUID().toString();
        String extension = multipartFile.getOriginalFilename();

        // 文件扩展名（.png、.jpg等）
        if (extension != null) {
            filename = filename + extension.substring(extension.indexOf("."));
        }

        // 保存文件
        File savedFile = new File(path, filename);
        multipartFile.transferTo(savedFile);
        log.info("图片已保存到: {}", savedFile.getAbsolutePath());

        // 验证文件是否成功保存
        if (!savedFile.exists()) {
            log.error("图片文件保存失败: {}", savedFile.getAbsolutePath());
            throw new IOException("图片文件保存失败");
        }
        log.info("图片文件大小: {} MB", savedFile.length() / (1024.0 * 1024.0));

        //调用ai审核

        // 2.1 构建请求对象，分别设置两个路径：
        //     - imageUrl: 前端显示用的相对URL（如 /ai-images/xxx.png）
        String imageUrl = "/ai-images/" + filename;  // 前端显示用
        String imagePath = savedFile.getAbsolutePath();  // AI读取用

        // 2.2 创建AI审核请求对象
        ImageAutoRequest aiRequest = new ImageAutoRequest();
        aiRequest.setImageUrl(imageUrl);  // 用于前端显示
        aiRequest.setFilePath(imagePath);  // 用于AI读取文件
        // 设置MIME类型（从MultipartFile获取或根据扩展名推断）
        String mimeType = multipartFile.getContentType();
        if (mimeType == null || mimeType.isEmpty()) {
            // 如果无法获取MIME类型，根据文件扩展名推断
            if (filename.toLowerCase().endsWith(".jpg") || filename.toLowerCase().endsWith(".jpeg")) {
                mimeType = "image/jpeg";
            } else if (filename.toLowerCase().endsWith(".png")) {
                mimeType = "image/png";
            }else {
                mimeType = "image/png"; // 默认为png
            }
        }
        aiRequest.setMimeType(mimeType);

        // 2.3 调用AI服务进行审核（识别色情/暴力/政治敏感内容）
        log.info("开始调用AI服务审核图片 - 显示URL: {}, 文件路径: {}, MIME类型: {}",
            imageUrl, imagePath, mimeType);
        ImageAutoResponse aiResponse = imageAutoService.AiImage(aiRequest);
        log.info("AI审核完成 - 结果: {}, 匹配分数: {}, 理由: {}",
            aiResponse.getResult(), aiResponse.getMatchScore(), aiResponse.getReason());


        // ==== 第三步：封装返回结果 ====

        ResultTemplate result = new ResultTemplate();

        // 3.1 返回文件信息
        result.putPayload("filename", "/ai-images/" + filename);  // 相对路径（前端用于显示）
        result.putPayload("originalFilename", multipartFile.getOriginalFilename());  // 原始文件名

        // 3.2 返回AI审核结果
        result.putPayload("moderation", aiResponse);  // 包含 result、reason、matchScore

        log.info("图片审核完成，返回结果给前端");
        return result;
    }
}
