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

    // 查找所有未删除的用户（支持分页）
    @Query("{ 'deletedAt': null }")
    Page<UserEntity> fetchByNotDeleted(Pageable pageable);
    // 统计未删除的用户数
    @Query(value = "{ 'deletedAt': null }", count = true)
    long countByNotDeleted();

    // 根据关键字搜索用户（用户名或姓名模糊匹配）
    @Query("{ 'deletedAt': null, $or: [ {'username': {$regex: ?0, $options: 'i'}}, {'name': {$regex: ?0, $options: 'i'}} ] }")
    Page<UserEntity> searchByKeywords(String keywords, Pageable pageable);
    // 根据关键字统计用户数
    @Query(value = "{ 'deletedAt': null, $or: [ {'username': {$regex: ?0, $options: 'i'}}, {'name': {$regex: ?0, $options: 'i'}} ] }", count = true)
    long countByKeywords(String keywords);

    
    // Optional可以避免空值
    // 根据用户名查找用户（deletedAt为null的用户）
    @Query("{ 'username': ?0, 'deletedAt': null }")
    Optional<UserEntity> fetchByUsername(String username);

    // 根据ID查找用户（deletedAt为null的用户）
    @Query("{ '_id': ?0, 'deletedAt': null }")
    Optional<UserEntity> fetchById(String id);

    // 根据邮箱查找用户
    @Query("{ 'email': ?0, 'deletedAt': null }")
    Optional<UserEntity> fetchByEmail(String email);

    // 根据手机号查找用户
    @Query("{ 'phone': ?0, 'deletedAt': null }")
    Optional<UserEntity> fetchByPhone(String phone);

    // 根据角色查找用户
    @Query("{ 'role': ?0, 'deletedAt': null }")
    List<UserEntity> fetchByRole(String role);

    // 根据状态查找用户
    @Query("{ 'status': ?0, 'deletedAt': null }")
    List<UserEntity> fetchByStatus(String status);
}
