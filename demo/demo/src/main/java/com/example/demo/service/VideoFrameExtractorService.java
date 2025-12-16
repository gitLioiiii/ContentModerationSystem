package com.example.demo.service;

import com.example.demo.dto.FrameExtractionResult;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// 使用JavaCV（FFmpeg）从视频中按时间间隔提取关键帧
@Slf4j
@Service
public class VideoFrameExtractorService {

    @Value("${application.upload-root}")
    private String uploadRoot;

    // 从视频文件中提取关键帧
    public List<FrameExtractionResult> extractFrames(String videoPath, int frameIntervalSeconds) throws Exception {
        log.info("----- 开始视频抽帧 -----");
        log.info("视频路径: {}", videoPath);
        log.info("抽帧间隔: {}秒", frameIntervalSeconds);

        // 验证视频文件是否存在
        File videoFile = new File(videoPath);
        if (!videoFile.exists()) {
            log.error("视频文件不存在: {}", videoPath);
            throw new Exception("视频文件不存在: " + videoPath);
        }
        log.info("视频文件大小: {} MB", videoFile.length() / (1024.0 * 1024.0));

        List<FrameExtractionResult> results = new ArrayList<>();

        // 创建存储目录
        File frameDir = new File(uploadRoot + File.separator + "ai-videos");
        if (!frameDir.exists()) {
            boolean created = frameDir.mkdirs();
            log.info("创建帧存储目录: {}, 结果: {}", frameDir.getAbsolutePath(), created);
        }

        log.info("初始化FFmpegFrameGrabber...");
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(videoPath);
            Java2DFrameConverter converter = new Java2DFrameConverter()) {

            // 设置超时时间为5秒（避免卡死）
            grabber.setOption("timeout", "5000000");

            log.info("启动FFmpegFrameGrabber...");
            grabber.start();
            log.info("FFmpegFrameGrabber启动成功");

            // 获取视频信息
            double frameRate = grabber.getFrameRate();
            int totalFrames = grabber.getLengthInFrames();
            double videoDuration = grabber.getLengthInTime() / 1000000.0;

            log.info("视频信息 - 帧率: {} FPS, 总帧数: {}, 时长: {} 秒", frameRate, totalFrames, String.format("%.2f", videoDuration));

            // 验证帧率是否有效
            if (frameRate <= 0 || frameRate > 120) {
                log.warn("帧率异常: {}, 使用默认值 25 FPS", frameRate);
                frameRate = 25.0;
            }

            // 计算抽帧间隔（帧数）
            int frameInterval = (int) (frameRate * frameIntervalSeconds);
            log.info("抽帧间隔: 每 {} 帧抽取一次", frameInterval);

            int frameNumber = 0;
            int extractedCount = 0;
            int maxFrames = 50; //避免处理时间过长，最多提取50帧

            log.info("开始抽取视频帧...");

            // 遍历视频帧
            Frame frame;
            while ((frame = grabber.grabImage()) != null && extractedCount < maxFrames) {
                // 按间隔提取帧
                if (frameNumber % frameInterval == 0) {
                    try {
                        // 转换为 BufferedImage
                        BufferedImage bufferedImage = converter.convert(frame);

                        if (bufferedImage != null) {
                            // 生成唯一文件名
                            String filename = UUID.randomUUID().toString() + ".jpg";
                            File outputFile = new File(frameDir, filename);

                            // 保存为JPEG图片
                            ImageIO.write(bufferedImage, "jpg", outputFile);

                            // 计算时间戳
                            double timestamp = frameNumber / frameRate;

                            // 添加结果
                            FrameExtractionResult result = new FrameExtractionResult();
                            result.setFrameNumber(extractedCount);
                            result.setTimestamp(timestamp);
                            result.setFilename("/ai-videos/" + filename);
                            result.setFilePath(outputFile.getAbsolutePath());

                            results.add(result);
                            extractedCount++;

                            log.debug("提取第 {} 帧 (视频帧号:{}, 时间: {}秒) -> {}",
                                extractedCount, frameNumber, String.format("%.2f", timestamp), filename);
                        }
                    } catch (Exception e) {
                        log.error("提取第 {} 帧时出错: {}", frameNumber, e.getMessage());
                        // 继续处理下一帧
                    }
                }

                frameNumber++;

                // 每处理100帧打印一次进度
                if (frameNumber % 100 == 0) {
                    log.info("处理进度: 已处理 {} 帧, 已提取 {} 帧", frameNumber, extractedCount);
                }
            }

            log.info("=== 视频抽帧完成 ===");
            log.info("总共处理 {} 帧, 提取 {} 帧", frameNumber, extractedCount);

        } catch (Exception e) {
            log.error("视频抽帧失败: {}", e.getMessage(), e);
            throw new Exception("视频抽帧失败: " + e.getMessage(), e);
        }

        return results;
    }
}
