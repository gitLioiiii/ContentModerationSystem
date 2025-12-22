package com.example.demo.service;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.demo.entity.ManualReviewEntity;

public interface ManualReviewService {

    // 保存人工审核记录
    ManualReviewEntity save(ManualReviewEntity manualReview);

    // 根据作品ID查询审核记录（分页）
    Page<ManualReviewEntity> findByContentId(String contentId, Pageable pageable);

    // 根据审核员ID查询审核记录（分页）
    Page<ManualReviewEntity> findByReviewerId(String reviewerId, Pageable pageable);

    // 根据审核决定查询审核记录（分页）
    Page<ManualReviewEntity> findByDecision(String decision, Pageable pageable);

    // 根据作品ID查询最新的审核记录
    Optional<ManualReviewEntity> findLatestByContentId(String contentId);

    // 根据多个条件筛选审核记录（分页）
    Page<ManualReviewEntity> findByFilters(
            String decision,
            String reviewType,
            String reviewerName,
            LocalDate reviewDate,
            Pageable pageable);
}
