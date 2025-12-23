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
import com.example.demo.entity.ManualReviewEntity;
import com.example.demo.entity.UserEntity;
import com.example.demo.exception.NotFoundException;
import com.example.demo.exception.ValidateFailedException;
import com.example.demo.service.ManualReviewService;
import com.example.demo.service.UserService;
import com.example.demo.service.WorksService;
import com.example.demo.utils.Pagination;
import com.example.demo.utils.ResultTemplate;

@RestController
@RequestMapping("/works")
public class WorksController {

    private final WorksService worksService;
    private final UserService userService;
    private final ManualReviewService manualReviewService;

    public WorksController(WorksService worksService, UserService userService, ManualReviewService manualReviewService) {
        this.worksService = worksService;
        this.userService = userService;
        this.manualReviewService = manualReviewService;
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

    // 申诉作品
    @PostMapping("/appeal")
    public ResultTemplate appeal(@RequestBody Map<String, String> requestBody) throws NotFoundException {
        ResultTemplate result = new ResultTemplate();

        // 获取参数
        String workId = requestBody.get("workId");
        String reason = requestBody.get("reason");

        if (workId == null || workId.isEmpty()) {
            result.setStatus(false);
            result.setMessage("作品ID不能为空");
            return result;
        }

        if (reason == null || reason.trim().isEmpty()) {
            result.setStatus(false);
            result.setMessage("申诉理由不能为空");
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
            result.setMessage("无权对该作品提出申诉");
            return result;
        }

        // 检查作品状态是否可以申诉（只有被拒绝、人工审核不通过或申诉被拒绝的作品可以申诉）
        if (!"rejected".equals(content.getStatus()) &&
            !"manual_rejected".equals(content.getStatus()) &&
            !"appeal_rejected".equals(content.getStatus())) {
            result.setStatus(false);
            result.setMessage("该作品当前状态不允许申诉");
            return result;
        }

        // 允许用户多次申诉，不再检查是否已经申诉过

        // 更新作品的申诉状态和主状态
        content.setAppealStatus("appealing");
        content.setStatus("appealing"); // 将主状态也更新为 appealing，这样审核队列可以查询到
        this.worksService.update(content);

        // 创建申诉记录（作为 manual_review 的一条记录）
        ManualReviewEntity appealReview = new ManualReviewEntity();
        appealReview.setContentId(workId);
        appealReview.setWorkTitle(content.getTitle());
        appealReview.setUsername(content.getAuthor());
        appealReview.setReviewerId(null); // 还未分配审核员
        appealReview.setReviewerName(null);
        appealReview.setDecision(null); // 待审核
        appealReview.setReason(reason.trim()); // 申诉理由
        appealReview.setReviewType("appeal_review"); // 申诉审核类型
        appealReview.setReviewedAt(LocalDateTime.now()); // 申诉提交时间

        // 保存申诉记录
        this.manualReviewService.save(appealReview);

        result.setMessage("申诉提交成功，请等待审核");

        return result;
    }

}
