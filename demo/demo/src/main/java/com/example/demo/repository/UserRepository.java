package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.UserEntity;

@Repository
public interface UserRepository extends MongoRepository<UserEntity, String> {

    // 显示所有未删除的用户
    Page<UserEntity> findByDeletedAtIsNull(Pageable pageable);

    // 根据关键字搜索用户（用户名、姓名或手机号模糊匹配）
    @Query("{ 'deletedAt': null, $or: [ {'username': {$regex: ?0, $options: 'i'}}, {'name': {$regex: ?0, $options: 'i'}}, {'phone': {$regex: ?0, $options: 'i'}} ] }")
    Page<UserEntity> findByKeywords(String keywords, Pageable pageable);

    // 根据角色筛选用户
    Page<UserEntity> findByRoleAndDeletedAtIsNull(String role, Pageable pageable);

    // 根据角色筛选用户（返回列表）
    List<UserEntity> findByRoleAndDeletedAtIsNull(String role);

    // 根据关键字用户名、姓名、手机号和角色同时筛选用户
    @Query("{ 'deletedAt': null, 'role': ?0, $or: [ {'username': {$regex: ?1, $options: 'i'}}, {'name': {$regex: ?1, $options: 'i'}}, {'phone': {$regex: ?1, $options: 'i'}} ] }")
    Page<UserEntity> findByKeywordsAndRole(String role, String keywords, Pageable pageable);

    // 登录时根据用户名查找用户
    Optional<UserEntity> findByUsernameAndDeletedAtIsNull(String username);

    // 利用id更新用户
    Optional<UserEntity> findByIdAndDeletedAtIsNull(String id);
}
