package com.example.demo.service;

import com.example.demo.dto.ImageAutoRequest;
import com.example.demo.dto.ImageAutoResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * 异步图片审核服务
 * 使用 Spring Boot 线程池并行处理图片审核任务
 */
@Slf4j
@Service
public class AsyncImageModerationService {

    private final ImageAutoService imageAutoService;

    public AsyncImageModerationService(ImageAutoService imageAutoService) {
        this.imageAutoService = imageAutoService;
    }

    /**
     * 异步审核单张图片
     * 使用 @Async 注解，该方法会在线程池中异步执行
     *
     * @param imageRequest 图片审核请求
     * @return CompletableFuture 包装的审核结果
     */
    @Async
    public CompletableFuture<ImageAutoResponse> moderateImageAsync(ImageAutoRequest imageRequest) {
        try {
            log.debug("线程 {} 开始审核图片: {}",
                Thread.currentThread().getName(),
                imageRequest.getImageUrl());

            // 调用原有的图片审核服务
            ImageAutoResponse response = imageAutoService.AiImage(imageRequest);

            log.debug("线程 {} 完成审核图片: {} - 结果: {}",
                Thread.currentThread().getName(),
                imageRequest.getImageUrl(),
                response.getResult());

            return CompletableFuture.completedFuture(response);

        } catch (Exception e) {
            log.error("异步审核图片失败: {}", imageRequest.getImageUrl(), e);
            // 返回失败的 Future
            CompletableFuture<ImageAutoResponse> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(e);
            return failedFuture;
        }
    }
}
