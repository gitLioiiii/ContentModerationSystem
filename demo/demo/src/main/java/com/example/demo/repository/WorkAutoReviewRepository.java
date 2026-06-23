package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
// import org.springframework.stereotype.Repository;

import com.example.demo.entity.WorkAutoReviewEntity;

// 对作品AI自动审核结果
// @Repository
public interface WorkAutoReviewRepository extends MongoRepository<WorkAutoReviewEntity, String> {

    // 查询ai审核结果根据作品ID
    Optional<WorkAutoReviewEntity> findByContentId(String contentId);
}
