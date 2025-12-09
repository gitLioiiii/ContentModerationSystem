package com.example.demo.service.implementation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.demo.entity.UserEntity;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.UserService;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<UserEntity> fetch(Map<String, Object> filter) {
        String keywords = (String) filter.get("keywords");
        String role = (String) filter.get("role");
        Integer offset = (Integer) filter.get("offset");
        Integer limit = (Integer) filter.get("limit");

        // 分页对象
        Pageable pageable = PageRequest.of(
            offset != null && limit != null ? offset / limit : 0,
            limit != null ? limit : 10
        );

        Page<UserEntity> count;

        // 判断筛选条件组合
        boolean hasKeywords = keywords != null && !keywords.isEmpty();
        boolean hasRole = role != null && !role.isEmpty();

        if (hasKeywords && hasRole) {
            // 关键字和角色
            count = this.userRepository.searchByKeywordsAndRole(role, keywords, pageable);
        } else if (hasKeywords) {
            // 关键字
            count = this.userRepository.searchByKeywords(keywords, pageable);
        } else if (hasRole) {
            // 角色
            count = this.userRepository.searchByRole(role, pageable);
        } else {
            // 未删除的
            count = this.userRepository.fetchByNotDeleted(pageable);
        }

        return count.getContent();
    }

    @Override
    public Integer create(UserEntity user) {
        UserEntity savedUser = this.userRepository.save(user);
        return savedUser.getId() != null ? 1 : 0;
    }

    @Override
    public Integer update(UserEntity user) {
        UserEntity savedUser = this.userRepository.save(user);
        return savedUser.getId() != null ? 1 : 0;
    }

    @Override
    public Integer remove(UserEntity user) {
        user.setDeletedAt(LocalDateTime.now());
        UserEntity savedUser = this.userRepository.save(user);
        return savedUser.getId() != null ? 1 : 0;
    }

    @Override
    public Integer count(Map<String, Object> filter) {
        String keywords = (String) filter.get("keywords");
        String role = (String) filter.get("role");

        // 获取总数
        Pageable pageable = PageRequest.of(0, 1);

        // 判断筛选条件组合
        boolean hasKeywords = keywords != null && !keywords.isEmpty();
        boolean hasRole = role != null && !role.isEmpty();

        Page<UserEntity> page;

        if (hasKeywords && hasRole) {
            // 关键字和角色
            page = this.userRepository.searchByKeywordsAndRole(role, keywords, pageable);
        } else if (hasKeywords) {
            // 关键字
            page = this.userRepository.searchByKeywords(keywords, pageable);
        } else if (hasRole) {
            // 角色
            page = this.userRepository.searchByRole(role, pageable);
        } else {
            // 未删除的
            page = this.userRepository.fetchByNotDeleted(pageable);
        }

        return (int) page.getTotalElements();
    }

    @Override
    public Optional<UserEntity> fetchById(String id) {
        return this.userRepository.fetchById(id);
    }

    @Override
    public Optional<UserEntity> fetchByUsername(String username) {
        return this.userRepository.fetchByUsername(username);
    }
}
