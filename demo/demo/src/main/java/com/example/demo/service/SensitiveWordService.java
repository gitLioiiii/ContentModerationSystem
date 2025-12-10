package com.example.demo.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.example.demo.entity.SensitiveWordEntity;

public interface SensitiveWordService {
    
    List<SensitiveWordEntity> fetch(Map<String, Object> filter);

    Integer create(SensitiveWordEntity sensitiveword);

    Integer update(SensitiveWordEntity sensitiveword);

    Integer remove(SensitiveWordEntity sensitiveword);

    Integer count(Map<String, Object> filter);

    Optional<SensitiveWordEntity> fetchById(String id);

    Optional<SensitiveWordEntity> fetchByWord(String word);
}
