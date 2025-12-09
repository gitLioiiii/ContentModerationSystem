package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.UserEntity;

// tag 仓库接口
@Repository
public interface UserRepository extends MongoRepository<UserEntity, String> {

    // 显示所有未删除的用户
    @Query("{ 'deletedAt': null }")
    Page<UserEntity> fetchByNotDeleted(Pageable pageable);
    @Query(value = "{ 'deletedAt': null }", count = true)
    long countByNotDeleted();

    // 根据关键字搜索用户（用户名、姓名或手机号模糊匹配）
    @Query("{ 'deletedAt': null, $or: [ {'username': {$regex: ?0, $options: 'i'}}, {'name': {$regex: ?0, $options: 'i'}}, {'phone': {$regex: ?0, $options: 'i'}} ] }")
    Page<UserEntity> searchByKeywords(String keywords, Pageable pageable);
    // 根据关键字统计用户数
    // @Query(value = "{ 'deletedAt': null, $or: [ {'username': {$regex: ?0, $options: 'i'}}, {'name': {$regex: ?0, $options: 'i'}}, {'phone': {$regex: ?0, $options: 'i'}} ] }", count = true)
    // long countByKeywords(String keywords);

    // 根据角色筛选用户
    @Query("{ 'deletedAt': null, 'role': ?0 }")
    Page<UserEntity> searchByRole(String role, Pageable pageable);
    // 根据角色统计用户数
    // @Query(value = "{ 'deletedAt': null, 'role': ?0 }", count = true)
    // long countByRole(String role);

    // 根据关键字用户名、姓名、手机号和角色同时筛选用户
    @Query("{ 'deletedAt': null, 'role': ?0, $or: [ {'username': {$regex: ?1, $options: 'i'}}, {'name': {$regex: ?1, $options: 'i'}}, {'phone': {$regex: ?1, $options: 'i'}} ] }")
    Page<UserEntity> searchByKeywordsAndRole(String role, String keywords, Pageable pageable);
    // 根据关键字用户名、姓名、手机号和角色统计用户数
    // @Query(value = "{ 'deletedAt': null, 'role': ?0, $or: [ {'username': {$regex: ?1, $options: 'i'}}, {'name': {$regex: ?1, $options: 'i'}}, {'phone': {$regex: ?1, $options: 'i'}} ] }", count = true)
    // long countByKeywordsAndRole(String role, String keywords);

    // 登录时根据用户名查找用户
    @Query("{ 'username': ?0, 'deletedAt': null }")
    Optional<UserEntity> fetchByUsername(String username);

    // 状态异常的用户
    @Query("{ 'status': ?0, 'deletedAt': null }")
    List<UserEntity> fetchByStatus(String status);
    
    // 利用id更新用户
    @Query("{ '_id': ?0, 'deletedAt': null }")
    Optional<UserEntity> fetchById(String id);
}
