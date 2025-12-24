package com.example.demo.controller;

import com.example.demo.dto.FrameExtractionResult;
import com.example.demo.dto.ImageAutoRequest;
import com.example.demo.dto.ImageAutoResponse;
import com.example.demo.dto.VideoFrameInfo;
import com.example.demo.dto.VideoModerationResponse;
import com.example.demo.service.AsyncImageModerationService;
import com.example.demo.service.VideoFrameExtractorService;
import com.example.demo.utils.ResultTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

// 对视频抽帧。然后使用ai对抽帧出来的图片进行审核
@Slf4j
@RestController
@RequestMapping("/moderation")
public class VideoModerationController {

    private final VideoFrameExtractorService frameExtractorService;
    private final AsyncImageModerationService asyncImageModerationService;

    @Value("${application.upload-root}")
    private String uploadRoot;

    // 构造方法
    public VideoModerationController(
            VideoFrameExtractorService frameExtractorService,
            AsyncImageModerationService asyncImageModerationService) {
        this.frameExtractorService = frameExtractorService;
        this.asyncImageModerationService = asyncImageModerationService;
    }

    // 视频审核
    @PostMapping("/video")
    public ResultTemplate moderateVideo(
            @RequestParam("file") MultipartFile multipartFile,
            @RequestParam(value = "frameInterval", defaultValue = "2") Integer frameInterval
    ) {
        log.info("收到视频审核请求 - 文件名: {}, 大小: {} bytes, 抽帧间隔: {}秒",
                multipartFile.getOriginalFilename(),
                multipartFile.getSize(),
                frameInterval);

        try {
            // 第一步：保存上传的视频文件
            File videoDir = new File(uploadRoot + File.separator + "ai-videos");
            if (!videoDir.exists()) {
                videoDir.mkdirs();
            }

            // 生成UUID文件名
            String filename = UUID.randomUUID().toString();
            String extension = multipartFile.getOriginalFilename();
            if (extension != null && extension.contains(".")) {
                filename = filename + extension.substring(extension.lastIndexOf("."));
            }

            File savedVideoFile = new File(videoDir, filename);
            multipartFile.transferTo(savedVideoFile);
            log.info("视频已保存到: {}", savedVideoFile.getAbsolutePath());

            // 验证视频文件
            if (!savedVideoFile.exists()) {
                log.error("视频文件不存在: {}", savedVideoFile.getAbsolutePath());
                throw new Exception("视频文件不存在: " + savedVideoFile.getAbsolutePath());
            }
            log.info("视频文件大小: {} MB", savedVideoFile.length() / (1024.0 * 1024.0));

            // 从视频中提取关键帧
            log.info("开始抽取视频帧...");

            // 准备帧存储目录（统一由控制器管理路径）
            File frameDir = new File(uploadRoot + File.separator + "ai-videos");

            // 调用服务层进行视频抽帧，传入帧保存目录
            List<FrameExtractionResult> extractedFrames =
                    frameExtractorService.extractFrames(
                        savedVideoFile.getAbsolutePath(),
                        frameInterval,
                        frameDir.getAbsolutePath()
                    );

            int totalFrames = extractedFrames.size();
            log.info("共提取 {} 帧图片", totalFrames);

            if (totalFrames == 0) {
                return new ResultTemplate()
                    .setStatus(false)
                    .setMessage("视频抽帧失败，未能提取到任何帧");
            }

            // 并行对每一帧进行AI审核
            log.info("开始并行审核 {} 帧图片...", totalFrames);

            // 创建异步任务列表
            List<CompletableFuture<ImageAutoResponse>> asyncTasks = new ArrayList<>();

            for (FrameExtractionResult frame : extractedFrames) {
                // 构建图片审核请求
                ImageAutoRequest imageRequest = new ImageAutoRequest();
                imageRequest.setImageUrl(frame.getFilename());
                imageRequest.setFilePath(frame.getFilePath());
                imageRequest.setMimeType("image/jpeg");

                // 提交异步任务到线程池
                CompletableFuture<ImageAutoResponse> asyncTask =
                    asyncImageModerationService.moderateImageAsync(imageRequest);
                asyncTasks.add(asyncTask);
            }

            // 等待所有异步任务完成
            log.info("等待所有审核任务完成...");
            CompletableFuture<Void> allTasks = CompletableFuture.allOf(
                asyncTasks.toArray(new CompletableFuture[0])
            );

            // 阻塞等待所有任务完成（最多5分钟）
            allTasks.get(5, java.util.concurrent.TimeUnit.MINUTES);
            log.info("所有审核任务已完成");

            // 收集审核结果
            List<VideoFrameInfo> allFrames = new ArrayList<>();
            int violationCount = 0;
            double totalMatchScore = 0.0;
            double maxMatchScore = 0.0;

            for (int i = 0; i < extractedFrames.size(); i++) {
                FrameExtractionResult frame = extractedFrames.get(i);
                ImageAutoResponse imageResponse = asyncTasks.get(i).get(); // 获取异步结果

                // 构建帧信息
                VideoFrameInfo frameInfo = new VideoFrameInfo();
                frameInfo.setFrameNumber(frame.getFrameNumber());
                frameInfo.setTimestamp(frame.getTimestamp());
                frameInfo.setResult(imageResponse.getResult());
                frameInfo.setReason(imageResponse.getReason());
                frameInfo.setFilename(frame.getFilename());
                frameInfo.setMatchScore(imageResponse.getMatchScore());

                allFrames.add(frameInfo);

                // 统计违规帧
                if ("不通过".equals(imageResponse.getResult()) ||
                        "人工审核".equals(imageResponse.getResult())) {
                    violationCount++;
                }

                // 累加总分数并更新最大分数
                totalMatchScore += imageResponse.getMatchScore();
                if (imageResponse.getMatchScore() > maxMatchScore) {
                    maxMatchScore = imageResponse.getMatchScore();
                }

                log.info("帧 {} ({}秒) 审核结果: {} - {}",
                        frame.getFrameNumber(),
                        String.format("%.2f", frame.getTimestamp()),
                        imageResponse.getResult(),
                        imageResponse.getReason());
            }

            double violationRate = (totalFrames > 0) ? (violationCount * 100.0 / totalFrames) : 0.0;
            double avgMatchScore = (totalFrames > 0) ? (totalMatchScore / totalFrames) : 0.0;

            // 确定总体审核结论
            String overallResult;
            String overallReason;

            if (violationRate >= 30.0 || avgMatchScore >= 70.0 || maxMatchScore >= 85.0) {
                // 违规率超过30% 或 平均分数>=70 或 最高分数>=85，判定为不通过
                overallResult = "不通过";
                overallReason = String.format("视频包含违规内容。违规帧数: %d/%d (%.2f%%)，平均分数: %.2f，最高分数: %.2f",
                        violationCount, totalFrames, violationRate, avgMatchScore, maxMatchScore);
            } else if (violationRate >= 10.0 || avgMatchScore >= 30.0 || maxMatchScore >= 60.0) {
                // 违规率10-30% 或 平均分数30-70 或 最高分数60-85，需要人工审核
                overallResult = "人工审核";
                overallReason = String.format("视频存在疑似违规内容，建议人工复审。违规帧数: %d/%d (%.2f%%)，平均分数: %.2f，最高分数: %.2f",
                        violationCount, totalFrames, violationRate, avgMatchScore, maxMatchScore);
            } else {
                // 违规率<10% 且 平均分数<30 且 最高分数<60，判定为通过
                overallResult = "通过";
                overallReason = String.format("视频内容健康，无明显违规。违规帧数: %d/%d (%.2f%%)，平均分数: %.2f，最高分数: %.2f",
                        violationCount, totalFrames, violationRate, avgMatchScore, maxMatchScore);
            }

            // 选择关键帧（违规帧 + 部分正常帧，最多显示10帧）
            List<VideoFrameInfo> keyFrames = selectKeyFrames(allFrames, 10);

            // 构建响应对象
            VideoModerationResponse response = new VideoModerationResponse();
            response.setOverallResult(overallResult);
            response.setTotalFrames(totalFrames);
            response.setViolationFrames(violationCount);
            response.setAvgScore(Math.round(avgMatchScore * 100.0) / 100.0); // 保留2位小数
            response.setMaxScore(Math.round(maxMatchScore * 100.0) / 100.0); // 保留2位小数
            response.setReason(overallReason);
            response.setKeyFrames(keyFrames);

            log.info("视频审核完成 - 总体结论: {}, 总帧数: {}, 违规帧数: {}, 平均分数: {}, 最高分数: {}",
                    overallResult, totalFrames, violationCount,
                    String.format("%.2f", avgMatchScore), String.format("%.2f", maxMatchScore));

            ResultTemplate result = new ResultTemplate();
            result.putPayload("moderation", response);

            // 删除临时视频文件（可选）
            try {
                if (savedVideoFile.exists()) {
                    savedVideoFile.delete();
                    log.info("已删除临时视频文件: {}", savedVideoFile.getName());
                }
            } catch (Exception e) {
                log.warn("删除临时视频文件失败", e);
            }

            return result;

        } catch (Exception e) {
            log.error("视频审核失败", e);
            return new ResultTemplate()
                .setStatus(false)
                .setMessage("视频审核失败: " + e.getMessage());
        }
    }

    // 优先选择违规帧，然后选择部分正常帧
    private List<VideoFrameInfo> selectKeyFrames(List<VideoFrameInfo> allFrames, int maxCount) {
        List<VideoFrameInfo> keyFrames = new ArrayList<>();

        // 优先选择违规帧
        for (VideoFrameInfo frame : allFrames) {
            if ("不通过".equals(frame.getResult()) || "人工审核".equals(frame.getResult())) {
                keyFrames.add(frame);
                if (keyFrames.size() >= maxCount) {
                    return keyFrames;
                }
            }
        }

        // 如果违规帧不足，补充正常帧
        int step = Math.max(1, allFrames.size() / (maxCount - keyFrames.size()));
        for (int i = 0; i < allFrames.size() && keyFrames.size() < maxCount; i += step) {
            VideoFrameInfo frame = allFrames.get(i);
            if (!"不通过".equals(frame.getResult()) && !"人工审核".equals(frame.getResult())) {
                keyFrames.add(frame);
            }
        }

        return keyFrames;
    }
}
