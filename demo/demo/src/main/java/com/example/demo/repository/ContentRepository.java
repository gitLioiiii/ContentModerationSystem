package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.ContentEntity;

@Repository
public interface ContentRepository extends MongoRepository<ContentEntity, String> {

    // 根据用户ID和删除状态查询作品（分页）
    Page<ContentEntity> findByUserIdAndDeletedAtIsNull(String userId, Pageable pageable);

    // 根据用户ID、状态和删除状态查询作品（分页）
    Page<ContentEntity> findByUserIdAndStatusAndDeletedAtIsNull(String userId, String status, Pageable pageable);

    // 根据用户ID、关键字（标题模糊匹配）查询作品（分页）
    @Query("{ 'userId': ?0, 'deletedAt': null, 'title': {$regex: ?1, $options: 'i'} }")
    Page<ContentEntity> findByUserIdAndKeywords(String userId, String keywords, Pageable pageable);

    // 根据用户ID、状态、关键字查询作品（分页）
    @Query("{ 'userId': ?0, 'status': ?1, 'deletedAt': null, 'title': {$regex: ?2, $options: 'i'} }")
    Page<ContentEntity> findByUserIdAndStatusAndKeywords(String userId, String status, String keywords, Pageable pageable);

    // 根据ID和删除状态查询单个作品
    Optional<ContentEntity> findByIdAndDeletedAtIsNull(String id);

    // 查询所有审核通过且未删除的作品(发现页面）
    Page<ContentEntity> findByStatusAndDeletedAtIsNull(String status, Pageable pageable);

    // 根据关键字查询所有审核通过且未删除的作品（发现页面）
    @Query("{ 'status': ?0, 'deletedAt': null, 'title': {$regex: ?1, $options: 'i'} }")
    Page<ContentEntity> findByStatusAndKeywords(String status, String keywords, Pageable pageable);

    // 查询多个状态的作品（用于审核队列，包含 reviewing 和 appealing）
    @Query("{ 'status': {$in: ?0}, 'deletedAt': null }")
    Page<ContentEntity> findByStatusInAndDeletedAtIsNull(java.util.List<String> statuses, Pageable pageable);
}
