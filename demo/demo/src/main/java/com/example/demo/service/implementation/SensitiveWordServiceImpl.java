package com.example.demo.service.implementation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.demo.entity.SensitiveWordEntity;
import com.example.demo.repository.SensitiveWordRepository;
import com.example.demo.service.SensitiveWordService;

@Service
public class SensitiveWordServiceImpl implements SensitiveWordService {

    private final SensitiveWordRepository sensitiveWordRepository;

    public SensitiveWordServiceImpl(SensitiveWordRepository sensitiveWordRepository) {
        this.sensitiveWordRepository = sensitiveWordRepository;
    }

    // 分页查询
    @Override
    public List<SensitiveWordEntity> fetch(Map<String, Object> filter) {
        String keywords = (String) filter.get("keywords");
        String category = (String) filter.get("category");
        String level = (String) filter.get("level");
        Integer offset = (Integer) filter.get("offset");
        Integer limit = (Integer) filter.get("limit");

        // 分页对象
        Pageable pageable = PageRequest.of(
            offset != null && limit != null ? offset / limit : 0,
            limit != null ? limit : 10
        );

        Page<SensitiveWordEntity> page;

        // 判断筛选条件组合
        boolean hasKeywords = keywords != null && !keywords.isEmpty();
        boolean hasCategory = category != null && !category.isEmpty();
        boolean hasLevel = level != null && !level.isEmpty();

        if (hasKeywords && hasCategory && hasLevel) {
            // 关键字、分类和等级
            page = this.sensitiveWordRepository.findByKeywordsAndCategoryAndLevel(category, level, keywords, pageable);
        } else if (hasKeywords && hasCategory) {
            // 关键字和分类
            page = this.sensitiveWordRepository.findByKeywordsAndCategory(category, keywords, pageable);
        } else if (hasKeywords && hasLevel) {
            // 关键字和等级
            page = this.sensitiveWordRepository.findByKeywordsAndLevel(level, keywords, pageable);
        } else if (hasCategory && hasLevel) {
            // 分类和等级
            page = this.sensitiveWordRepository.findByCategoryAndLevel(category, level, pageable);
        } else if (hasKeywords) {
            // 关键字
            page = this.sensitiveWordRepository.findByKeywords(keywords, pageable);
        } else if (hasCategory) {
            // 分类
            page = this.sensitiveWordRepository.findByCategory(category, pageable);
        } else if (hasLevel) {
            // 等级
            page = this.sensitiveWordRepository.findByLevel(level, pageable);
        } else {
            // 查询所有
            page = this.sensitiveWordRepository.findAll(pageable);
        }

        return page.getContent();
    }

    // 统计总数
    @Override
    public Integer count(Map<String, Object> filter) {
        String keywords = (String) filter.get("keywords");
        String category = (String) filter.get("category");
        String level = (String) filter.get("level");

        // 获取总数，使用较小的页面大小
        Pageable pageable = PageRequest.of(0, 1);

        // 判断筛选条件组合
        boolean hasKeywords = keywords != null && !keywords.isEmpty();
        boolean hasCategory = category != null && !category.isEmpty();
        boolean hasLevel = level != null && !level.isEmpty();

        Page<SensitiveWordEntity> page;

        if (hasKeywords && hasCategory && hasLevel) {
            // 关键字、分类和等级
            page = this.sensitiveWordRepository.findByKeywordsAndCategoryAndLevel(category, level, keywords, pageable);
        } else if (hasKeywords && hasCategory) {
            // 关键字和分类
            page = this.sensitiveWordRepository.findByKeywordsAndCategory(category, keywords, pageable);
        } else if (hasKeywords && hasLevel) {
            // 关键字和等级
            page = this.sensitiveWordRepository.findByKeywordsAndLevel(level, keywords, pageable);
        } else if (hasCategory && hasLevel) {
            // 分类和等级
            page = this.sensitiveWordRepository.findByCategoryAndLevel(category, level, pageable);
        } else if (hasKeywords) {
            // 关键字
            page = this.sensitiveWordRepository.findByKeywords(keywords, pageable);
        } else if (hasCategory) {
            // 分类
            page = this.sensitiveWordRepository.findByCategory(category, pageable);
        } else if (hasLevel) {
            // 等级
            page = this.sensitiveWordRepository.findByLevel(level, pageable);
        } else {
            // 查询所有
            page = this.sensitiveWordRepository.findAll(pageable);
        }

        return (int) page.getTotalElements();
    }

    // 创建敏感词
    @Override
    public Integer create(SensitiveWordEntity sensitiveWord) {
        sensitiveWord.setCreatedAt(LocalDateTime.now());
        SensitiveWordEntity saved = this.sensitiveWordRepository.save(sensitiveWord);
        return saved.getId() != null ? 1 : 0;
    }

    // 更新敏感词
    @Override
    public Integer update(SensitiveWordEntity sensitiveWord) {
        SensitiveWordEntity saved = this.sensitiveWordRepository.save(sensitiveWord);
        return saved.getId() != null ? 1 : 0;
    }

    // 删除敏感词
    @Override
    public Integer remove(SensitiveWordEntity sensitiveWord) {
        try {
            this.sensitiveWordRepository.delete(sensitiveWord);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    // 根据ID查询敏感词
    @Override
    public Optional<SensitiveWordEntity> fetchById(String id) {
        return this.sensitiveWordRepository.findById(id);
    }

    // 根据word精确查询敏感词（用于检查重复）
    @Override
    public Optional<SensitiveWordEntity> fetchByWord(String word) {
        return this.sensitiveWordRepository.findByWord(word);
    }
}
