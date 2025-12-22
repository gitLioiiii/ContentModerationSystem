package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.ManualReviewEntity;

@Repository
public interface ManualReviewRepository extends MongoRepository<ManualReviewEntity, String> {

    // 根据作品ID查询审核记录（分页）
    Page<ManualReviewEntity> findByContentId(String contentId, Pageable pageable);

    // 根据审核员ID查询审核记录（分页）
    Page<ManualReviewEntity> findByReviewerId(String reviewerId, Pageable pageable);

    // 根据审核决定查询审核记录（分页）
    Page<ManualReviewEntity> findByDecision(String decision, Pageable pageable);

    // 根据作品ID查询最新的审核记录
    Optional<ManualReviewEntity> findFirstByContentIdOrderByReviewedAtDesc(String contentId);
}
