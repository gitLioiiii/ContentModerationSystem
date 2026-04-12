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

    // 更新作品
    Integer update(ContentEntity content);

    // 获取所有用户的审核通过作品列表（发现页面）
    List<ContentEntity> fetchDiscoverWorks(Map<String, Object> filter);

    // 统计所有用户的审核通过作品数量（发现页面）
    Integer countDiscoverWorks(Map<String, Object> filter);
}
