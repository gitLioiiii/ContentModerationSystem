package com.example.demo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.SensitiveWordEntity;

/**
 * 敏感词仓库接口
 */
@Repository
public interface SensitiveWordRepository extends MongoRepository<SensitiveWordEntity, String> {

    //todo
}
