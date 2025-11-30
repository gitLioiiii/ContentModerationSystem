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
        Integer offset = (Integer) filter.get("offset");
        Integer limit = (Integer) filter.get("limit");

        // 创建分页对象
        Pageable pageable = PageRequest.of(
            offset != null && limit != null ? offset / limit : 0,
            limit != null ? limit : 10
        );

        Page<UserEntity> page;
        if (keywords != null && !keywords.isEmpty()) {
            page = this.userRepository.searchByKeywords(keywords, pageable);
        } else {
            page = this.userRepository.fetchByNotDeleted(pageable);
        }

        return page.getContent();
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

        if (keywords != null && !keywords.isEmpty()) {
            return (int) this.userRepository.countByKeywords(keywords);
        } else {
            return (int) this.userRepository.countByNotDeleted();
        }
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
