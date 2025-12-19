package com.example.demo.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.service.WorksService;
import com.example.demo.utils.Pagination;
import com.example.demo.utils.ResultTemplate;

@RestController
@RequestMapping("/works")
public class FindWorkController {

    private final WorksService worksService;

    public FindWorkController(WorksService worksService) {
        this.worksService = worksService;
    }

    // 发现页面 - 获取所有用户审核通过的作品
    @GetMapping("/discover")
    public ResultTemplate discover(
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer pageSize,
        @RequestParam(required = false) String keywords
    ) {
        ResultTemplate result = new ResultTemplate();

        // 搜索条件
        Map<String, Object> filter = new HashMap<>();

        if (keywords != null && !keywords.isEmpty()) {
            filter.put("keywords", keywords);
        }

        // 搜索的作品总条数
        Integer total = this.worksService.countDiscoverWorks(filter);

        // 分页
        Pagination pagination = Pagination.paginate(total, pageSize, page);

        filter.put("offset", pagination.getOffset());
        filter.put("limit", pagination.getLimit());

        // 获取所有用户的审核通过作品列表
        result.putPayload("works", this.worksService.fetchDiscoverWorks(filter));
        result.putPayload("pagination", pagination);

        return result;
    }
}
