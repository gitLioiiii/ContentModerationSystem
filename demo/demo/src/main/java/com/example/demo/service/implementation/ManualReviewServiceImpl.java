package com.example.demo.service.implementation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.example.demo.entity.ManualReviewEntity;
import com.example.demo.repository.ManualReviewRepository;
import com.example.demo.service.ManualReviewService;

@Service
public class ManualReviewServiceImpl implements ManualReviewService {

    private final ManualReviewRepository manualReviewRepository;
    private final MongoTemplate mongoTemplate;

    public ManualReviewServiceImpl(ManualReviewRepository manualReviewRepository, MongoTemplate mongoTemplate) {
        this.manualReviewRepository = manualReviewRepository;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public ManualReviewEntity save(ManualReviewEntity manualReview) {
        return manualReviewRepository.save(manualReview);
    }

    @Override
    public Page<ManualReviewEntity> findByContentId(String contentId, Pageable pageable) {
        return manualReviewRepository.findByContentId(contentId, pageable);
    }

    @Override
    public Page<ManualReviewEntity> findByReviewerId(String reviewerId, Pageable pageable) {
        return manualReviewRepository.findByReviewerId(reviewerId, pageable);
    }

    @Override
    public Page<ManualReviewEntity> findByDecision(String decision, Pageable pageable) {
        return manualReviewRepository.findByDecision(decision, pageable);
    }

    @Override
    public Optional<ManualReviewEntity> findLatestByContentId(String contentId) {
        return manualReviewRepository.findFirstByContentIdOrderByReviewedAtDesc(contentId);
    }

    @Override
    public Page<ManualReviewEntity> findByFilters(
            String decision,
            String reviewType,
            String reviewerName,
            LocalDate reviewDate,
            Pageable pageable) {

        // 创建查询对象
        Query query = new Query();
        List<Criteria> criteriaList = new ArrayList<>();

        // 添加筛选条件
        if (decision != null && !decision.isEmpty()) {
            criteriaList.add(Criteria.where("decision").is(decision));
        }

        if (reviewType != null && !reviewType.isEmpty()) {
            criteriaList.add(Criteria.where("reviewType").is(reviewType));
        }

        if (reviewerName != null && !reviewerName.isEmpty()) {
            criteriaList.add(Criteria.where("reviewerName").is(reviewerName));
        }

        if (reviewDate != null) {
            // 将日期转换为当天的起始和结束时间
            LocalDateTime startOfDay = reviewDate.atStartOfDay();
            LocalDateTime endOfDay = reviewDate.atTime(LocalTime.MAX);
            criteriaList.add(Criteria.where("reviewedAt").gte(startOfDay).lte(endOfDay));
        }

        // 合并所有条件
        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }

        // 添加分页和排序
        query.with(pageable);

        // 执行查询
        List<ManualReviewEntity> records = mongoTemplate.find(query, ManualReviewEntity.class);

        // 获取总数
        long total = mongoTemplate.count(query.skip(0).limit(0), ManualReviewEntity.class);

        // 返回分页结果
        return new PageImpl<>(records, pageable, total);
    }
}
