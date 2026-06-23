package com.example.demo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
// import org.springframework.stereotype.Repository;

import com.example.demo.entity.ReviewConfigEntity;

// @Repository
public interface ReviewConfigRepository extends MongoRepository<ReviewConfigEntity, String> {

}
