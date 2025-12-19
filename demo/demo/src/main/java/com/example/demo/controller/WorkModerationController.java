package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.entity.ContentEntity;
import com.example.demo.entity.WorkAutoReviewEntity;
import com.example.demo.repository.ContentRepository;
import com.example.demo.service.*;
import com.example.demo.utils.ResultTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

// 作品综合审核控制器
// 对作品进行文本（标题+描述）、图片（封面）、视频的综合AI审核
@Slf4j
@RestController
@RequestMapping("/moderation")
public class WorkModerationController {

    private final TextAutoService textAutoService;
    private final ImageAutoService imageAutoService;
    private final VideoFrameExtractorService videoFrameExtractorService;
    private final AsyncImageModerationService asyncImageModerationService;
    private final ContentRepository contentRepository;
    private final WorkAutoReviewService workAutoReviewService;

    @Value("${application.upload-root}")
    private String uploadRoot;

    // 构造方法
    public WorkModerationController(
            TextAutoService textAutoService,
            ImageAutoService imageAutoService,
            VideoFrameExtractorService videoFrameExtractorService,
            AsyncImageModerationService asyncImageModerationService,
            ContentRepository contentRepository,
            WorkAutoReviewService workAutoReviewService) {
        this.textAutoService = textAutoService;
        this.imageAutoService = imageAutoService;
        this.videoFrameExtractorService = videoFrameExtractorService;
        this.asyncImageModerationService = asyncImageModerationService;
        this.contentRepository = contentRepository;
        this.workAutoReviewService = workAutoReviewService;
    }

    // 作品综合审核
    @PostMapping("/work")
    public ResultTemplate moderateWork(@RequestParam("contentId") String contentId) {
        log.info("收到作品审核请求 - 作品ID: {}", contentId);

        long startTime = System.currentTimeMillis();

        try {
            // ----- 第一步：查询作品信息 -----
            Optional<ContentEntity> contentOpt = contentRepository.findByIdAndDeletedAtIsNull(contentId);
            if (!contentOpt.isPresent()) {
                log.error("作品不存在或已删除: {}", contentId);
                return new ResultTemplate()
                        .setStatus(false)
                        .setMessage("作品不存在或已删除");
            }

            ContentEntity content = contentOpt.get();
            log.info("开始审核作品 - 标题: {}, 用户: {}", content.getTitle(), content.getAuthor());

            // ----- 第二步：对（标题+描述）进行文本审核-----
            log.info("开始文本审核...");
            long textStartTime = System.currentTimeMillis();

            String textContent = content.getTitle();
            if (content.getDescription() != null && !content.getDescription().isEmpty()) {
                textContent += "\n" + content.getDescription();
            }

            TextAutoRequest textRequest = new TextAutoRequest();
            textRequest.setContent(textContent);
            TextAutoResponse textResponse = textAutoService.AiText(textRequest);

            long textProcessingTime = System.currentTimeMillis() - textStartTime;
            log.info("文本审核完成 - 结果: {}, 风险等级: {}, 耗时: {}ms",
                    textResponse.getResult(), textResponse.getRiskLevel(), textProcessingTime);

            // -----第三步：对封面图片进行审核-----
            WorkAutoReviewEntity.ImageReview imageReview = null;
            if (content.getCoverUrl() != null && !content.getCoverUrl().isEmpty()) {
                log.info("开始封面图片审核...");
                long imageStartTime = System.currentTimeMillis();

                String coverPath = uploadRoot + File.separator + content.getCoverUrl().replace("/", File.separator);
                File coverFile = new File(coverPath);

                if (coverFile.exists()) {
                    ImageAutoRequest imageRequest = new ImageAutoRequest();
                    imageRequest.setImageUrl(content.getCoverUrl());
                    imageRequest.setFilePath(coverFile.getAbsolutePath());
                    imageRequest.setMimeType("image/jpeg");

                    ImageAutoResponse imageResponse = imageAutoService.AiImage(imageRequest);

                    long imageProcessingTime = System.currentTimeMillis() - imageStartTime;
                    log.info("封面审核完成 - 结果: {}, 匹配分数: {}, 耗时: {}ms",
                            imageResponse.getResult(), imageResponse.getMatchScore(), imageProcessingTime);

                    // 构建图片审核结果
                    imageReview = new WorkAutoReviewEntity.ImageReview();
                    imageReview.setResult(imageResponse.getResult());
                    imageReview.setReason(imageResponse.getReason());
                    imageReview.setMatchScore(imageResponse.getMatchScore());
                    imageReview.setProcessingTime((int) imageProcessingTime);
                } else {
                    log.warn("封面文件不存在: {}", coverPath);
                }
            }

            // ---- 第四步：对视频进行抽帧审核 ------
            WorkAutoReviewEntity.VideoReview videoReview = null;
            if (content.getVideoUrl() != null && !content.getVideoUrl().isEmpty()) {
                log.info("开始视频审核...");
                long videoStartTime = System.currentTimeMillis();

                String videoPath = uploadRoot + File.separator + content.getVideoUrl().replace("/", File.separator);
                File videoFile = new File(videoPath);

                if (videoFile.exists()) {
                    log.info("视频文件大小: {} MB", videoFile.length() / (1024.0 * 1024.0));

                    // 存储的路径
                    File frameDir = new File(uploadRoot + File.separator + "ai-videos");
                    if (!frameDir.exists()) {
                        frameDir.mkdirs();
                    }

                    // 视频抽帧
                    List<FrameExtractionResult> extractedFrames = videoFrameExtractorService.extractFrames(
                            videoFile.getAbsolutePath(),
                            3, //抽帧间隔为3秒
                            frameDir.getAbsolutePath()
                    );

                    int totalFrames = extractedFrames.size();
                    log.info("共提取 {} 帧图片", totalFrames);

                    if (totalFrames > 0) {
                        // 连接池审核所有帧
                        List<CompletableFuture<ImageAutoResponse>> asyncTasks = new ArrayList<>();

                        for (FrameExtractionResult frame : extractedFrames) {
                            ImageAutoRequest imageRequest = new ImageAutoRequest();
                            imageRequest.setImageUrl(frame.getFilename());
                            imageRequest.setFilePath(frame.getFilePath());
                            imageRequest.setMimeType("image/jpeg");

                            CompletableFuture<ImageAutoResponse> asyncTask =
                                    asyncImageModerationService.moderateImageAsync(imageRequest);
                            asyncTasks.add(asyncTask);
                        }

                        // 等待所有任务完成
                        CompletableFuture<Void> allTasks = CompletableFuture.allOf(
                                asyncTasks.toArray(new CompletableFuture[0])
                        );
                        // 等待任务结束5分钟超时
                        allTasks.get(5, java.util.concurrent.TimeUnit.MINUTES);

                        // 审核结果
                        int riskyFrameCount = 0;
                        double maxScore = 0.0;
                        List<WorkAutoReviewEntity.RiskyFrame> riskyFrames = new ArrayList<>();

                        for (int i = 0; i < extractedFrames.size(); i++) {
                            FrameExtractionResult frame = extractedFrames.get(i);
                            ImageAutoResponse imageResponse = asyncTasks.get(i).get();

                            // 统计风险帧（不通过或人工审核）
                            if ("不通过".equals(imageResponse.getResult()) ||
                                    "人工审核".equals(imageResponse.getResult())) {
                                riskyFrameCount++;

                                // 记录风险帧详情
                                WorkAutoReviewEntity.RiskyFrame riskyFrame = new WorkAutoReviewEntity.RiskyFrame();
                                riskyFrame.setFrameIndex(frame.getFrameNumber());
                                riskyFrame.setTimestamp(frame.getTimestamp());
                                riskyFrame.setScore(imageResponse.getMatchScore());
                                riskyFrame.setReason(imageResponse.getReason());
                                riskyFrames.add(riskyFrame);

                                // 更新最高分数
                                if (imageResponse.getMatchScore() > maxScore) {
                                    maxScore = imageResponse.getMatchScore();
                                }
                            }
                        }

                        long videoProcessingTime = System.currentTimeMillis() - videoStartTime;

                        // 计算违规率
                        double violationRate = (totalFrames > 0) ? (riskyFrameCount * 100.0 / totalFrames) : 0.0;

                        // 确定视频审核结论
                        String videoResult;
                        String videoReason;

                        if (violationRate >= 30.0 || maxScore >= 70.0) {
                            videoResult = "不通过";
                            videoReason = String.format("视频包含违规内容。违规帧数: %d/%d，违规率: %.2f%%，最高风险分数: %.2f",
                                    riskyFrameCount, totalFrames, violationRate, maxScore);
                        } else if (violationRate >= 10.0 || maxScore >= 30.0) {
                            videoResult = "人工审核";
                            videoReason = String.format("视频存在疑似违规内容，建议人工复审。违规帧数: %d/%d，违规率: %.2f%%，最高风险分数: %.2f",
                                    riskyFrameCount, totalFrames, violationRate, maxScore);
                        } else {
                            videoResult = "通过";
                            videoReason = String.format("视频内容健康，无明显违规。违规帧数: %d/%d，违规率: %.2f%%",
                                    riskyFrameCount, totalFrames, violationRate);
                        }

                        log.info("视频审核完成 - 结果: {}, 总帧数: {}, 风险帧数: {}, 耗时: {}ms",
                                videoResult, totalFrames, riskyFrameCount, videoProcessingTime);

                        // 构建视频审核结果
                        videoReview = new WorkAutoReviewEntity.VideoReview();
                        videoReview.setResult(videoResult);
                        videoReview.setReason(videoReason);
                        videoReview.setCount(totalFrames);
                        videoReview.setRiskyCount(riskyFrameCount);
                        videoReview.setMaxScore(maxScore);
                        videoReview.setRiskyList(riskyFrames);
                        videoReview.setProcessingTime((int) (videoProcessingTime / 1000)); // 转换为秒
                    }
                } else {
                    log.warn("视频文件不存在: {}", videoPath);
                }
            }

            // --- 第五步：聚合审核结果 ----
            log.info("开始聚合审核结果...");

            // 构建文本审核结果
            WorkAutoReviewEntity.TextReview textReview = new WorkAutoReviewEntity.TextReview();
            textReview.setResult(textResponse.getResult());
            textReview.setReason(textResponse.getReason());
            textReview.setRiskLevel(textResponse.getRiskLevel());

            // 敏感词列表（已由 TextAutoService 检测并包含在 reason 中）
            // 如果 reason 包含敏感词信息，格式为："敏感词: word1, word2"
            List<String> sensitiveWords = new ArrayList<>();
            if (textResponse.getReason().contains("敏感词:")) {
                String[] parts = textResponse.getReason().split("敏感词:");
                if (parts.length >= 2) {
                    String[] words = parts[1].trim().split(",");
                    for (String word : words) {
                        sensitiveWords.add(word.trim());
                    }
                }
            }
            textReview.setSensitiveWords(sensitiveWords);
            textReview.setProcessingTime((int) textProcessingTime);

            // 构建审核结果对象
            WorkAutoReviewEntity.ReviewResults reviewResults = new WorkAutoReviewEntity.ReviewResults();
            reviewResults.setTextReview(textReview);
            reviewResults.setImageReview(imageReview);
            reviewResults.setVideoReview(videoReview);

            // 确定总体审核状态
            String overallStatus = determineOverallStatus(textReview, imageReview, videoReview);
            log.info("总体审核状态: {}", overallStatus);

            // 计算总耗时（秒）
            long totalProcessingTime = System.currentTimeMillis() - startTime;

            // 构建审核实体
            WorkAutoReviewEntity workAutoReview = new WorkAutoReviewEntity();
            workAutoReview.setContentId(contentId);
            workAutoReview.setReviewResults(reviewResults);
            workAutoReview.setStatus(overallStatus);
            workAutoReview.setFinalProcessingTime((int) (totalProcessingTime / 1000)); // 转换为秒
            workAutoReview.setReviewedAt(LocalDateTime.now());

            // ---- 第六步：保存审核结果到数据库 ----
            log.info("保存审核结果到数据库...");
            workAutoReviewService.save(workAutoReview);

            // ---- 第七步：更新作品状态 ----
            String contentStatus;
            if ("approved".equals(overallStatus)) {
                contentStatus = "approved";
            } else if ("rejected".equals(overallStatus)) {
                contentStatus = "rejected";
            } else if ("reviewing".equals(overallStatus)) {
                contentStatus = "reviewing"; // 需要人工审核
            } else {
                contentStatus = "pending"; // 等待AI审核
            }
            content.setStatus(contentStatus);
            contentRepository.save(content);

            log.info("作品状态已更新 - 作品ID: {}, 审核状态: {} -> {}, 内容状态: {}",
                    contentId, overallStatus, contentStatus, contentStatus);
            log.info("-------- 作品审核完成 - 作品ID: {}, 标题: {}, 总体状态: {}, 总耗时: {}ms --------",
                    contentId, content.getTitle(), overallStatus, totalProcessingTime);

            //  第八步：返回结果 
            ResultTemplate result = new ResultTemplate();
            result.putPayload("reviewId", workAutoReview.getId());
            result.putPayload("Status", overallStatus);
            result.putPayload("contentStatus", contentStatus);
            result.putPayload("reviewResults", reviewResults);
            result.putPayload("finalProcessingTime", totalProcessingTime / 1000); // 转换为秒

            return result;

        } catch (Exception e) {
            log.error("作品审核失败 - 作品ID: {}, 错误详情: {}", contentId, e.getMessage(), e);

            // 记录异常堆栈，便于调试
            StringBuilder errorDetail = new StringBuilder();
            errorDetail.append("异常类型: ").append(e.getClass().getSimpleName()).append("\n");
            errorDetail.append("异常信息: ").append(e.getMessage()).append("\n");

            if (e.getCause() != null) {
                errorDetail.append("根本原因: ").append(e.getCause().getMessage());
            }

            log.error("详细错误信息: {}", errorDetail.toString());

            return new ResultTemplate()
                    .setStatus(false)
                    .setMessage("作品审核失败: " + e.getMessage());
        }
    }

    //  当
    //  approved: 所有审核都通过
    //  rejected: 任意一项明确拒绝
    //  reviewing: 任意一项需要人工审核（且没有被拒绝的）
    private String determineOverallStatus(
            WorkAutoReviewEntity.TextReview textReview,
            WorkAutoReviewEntity.ImageReview imageReview,
            WorkAutoReviewEntity.VideoReview videoReview) {

        // 检查是否被拒绝
        if ("拒绝".equals(textReview.getResult())) {
            return "rejected";
        }
        if (imageReview != null && "不通过".equals(imageReview.getResult())) {
            return "rejected";
        }
        if (videoReview != null && "不通过".equals(videoReview.getResult())) {
            return "rejected";
        }

        // 检查是否需要人工审核
        if ("需人工审核".equals(textReview.getResult())) {
            return "reviewing";
        }
        if (imageReview != null && "人工审核".equals(imageReview.getResult())) {
            return "reviewing";
        }
        if (videoReview != null && "人工审核".equals(videoReview.getResult())) {
            return "reviewing";
        }

        // 全部通过
        return "approved";
    }

    // 查询作品审核结果GET /moderation/work/result
    // 参数：contentId（作品ID）
    @GetMapping("/work/result")
    public ResultTemplate getWorkReviewResult(@RequestParam("contentId") String contentId) {
        log.info("查询作品审核结果 - 作品ID: {}", contentId);

        try {
            Optional<WorkAutoReviewEntity> reviewOpt = workAutoReviewService.findByContentId(contentId);

            if (!reviewOpt.isPresent()) {
                return new ResultTemplate()
                        .setStatus(false)
                        .setMessage("未找到审核结果");
            }

            WorkAutoReviewEntity review = reviewOpt.get();
            ResultTemplate result = new ResultTemplate();
            result.putPayload("review", review);

            return result;

        } catch (Exception e) {
            log.error("查询审核结果失败", e);
            return new ResultTemplate()
                    .setStatus(false)
                    .setMessage("查询审核结果失败: " + e.getMessage());
        }
    }
}
