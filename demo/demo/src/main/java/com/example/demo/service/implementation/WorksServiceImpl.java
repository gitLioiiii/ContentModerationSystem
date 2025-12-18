package com.example.demo.service.implementation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.demo.entity.ContentEntity;
import com.example.demo.repository.ContentRepository;
import com.example.demo.service.WorksService;

@Service
public class WorksServiceImpl implements WorksService {

    private final ContentRepository contentRepository;

    public WorksServiceImpl(ContentRepository contentRepository) {
        this.contentRepository = contentRepository;
    }

    @Override
    public List<ContentEntity> fetch(Map<String, Object> filter) {
        String userId = (String) filter.get("userId");
        String keywords = (String) filter.get("keywords");
        String status = (String) filter.get("status");
        Integer offset = (Integer) filter.get("offset");
        Integer limit = (Integer) filter.get("limit");

        // 按创建时间倒序分页
        Pageable pageable = PageRequest.of(
            offset != null && limit != null ? offset / limit : 0,
            limit != null ? limit : 10,
            Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<ContentEntity> page;

        // 关键字
        boolean hasKeywords = keywords != null && !keywords.isEmpty();
        // 审核状态
        boolean hasStatus = status != null && !status.isEmpty();

        if (hasKeywords && hasStatus) {
            // 关键字和状审核态
            page = this.contentRepository.findByUserIdAndStatusAndKeywords(userId, status, keywords, pageable);
        } else if (hasKeywords) {
            // 关键字
            page = this.contentRepository.findByUserIdAndKeywords(userId, keywords, pageable);
        } else if (hasStatus) {
            // 审核状态
            page = this.contentRepository.findByUserIdAndStatusAndDeletedAtIsNull(userId, status, pageable);
        } else {
            // 所有作品
            page = this.contentRepository.findByUserIdAndDeletedAtIsNull(userId, pageable);
        }

        return page.getContent();
    }

    @Override
    public Integer count(Map<String, Object> filter) {
        String userId = (String) filter.get("userId");
        String keywords = (String) filter.get("keywords");
        String status = (String) filter.get("status");

        // 获取总数
        Pageable pageable = PageRequest.of(0, 1);

        // 关键字
        boolean hasKeywords = keywords != null && !keywords.isEmpty();
        // 审核状态
        boolean hasStatus = status != null && !status.isEmpty();

        Page<ContentEntity> count;

        if (hasKeywords && hasStatus) {
            // 关键字和审核状态
            count = this.contentRepository.findByUserIdAndStatusAndKeywords(userId, status, keywords, pageable);
        } else if (hasKeywords) {
            // 关键字
            count = this.contentRepository.findByUserIdAndKeywords(userId, keywords, pageable);
        } else if (hasStatus) {
            // 审核状态
            count = this.contentRepository.findByUserIdAndStatusAndDeletedAtIsNull(userId, status, pageable);
        } else {
            // 所有作品
            count = this.contentRepository.findByUserIdAndDeletedAtIsNull(userId, pageable);
        }

        return (int) count.getTotalElements();
    }

    @Override
    public Integer create(ContentEntity content) {
        ContentEntity savedContent = this.contentRepository.save(content);
        return savedContent.getId() != null ? 1 : 0;
    }

    @Override
    public Optional<ContentEntity> fetchById(String id) {
        return this.contentRepository.findByIdAndDeletedAtIsNull(id);
    }

    @Override
    public Integer remove(ContentEntity content) {
        content.setDeletedAt(LocalDateTime.now());
        ContentEntity savedContent = this.contentRepository.save(content);
        return savedContent.getId() != null ? 1 : 0;
    }
}
