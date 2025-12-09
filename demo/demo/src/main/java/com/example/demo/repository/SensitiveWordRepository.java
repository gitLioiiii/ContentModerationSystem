package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.SensitiveWordEntity;

/**
 * 敏感词仓库接口
 */
@Repository
public interface SensitiveWordRepository extends MongoRepository<SensitiveWordEntity, String> {

    // 查找所有启用的敏感词
    @Query("{ 'enabled': true }")
    List<SensitiveWordEntity> findAllEnabled();

    // 根据分类查找敏感词（支持分页）
    @Query("{ 'category': ?0, 'enabled': true }")
    Page<SensitiveWordEntity> findByCategory(String category, Pageable pageable);

    // 根据严重程度查找敏感词
    @Query("{ 'level': ?0, 'enabled': true }")
    List<SensitiveWordEntity> findByLevel(String level);

    // 根据关键字搜索敏感词（支持分页）
    @Query("{ 'enabled': true, $or: [ {'word': {$regex: ?0, $options: 'i'}}, {'remark': {$regex: ?0, $options: 'i'}} ] }")
    Page<SensitiveWordEntity> searchByKeywords(String keywords, Pageable pageable);

    // 统计搜索关键字的敏感词数量
    @Query(value = "{ 'enabled': true, $or: [ {'word': {$regex: ?0, $options: 'i'}}, {'remark': {$regex: ?0, $options: 'i'}} ] }", count = true)
    long countByKeywords(String keywords);

    // 根据敏感词查找（精确匹配）
    @Query("{ 'word': ?0 }")
    Optional<SensitiveWordEntity> findByWord(String word);

    // 查询所有敏感词（支持分页，包含禁用的）
    Page<SensitiveWordEntity> findAll(Pageable pageable);

    // 统计启用的敏感词数量
    @Query(value = "{ 'enabled': true }", count = true)
    long countEnabled();

    // 根据分类和严重程度查找
    @Query("{ 'category': ?0, 'level': ?1, 'enabled': true }")
    List<SensitiveWordEntity> findByCategoryAndLevel(String category, String level);

    // 多条件组合查询（支持分页）
    @Query("{ $and: [ " +
           "{ $or: [ " +
           "  { $expr: { $eq: [ ?0, null ] } }, " +
           "  { $expr: { $eq: [ ?0, '' ] } }, " +
           "  { 'word': { $regex: ?0, $options: 'i' } } " +
           "] }, " +
           "{ $or: [ " +
           "  { $expr: { $eq: [ ?1, null ] } }, " +
           "  { $expr: { $eq: [ ?1, '' ] } }, " +
           "  { 'category': ?1 } " +
           "] }, " +
           "{ $or: [ " +
           "  { $expr: { $eq: [ ?2, null ] } }, " +
           "  { $expr: { $eq: [ ?2, '' ] } }, " +
           "  { 'level': ?2 } " +
           "] } " +
           "] }")
    Page<SensitiveWordEntity> findByFilters(String keywords, String category, String level, Pageable pageable);
}
