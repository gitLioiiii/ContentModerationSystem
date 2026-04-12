package com.example.demo.service;

import java.util.Optional;

import com.example.demo.entity.WorkAutoReviewEntity;

public interface WorkAutoReviewService {

    // 保存审核
    Integer save(WorkAutoReviewEntity workAutoReview);

    // 通过id查询审核结果
    Optional<WorkAutoReviewEntity> findByContentId(String contentId);
}
