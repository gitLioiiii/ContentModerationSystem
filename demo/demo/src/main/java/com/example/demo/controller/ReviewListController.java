package com.example.demo.controller;

import com.example.demo.repository.ContentRepository;
import com.example.demo.repository.SensitiveWordRepository;
import com.example.demo.repository.WorkAutoReviewRepository;
import com.example.demo.utils.ResultTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

// 审核报表
@Slf4j
@RestController
@RequestMapping("/reviewlist")
public class ReviewListController {

    private final ContentRepository contentRepository;
    private final WorkAutoReviewRepository workAutoReviewRepository;
    private final SensitiveWordRepository sensitiveWordRepository;

    public ReviewListController(
            ContentRepository contentRepository,
            WorkAutoReviewRepository workAutoReviewRepository,
            SensitiveWordRepository sensitiveWordRepository) {
        this.contentRepository = contentRepository;
        this.workAutoReviewRepository = workAutoReviewRepository;
        this.sensitiveWordRepository = sensitiveWordRepository;
    }

    // 包括：AI共处理违规条数、今日违规未通过条数、待人工审核条数、待处理申诉量、敏感词条数
    @GetMapping("/overview")
    public ResultTemplate getOverview() {
        log.info("获取审核统计概览数据");

        try {
            Map<String, Object> overview = new HashMap<>();

            // AI共处理违规条数（auto_review_forworks表中Status为rejected的记录数）
            long totalAiViolations = workAutoReviewRepository.findAll().stream()
                    .filter(review -> "rejected".equals(review.getStatus()))
                    .count();
            overview.put("totalViolations", totalAiViolations);
            log.info("AI共处理违规条数: {}", totalAiViolations);

            // 今日违规未通过条数（今天rejected的记录数）
            LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
            LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

            long todayViolations = workAutoReviewRepository.findAll().stream()
                    .filter(review -> "rejected".equals(review.getStatus()))
                    .filter(review -> {
                        LocalDateTime reviewedAt = review.getReviewedAt();
                        return reviewedAt != null &&
                            reviewedAt.isAfter(todayStart) &&
                            reviewedAt.isBefore(todayEnd);
                    })
                    .count();
            overview.put("todayViolations", todayViolations);
            log.info("今日违规未通过条数: {}", todayViolations);

            // 待人工审核条数（content表中status为reviewing的记录数）
            long pendingReview = contentRepository.findAll().stream()
                    .filter(content -> content.getDeletedAt() == null)
                    .filter(content -> "reviewing".equals(content.getStatus()))
                    .count();
            overview.put("pendingReview", pendingReview);
            log.info("待人工审核条数: {}", pendingReview);

            // 待处理申诉量（content表中appealStatus为appealing的记录数）
            long appealCount = contentRepository.findAll().stream()
                    .filter(content -> content.getDeletedAt() == null)
                    .filter(content -> "appealing".equals(content.getAppealStatus()))
                    .count();
            overview.put("appealCount", appealCount);
            log.info("待处理申诉量: {}", appealCount);

            // 敏感词条数（sensitive_words表中effective为true的记录数）
            long violationTypesCount = sensitiveWordRepository.findAll().stream()
                    .filter(word -> word.getEffective() != null && word.getEffective())
                    .count();
            overview.put("violationTypesCount", violationTypesCount);
            log.info("敏感词条数: {}", violationTypesCount);

            ResultTemplate result = new ResultTemplate();
            result.putPayload("overview", overview);
            return result;

        } catch (Exception e) {
            log.error("获取统计概览数据失败", e);
            return new ResultTemplate()
                    .setStatus(false)
                    .setMessage("获取统计数据失败: " + e.getMessage());
        }
    }

// 柱状图展示 - 一周每日违规量
    @GetMapping("/daily-trend")
    public ResultTemplate getDailyTrend() {
        log.info("获取一周违规趋势数据");

        try {
            // 初始化最近7天的数据
            Map<LocalDate, Long> dailyCount = new LinkedHashMap<>();
            LocalDate today = LocalDate.now();

            // 从6天前到今天，共7天
            for (int i = 6; i >= 0; i--) {
                LocalDate date = today.minusDays(i);
                dailyCount.put(date, 0L);
            }

            // 统计最近7天每天的违规数据
            LocalDateTime weekStart = LocalDateTime.of(today.minusDays(6), LocalTime.MIN);
            LocalDateTime weekEnd = LocalDateTime.of(today, LocalTime.MAX);

            workAutoReviewRepository.findAll().stream()
                    .filter(review -> "rejected".equals(review.getStatus()))
                    .filter(review -> {
                        LocalDateTime reviewedAt = review.getReviewedAt();
                        return reviewedAt != null &&
                                !reviewedAt.isBefore(weekStart) &&
                                !reviewedAt.isAfter(weekEnd);
                    })
                    .forEach(review -> {
                        LocalDate date = review.getReviewedAt().toLocalDate();
                        if (dailyCount.containsKey(date)) {
                            dailyCount.put(date, dailyCount.get(date) + 1);
                        }
                    });

            // 准备图表数据
            List<String> dates = new ArrayList<>();
            List<Long> counts = new ArrayList<>();

            for (Map.Entry<LocalDate, Long> entry : dailyCount.entrySet()) {
                // 格式化日期为 "MM-DD"
                dates.add(String.format("%02d-%02d",
                    entry.getKey().getMonthValue(),
                    entry.getKey().getDayOfMonth()));
                counts.add(entry.getValue());
            }

            Map<String, Object> trendData = new HashMap<>();
            trendData.put("dates", dates);
            trendData.put("counts", counts);

            ResultTemplate result = new ResultTemplate();
            result.putPayload("dailyTrend", trendData);

            log.info("一周违规趋势数据获取成功 - 总违规数: {}",
                    counts.stream().mapToLong(Long::longValue).sum());

            return result;

        } catch (Exception e) {
            log.error("获取一周违规趋势数据失败", e);
            return new ResultTemplate()
                    .setStatus(false)
                    .setMessage("获取趋势数据失败: " + e.getMessage());
        }
    }

// 饼图展示
    @GetMapping("/sensitive-words-distribution")
    public ResultTemplate getSensitiveWordsDistribution() {
        log.info("获取敏感词分类分布数据");

        try {
            // 分类映射
            Map<String, String> categoryMap = new HashMap<>();
            categoryMap.put("porn", "色情类");
            categoryMap.put("violence", "暴力类");
            categoryMap.put("political", "政治类");
            categoryMap.put("spam_mail", "垃圾邮件");
            categoryMap.put("advertising", "广告营销");
            categoryMap.put("other", "其他");

            // 统计各分类的敏感词数量（只统计生效的）
            Map<String, Long> categoryCount = sensitiveWordRepository.findAll().stream()
                    .filter(word -> word.getEffective() != null && word.getEffective())
                    .collect(Collectors.groupingBy(
                            word -> {
                                String category = word.getCategory();
                                return categoryMap.getOrDefault(category, "未分类");
                            },
                            Collectors.counting()
                    ));

            // 转换为前端需要的格式
            List<Map<String, Object>> distribution = categoryCount.entrySet().stream()
                    .map(entry -> {
                        Map<String, Object> item = new HashMap<>();
                        item.put("name", entry.getKey());
                        item.put("value", entry.getValue());
                        return item;
                    })
                    .collect(Collectors.toList());

            long totalWords = categoryCount.values().stream().mapToLong(Long::longValue).sum();

            Map<String, Object> distributionData = new HashMap<>();
            distributionData.put("distribution", distribution);
            distributionData.put("totalWords", totalWords);

            ResultTemplate result = new ResultTemplate();
            result.putPayload("sensitiveWordsDistribution", distributionData);

            log.info("敏感词分类分布数据获取成功 - 总计: {}个", totalWords);

            return result;

        } catch (Exception e) {
            log.error("获取敏感词分类分布数据失败", e);
            return new ResultTemplate()
                    .setStatus(false)
                    .setMessage("获取分布数据失败: " + e.getMessage());
        }
    }

// 获取所有统计数据
    @GetMapping("/all")
    public ResultTemplate getAllStats() {
        log.info("获取所有审核统计数据");

        try {
            ResultTemplate overviewResult = getOverview();
            ResultTemplate trendResult = getDailyTrend();
            ResultTemplate distributionResult = getSensitiveWordsDistribution();

            if (!overviewResult.getStatus() || !trendResult.getStatus() || !distributionResult.getStatus()) {
                return new ResultTemplate()
                        .setStatus(false)
                        .setMessage("获取统计数据失败");
            }

            ResultTemplate result = new ResultTemplate();
            result.putPayload("overview", overviewResult.getPayload().get("overview"));
            result.putPayload("dailyTrend", trendResult.getPayload().get("dailyTrend"));
            result.putPayload("sensitiveWordsDistribution", distributionResult.getPayload().get("sensitiveWordsDistribution"));

            log.info("所有审核统计数据获取成功");
            return result;

        } catch (Exception e) {
            log.error("获取所有统计数据失败", e);
            return new ResultTemplate()
                    .setStatus(false)
                    .setMessage("获取统计数据失败: " + e.getMessage());
        }
    }
}
