package com.example.demo.controller;

import com.example.demo.entity.ManualReviewEntity;
import com.example.demo.entity.UserEntity;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.ManualReviewService;
import com.example.demo.utils.ResultTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

// 人工审核记录控制器
@Slf4j
@RestController
@RequestMapping("/manual-review")
public class ManualReviewRecordController {

    private final ManualReviewService manualReviewService;
    private final UserRepository userRepository;

    public ManualReviewRecordController(
            ManualReviewService manualReviewService,
            UserRepository userRepository) {
        this.manualReviewService = manualReviewService;
        this.userRepository = userRepository;
    }

    // 获取审核记录（分页+筛选）
    @GetMapping("/records")
    public ResultTemplate getRecords(
            @RequestParam(required = false) String decision,
            @RequestParam(required = false) String reviewType,
            @RequestParam(required = false) String reviewerName,
            @RequestParam(required = false) String reviewDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {

        log.info("获取审核记录 - 决定: {}, 类型: {}, 审核人: {}, 日期: {}, 页码: {}, 每页大小: {}",
                decision, reviewType, reviewerName, reviewDate, page, pageSize);

        try {
            // 创建分页对象（页码从0开始）
            Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "reviewedAt"));

            // 处理日期参数
            LocalDate date = null;
            if (reviewDate != null && !reviewDate.isEmpty()) {
                try {
                    date = LocalDate.parse(reviewDate, DateTimeFormatter.ISO_LOCAL_DATE);
                } catch (Exception e) {
                    log.warn("日期格式解析失败: {}", reviewDate, e);
                }
            }

            // 调用服务层方法进行筛选查询
            Page<ManualReviewEntity> recordPage = manualReviewService.findByFilters(
                    decision, reviewType, reviewerName, date, pageable);

            // 转换为前端所需的格式
            List<Map<String, Object>> recordList = recordPage.getContent().stream().map(record -> {
                Map<String, Object> item = new HashMap<>();
                item.put("id", record.getId()); // 添加ID字段用于删除
                item.put("contentId", record.getContentId());
                item.put("workTitle", record.getWorkTitle());
                item.put("username", record.getUsername());
                item.put("reviewerName", record.getReviewerName());
                item.put("decision", record.getDecision());
                item.put("reason", record.getReason());
                item.put("reviewType", record.getReviewType());
                item.put("reviewedAt", record.getReviewedAt());
                return item;
            }).collect(Collectors.toList());

            // 构建分页信息
            Map<String, Object> pagination = new HashMap<>();
            pagination.put("currentPage", page);
            pagination.put("pageSize", pageSize);
            pagination.put("total", recordPage.getTotalElements());

            // 返回结果
            ResultTemplate result = new ResultTemplate();
            result.putPayload("records", recordList);
            result.putPayload("pagination", pagination);

            log.info("成功获取审核记录 - 共 {} 条记录", recordList.size());
            return result;

        } catch (Exception e) {
            log.error("获取审核记录失败", e);
            return new ResultTemplate()
                    .setStatus(false)
                    .setMessage("获取审核记录失败: " + e.getMessage());
        }
    }

    // 获取管理员列表（用于筛选下拉框）
    @GetMapping("/admins")
    public ResultTemplate getAdmins() {
        log.info("获取管理员列表");

        try {
            // 查询所有管理员用户（角色为admin）
            List<UserEntity> admins = userRepository.findByRoleAndDeletedAtIsNull("admin");

            // 转换为前端所需的格式
            List<Map<String, Object>> adminList = admins.stream().map(admin -> {
                Map<String, Object> item = new HashMap<>();
                item.put("id", admin.getId());
                item.put("username", admin.getUsername());
                item.put("name", admin.getName());
                return item;
            }).collect(Collectors.toList());

            // 返回结果
            ResultTemplate result = new ResultTemplate();
            result.putPayload("admins", adminList);

            log.info("成功获取管理员列表 - 共 {} 位管理员", adminList.size());
            return result;

        } catch (Exception e) {
            log.error("获取管理员列表失败", e);
            return new ResultTemplate()
                    .setStatus(false)
                    .setMessage("获取管理员列表失败: " + e.getMessage());
        }
    }

    // 删除审核记录（硬删除）
    @PostMapping("/records/remove")
    public ResultTemplate deleteRecord(@RequestBody Map<String, String> request) {
        String id = request.get("id");
        log.info("删除审核记录 - ID: {}", id);

        try {
            // 查询审核记录是否存在
            Optional<ManualReviewEntity> recordOpt = manualReviewService.findById(id);
            if (!recordOpt.isPresent()) {
                log.warn("审核记录不存在 - ID: {}", id);
                return new ResultTemplate()
                        .setStatus(false)
                        .setMessage("审核记录不存在");
            }

            // 硬删除审核记录
            manualReviewService.deleteById(id);

            log.info("成功删除审核记录 - ID: {}", id);
            return new ResultTemplate()
                    .setStatus(true)
                    .setMessage("删除成功");

        } catch (Exception e) {
            log.error("删除审核记录失败 - ID: {}", id, e);
            return new ResultTemplate()
                    .setStatus(false)
                    .setMessage("删除失败: " + e.getMessage());
        }
    }
}
