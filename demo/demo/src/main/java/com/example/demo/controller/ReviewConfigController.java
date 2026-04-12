package com.example.demo.controller;

import java.time.LocalDateTime;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.ReviewConfigEntity;
import com.example.demo.repository.ReviewConfigRepository;
import com.example.demo.utils.ResultTemplate;

import lombok.extern.slf4j.Slf4j;

// 审核阈值配置控制器
@Slf4j
@RestController
@RequestMapping("/review-config")
public class ReviewConfigController {

    private final ReviewConfigRepository reviewConfigRepository;

    public ReviewConfigController(ReviewConfigRepository reviewConfigRepository) {
        this.reviewConfigRepository = reviewConfigRepository;
    }

    // 获取当前审核配置
    @GetMapping
    public ResultTemplate getConfig() {
        ResultTemplate result = new ResultTemplate();
        ReviewConfigEntity config = getOrCreateConfig();
        result.putPayload("config", config);
        return result;
    }

    // 更新审核配置
    @PostMapping("/update")
    public ResultTemplate updateConfig(@RequestBody ReviewConfigEntity newConfig) {
        log.info("更新审核阈值配置");
        ResultTemplate result = new ResultTemplate();

        ReviewConfigEntity config = getOrCreateConfig();

        config.setImagePassThreshold(newConfig.getImagePassThreshold());
        config.setImageRejectThreshold(newConfig.getImageRejectThreshold());
        config.setVideoRejectViolationRate(newConfig.getVideoRejectViolationRate());
        config.setVideoRejectMaxScore(newConfig.getVideoRejectMaxScore());
        config.setVideoManualViolationRate(newConfig.getVideoManualViolationRate());
        config.setVideoManualMaxScore(newConfig.getVideoManualMaxScore());
        config.setVideoFrameInterval(newConfig.getVideoFrameInterval());
        config.setUpdatedAt(LocalDateTime.now());

        reviewConfigRepository.save(config);

        log.info("审核阈值配置已更新");
        result.putPayload("config", config);
        return result;
    }

    // 获取配置，不存在则创建默认配置
    private ReviewConfigEntity getOrCreateConfig() {
        return reviewConfigRepository.findAll().stream().findFirst().orElseGet(() -> {
            ReviewConfigEntity defaultConfig = new ReviewConfigEntity();
            defaultConfig.setUpdatedAt(LocalDateTime.now());
            return reviewConfigRepository.save(defaultConfig);
        });
    }

}
