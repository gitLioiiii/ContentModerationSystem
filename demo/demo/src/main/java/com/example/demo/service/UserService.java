package com.example.demo.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.example.demo.entity.UserEntity;

public interface UserService {

    List<UserEntity> fetch(Map<String, Object> filter);

    Integer create(UserEntity user);

    Integer update(UserEntity user);

    Integer remove(UserEntity user);

    Integer count(Map<String, Object> filter);

    Optional<UserEntity> fetchById(String id);

    Optional<UserEntity> fetchByUsername(String username);

}
