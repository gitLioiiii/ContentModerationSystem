package com.example.demo.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.example.demo.entity.ContentEntity;

public interface WorksService {

    // 获取作品列表
    List<ContentEntity> fetch(Map<String, Object> filter);

    // 统计作品数量
    Integer count(Map<String, Object> filter);

    // 创建作品
    Integer create(ContentEntity content);

    // 根据ID获取作品
    Optional<ContentEntity> fetchById(String id);

    // 删除作品
    Integer remove(ContentEntity content);
}
