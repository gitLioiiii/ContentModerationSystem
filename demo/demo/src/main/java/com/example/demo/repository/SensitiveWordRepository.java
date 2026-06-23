package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
// import org.springframework.stereotype.Repository;

import com.example.demo.entity.SensitiveWordEntity;

// @Repository
public interface SensitiveWordRepository extends MongoRepository<SensitiveWordEntity, String> {

    // 分页查询所有敏感词
    Page<SensitiveWordEntity> findAll(Pageable pageable);

    // 根据关键字搜索敏感词（在word或category字段中搜索）
    @Query("{ $or: [ {'word': {$regex: ?0, $options: 'i'}}, {'category': {$regex: ?0, $options: 'i'}} ] }")
    Page<SensitiveWordEntity> findByKeywords(String keywords, Pageable pageable);

    // 根据分类筛选敏感词
    Page<SensitiveWordEntity> findByCategory(String category, Pageable pageable);

    // 根据等级筛选敏感词
    Page<SensitiveWordEntity> findByLevel(String level, Pageable pageable);

    // 根据关键字和分类同时筛选敏感词
    @Query("{ 'category': ?0, $or: [ {'word': {$regex: ?1, $options: 'i'}} ] }")
    Page<SensitiveWordEntity> findByKeywordsAndCategory(String category, String keywords, Pageable pageable);

    // 根据关键字和等级同时筛选敏感词
    @Query("{ 'level': ?0, $or: [ {'word': {$regex: ?1, $options: 'i'}}, {'category': {$regex: ?1, $options: 'i'}} ] }")
    Page<SensitiveWordEntity> findByKeywordsAndLevel(String level, String keywords, Pageable pageable);

    // 根据分类和等级同时筛选敏感词
    Page<SensitiveWordEntity> findByCategoryAndLevel(String category, String level, Pageable pageable);

    // 根据关键字、分类和等级同时筛选敏感词
    @Query("{ 'category': ?0, 'level': ?1, $or: [ {'word': {$regex: ?2, $options: 'i'}} ] }")
    Page<SensitiveWordEntity> findByKeywordsAndCategoryAndLevel(String category, String level, String keywords, Pageable pageable);

    // 根据id查找
    Optional<SensitiveWordEntity> findById(String id);

    // 根据word精确查找（用于检查重复）
    Optional<SensitiveWordEntity> findByWord(String word);
}
