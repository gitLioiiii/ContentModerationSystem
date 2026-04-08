package com.example.demo.controller;

import com.example.demo.entity.ContentEntity;
import com.example.demo.entity.ManualReviewEntity;
import com.example.demo.entity.UserEntity;
import com.example.demo.entity.WorkAutoReviewEntity;
import com.example.demo.repository.ContentRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.WorkAutoReviewRepository;
import com.example.demo.service.ManualReviewService;
import com.example.demo.utils.ResultTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

// 人工审核控制器
@Slf4j
@RestController
@RequestMapping("/queue")
public class ManualModerationController {

    private final ContentRepository contentRepository;
    private final WorkAutoReviewRepository workAutoReviewRepository;
    private final ManualReviewService manualReviewService;
    private final UserRepository userRepository;

    public ManualModerationController(
            ContentRepository contentRepository,
            WorkAutoReviewRepository workAutoReviewRepository,
            ManualReviewService manualReviewService,
            UserRepository userRepository) {
        this.contentRepository = contentRepository;
        this.workAutoReviewRepository = workAutoReviewRepository;
        this.manualReviewService = manualReviewService;
        this.userRepository = userRepository;
    }

    // 获取待审核队列（分页）
    @GetMapping("")
    public ResultTemplate getQueue(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String date,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int pageSize) {

        log.info("获取审核队列 - 状态: {}, 日期: {}, 页码: {}, 每页大小: {}", status, date, page, pageSize);

        try {
            // 创建分页对象（页码从0开始）
            Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));

            Page<ContentEntity> contentPage;

            // 根据状态筛选
            if (status != null && !status.isEmpty()) {
                // 如果前端传的是"pending"，则查询"reviewing"和"appealing"状态（待审核和申诉中）
                if ("pending".equals(status)) {
                    List<String> statuses = Arrays.asList("reviewing", "appealing");
                    contentPage = contentRepository.findByStatusInAndDeletedAtIsNull(statuses, pageable);
                } else {
                    contentPage = contentRepository.findByStatusAndDeletedAtIsNull(status, pageable);
                }
            } else {
                // 默认查询状态为"reviewing"和"appealing"的作品（需要人工审核和申诉中）
                List<String> statuses = Arrays.asList("reviewing", "appealing");
                contentPage = contentRepository.findByStatusInAndDeletedAtIsNull(statuses, pageable);
            }

            // 转换为前端所需的格式
            List<Map<String, Object>> queueList = contentPage.getContent().stream().map(content -> {
                Map<String, Object> item = new HashMap<>();

                // 基本信息
                item.put("contentId", content.getId());
                item.put("workTitle", content.getTitle());
                item.put("username", content.getAuthor());

                // 查询AI审核记录获取触发时间
                Optional<WorkAutoReviewEntity> autoReviewOpt = workAutoReviewRepository.findByContentId(content.getId());
                if (autoReviewOpt.isPresent()) {
                    WorkAutoReviewEntity autoReview = autoReviewOpt.get();
                    item.put("manualReviewTriggeredAt", autoReview.getReviewedAt());
                } else {
                    item.put("manualReviewTriggeredAt", null);
                }

                // 查询初次人工审核记录（first_review）
                Optional<ManualReviewEntity> firstReviewOpt = manualReviewService.findLatestByContentIdAndReviewType(content.getId(), "first_review");
                if (firstReviewOpt.isPresent()) {
                    ManualReviewEntity firstReview = firstReviewOpt.get();
                    item.put("reviewCompletedAt", firstReview.getReviewedAt());
                    item.put("reviewerName", firstReview.getReviewerName()); // 审核员姓名
                    item.put("reviewReason", firstReview.getReason()); // 初次审核理由
                } else {
                    item.put("reviewCompletedAt", null);
                    item.put("reviewerName", null);
                    item.put("reviewReason", null);
                }

                // 申诉信息（从 manual_review 表查询 appeal_review）
                Optional<ManualReviewEntity> appealReviewOpt = manualReviewService.findLatestByContentIdAndReviewType(content.getId(), "appeal_review");
                if (appealReviewOpt.isPresent()) {
                    ManualReviewEntity appealReview = appealReviewOpt.get();
                    item.put("appealReason", appealReview.getReason()); // 申诉理由
                    item.put("appealedAt", appealReview.getReviewedAt()); // 申诉时间
                } else {
                    item.put("appealReason", null);
                    item.put("appealedAt", null);
                }

                // 状态映射（将数据库状态映射为前端显示状态）
                String displayStatus = mapContentStatus(content.getStatus());
                item.put("status", displayStatus);

                return item;
            }).collect(Collectors.toList());

            // 构建分页信息
            Map<String, Object> pagination = new HashMap<>();
            pagination.put("currentPage", page);
            pagination.put("pageSize", pageSize);
            pagination.put("total", contentPage.getTotalElements());

            // 返回结果
            ResultTemplate result = new ResultTemplate();
            result.putPayload("queueList", queueList);
            result.putPayload("pagination", pagination);

            log.info("成功获取审核队列 - 共 {} 条记录", queueList.size());
            return result;

        } catch (Exception e) {
            log.error("获取审核队列失败", e);
            return new ResultTemplate()
                    .setStatus(false)
                    .setMessage("获取审核队列失败: " + e.getMessage());
        }
    }

    // 提交审核结果
    @PostMapping("/review")
    public ResultTemplate submitReview(
            @RequestBody Map<String, Object> requestBody,
            Authentication authentication) {

        String contentId = (String) requestBody.get("contentId");
        String decision = (String) requestBody.get("decision");
        String comment = (String) requestBody.get("comment");

        log.info("提交审核 - 作品ID: {}, 决定: {}, 意见: {}", contentId, decision, comment);

        try {
            // 获取当前审核员信息
            String reviewerUsername = authentication.getName();
            Optional<UserEntity> reviewerOpt = userRepository.findByUsernameAndDeletedAtIsNull(reviewerUsername);

            if (!reviewerOpt.isPresent()) {
                return new ResultTemplate()
                        .setStatus(false)
                        .setMessage("审核员信息不存在");
            }

            UserEntity reviewer = reviewerOpt.get();

            // 查询作品信息
            Optional<ContentEntity> contentOpt = contentRepository.findByIdAndDeletedAtIsNull(contentId);
            if (!contentOpt.isPresent()) {
                return new ResultTemplate()
                        .setStatus(false)
                        .setMessage("作品不存在或已删除");
            }

            ContentEntity content = contentOpt.get();

            // 判断审核类型：如果作品是申诉中状态，则为申诉审核
            String reviewType = "appealing".equals(content.getStatus()) ? "appeal_review" : "first_review";

            // 创建人工审核记录
            ManualReviewEntity manualReview = new ManualReviewEntity();
            manualReview.setContentId(contentId);
            manualReview.setWorkTitle(content.getTitle());
            manualReview.setUsername(content.getAuthor());
            manualReview.setReviewerId(reviewer.getId());
            manualReview.setReviewerName(reviewer.getName()); // 设置审核员姓名
            manualReview.setDecision(decision);
            manualReview.setReason(comment);
            manualReview.setReviewType(reviewType);
            manualReview.setReviewedAt(LocalDateTime.now());

            // 保存审核记录
            manualReviewService.save(manualReview);

            // 更新作品状态
            if ("pass".equals(decision)) {
                content.setStatus("approved");
                if ("appeal_review".equals(reviewType)) {
                    content.setAppealStatus("approved");
                }
            } else if ("reject".equals(decision)) {
                if ("appeal_review".equals(reviewType)) {
                    content.setStatus("appeal_rejected");
                    content.setAppealStatus("rejected");
                } else {
                    content.setStatus("rejected");
                }
            }
            contentRepository.save(content);

            log.info("审核提交成功 - 作品ID: {}, 审核决定: {}, 审核员: {}",
                    contentId, decision, reviewerUsername);

            return new ResultTemplate()
                    .setStatus(true)
                    .setMessage("审核提交成功");

        } catch (Exception e) {
            log.error("审核提交失败 - 作品ID: {}", contentId, e);
            return new ResultTemplate()
                    .setStatus(false)
                    .setMessage("审核提交失败: " + e.getMessage());
        }
    }

    // 状态映射（将数据库状态映射为前端显示状态）
    private String mapContentStatus(String status) {
        switch (status) {
            case "pending":
                return "pending";
            case "reviewing":
                return "pending";  // reviewing状态在前端显示为pending（待人工审核）
            case "appealing":
                return "appealed";  // appealing状态在前端显示为appealed（有用户申诉）
            case "approved":
                return "approved";
            case "rejected":
                return "rejected";
            default:
                return status;
        }
    }
}
