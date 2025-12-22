package com.example.demo.service.implementation;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.demo.entity.WorkAutoReviewEntity;
import com.example.demo.repository.WorkAutoReviewRepository;
import com.example.demo.service.WorkAutoReviewService;

@Service
public class WorkAutoReviewServiceImpl implements WorkAutoReviewService {

    private final WorkAutoReviewRepository workAutoReviewRepository;

    public WorkAutoReviewServiceImpl(WorkAutoReviewRepository workAutoReviewRepository) {
        this.workAutoReviewRepository = workAutoReviewRepository;
    }

    @Override
    public Integer save(WorkAutoReviewEntity workAutoReview) {
        WorkAutoReviewEntity saved = this.workAutoReviewRepository.save(workAutoReview);
        return saved.getId() != null ? 1 : 0;
    }

    @Override
    public Optional<WorkAutoReviewEntity> findByContentId(String contentId) {
        return this.workAutoReviewRepository.findByContentId(contentId);
    }
}
