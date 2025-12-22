package com.example.demo.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.ContentEntity;
import com.example.demo.entity.UserEntity;
import com.example.demo.exception.NotFoundException;
import com.example.demo.exception.ValidateFailedException;
import com.example.demo.service.UserService;
import com.example.demo.service.WorksService;
import com.example.demo.utils.Pagination;
import com.example.demo.utils.ResultTemplate;

@RestController
@RequestMapping("/works")
public class WorksController {

    private final WorksService worksService;
    private final UserService userService;

    public WorksController(WorksService worksService, UserService userService) {
        this.worksService = worksService;
        this.userService = userService;
    }

    // 作品列表分页
    @GetMapping("")
    public ResultTemplate index(
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer pageSize,
        @RequestParam(required = false) String keywords,
        @RequestParam(required = false) String status
    ) throws NotFoundException {
        ResultTemplate result = new ResultTemplate();

        // 获取当前用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new NotFoundException();
        }

        String username = authentication.getName();
        UserEntity user = this.userService.fetchByUsername(username).orElseThrow(
            () -> new NotFoundException()
        );

        // 搜索
        Map<String, Object> filter = new HashMap<>();
        filter.put("userId", user.getId());

        if (keywords != null && !keywords.isEmpty()) {
            filter.put("keywords", keywords);
        }

        if (status != null && !status.isEmpty()) {
            filter.put("status", status);
        }

        // 搜索的作品总条数
        Integer total = this.worksService.count(filter);

        // 分页
        Pagination pagination = Pagination.paginate(total, pageSize, page);

        filter.put("offset", pagination.getOffset());
        filter.put("limit", pagination.getLimit());

        // 获取作品列表
        result.putPayload("works", this.worksService.fetch(filter));
        result.putPayload("pagination", pagination);

        return result;
    }

    // 创建作品
    @PostMapping("/create")
    public ResultTemplate create(
        @RequestBody @Validated ContentEntity content,
        BindingResult bindingResult
    ) throws ValidateFailedException, NotFoundException {
        ResultTemplate result = new ResultTemplate();

        if (bindingResult.hasErrors()) {
            throw new ValidateFailedException();
        }

        // 获取当前用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new NotFoundException();
        }

        String username = authentication.getName();
        UserEntity user = this.userService.fetchByUsername(username).orElseThrow(
            () -> new NotFoundException()
        );

        content.setUserId(user.getId());
        content.setAuthorAvatar(user.getAvatar());
        content.setAuthor(user.getUsername());
        content.setStatus("pending"); // 待审核状态
        content.setCreatedAt(LocalDateTime.now());
        content.setViolationCount(0);
        content.setAppealStatus("none");

        if (content.getLocation() == null) {
            ContentEntity.Location location = new ContentEntity.Location();
            location.setProvince("");
            location.setCity("");
            content.setLocation(location);
        }

        // 保存作品
        this.worksService.create(content);

        result.setMessage("作品发布成功，正在审核中");

        return result;
    }

    // 删除作品
    @PostMapping("/delete")
    public ResultTemplate delete(
        @RequestBody Map<String, String> requestBody
    ) throws NotFoundException {
        ResultTemplate result = new ResultTemplate();

        // 获取作品ID
        String workId = requestBody.get("id");
        if (workId == null || workId.isEmpty()) {
            result.setStatus(false);
            result.setMessage("作品ID不能为空");
            return result;
        }

        // 获取当前用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new NotFoundException();
        }

        String username = authentication.getName();
        UserEntity user = this.userService.fetchByUsername(username).orElseThrow(
            () -> new NotFoundException()
        );

        // 获取作品信息
        ContentEntity content = this.worksService.fetchById(workId).orElseThrow(
            () -> {
                result.setStatus(false);
                result.setMessage("作品不存在");
                return new NotFoundException();
            }
        );

        // 验证作品是否属于当前用户
        if (!content.getUserId().equals(user.getId())) {
            result.setStatus(false);
            result.setMessage("无权删除该作品");
            return result;
        }

        // 删除作品
        this.worksService.remove(content);

        result.setMessage("删除成功");

        return result;
    }

    // 获取作品详情
    @GetMapping("/detail")
    public ResultTemplate detail(@RequestParam String contentId) {
        ResultTemplate result = new ResultTemplate();

        try {
            // 获取作品信息
            ContentEntity content = this.worksService.fetchById(contentId).orElseThrow(
                () -> new NotFoundException()
            );

            result.putPayload("work", content);
            return result;

        } catch (NotFoundException e) {
            result.setStatus(false);
            result.setMessage("作品不存在");
            return result;
        } catch (Exception e) {
            result.setStatus(false);
            result.setMessage("获取作品详情失败: " + e.getMessage());
            return result;
        }
    }

}
