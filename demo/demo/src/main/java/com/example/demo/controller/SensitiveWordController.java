package com.example.demo.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.SensitiveWordEntity;
import com.example.demo.exception.NotFoundException;
import com.example.demo.exception.ValidateFailedException;
import com.example.demo.service.SensitiveWordService;
import com.example.demo.utils.Pagination;
import com.example.demo.utils.ResultTemplate;
import com.example.demo.validator.SensitiveWordValidateGroup;

@RestController
@RequestMapping("/sensitive")
public class SensitiveWordController {

    private final SensitiveWordService sensitiveWordService;

    public SensitiveWordController(SensitiveWordService sensitiveWordService) {
        this.sensitiveWordService = sensitiveWordService;
    }

    // 分页查询所有敏感词
    @GetMapping("")
    public ResultTemplate index(
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer pageSize,
        @RequestParam(required = false) String keywords,
        @RequestParam(required = false) String category,
        @RequestParam(required = false) String level
    ) {
        ResultTemplate result = new ResultTemplate();
        Map<String, Object> filter = new HashMap<>();

        if (keywords != null) {
            filter.put("keywords", keywords);
        }

        if (category != null) {
            filter.put("category", category);
        }

        if (level != null) {
            filter.put("level", level);
        }

        Integer total = this.sensitiveWordService.count(filter);

        Pagination pagination = Pagination.paginate(total, pageSize, page);

        filter.put("offset", pagination.getOffset());
        filter.put("limit", pagination.getLimit());
        result.putPayload("sensitiveWords", this.sensitiveWordService.fetch(filter));

        result.putPayload("pagination", pagination);

        return result;
    }

    // 创建敏感词
    @PostMapping("/create")
    public ResultTemplate create(
        @RequestBody @Validated({SensitiveWordValidateGroup.Create.class}) SensitiveWordEntity sensitiveWord,
        BindingResult bindingResult
    ) throws ValidateFailedException {
        ResultTemplate result = new ResultTemplate();

        if (bindingResult.hasErrors()) {
            throw new ValidateFailedException();
        }

        // 检查敏感词是否已存在
        if (this.sensitiveWordService.fetchByWord(sensitiveWord.getWord()).isPresent()) {
            result.setStatus(false);
            result.setMessage("该敏感词已存在");
            return result;
        }

        sensitiveWord.setCreatedAt(LocalDateTime.now());
        this.sensitiveWordService.create(sensitiveWord);

        return result;
    }

    // 更新敏感词
    @PostMapping("/update")
    public ResultTemplate update(
        @RequestBody @Validated(SensitiveWordValidateGroup.Update.class) SensitiveWordEntity sensitiveWord,
        BindingResult bindingResult
    ) throws ValidateFailedException, NotFoundException {
        ResultTemplate result = new ResultTemplate();

        if (bindingResult.hasErrors()) {
            throw new ValidateFailedException();
        }

        // 验证敏感词是否存在
        SensitiveWordEntity existingWord = this.sensitiveWordService.fetchById(sensitiveWord.getId()).orElseThrow(
            () -> new NotFoundException()
        );

        // 更新敏感词内容
        if (sensitiveWord.getWord() != null) {
            existingWord.setWord(sensitiveWord.getWord());
        }
        // 更新分类
        if (sensitiveWord.getCategory() != null) {
            existingWord.setCategory(sensitiveWord.getCategory());
        }
        // 更新级别
        if (sensitiveWord.getLevel() != null) {
            existingWord.setLevel(sensitiveWord.getLevel());
        }
        // 更新生效启用状态
        if (sensitiveWord.getEffective() != null) {
            existingWord.setEffective(sensitiveWord.getEffective());
        }

        this.sensitiveWordService.update(existingWord);

        return result;
    }

    // 删除敏感词
    @PostMapping("/remove")
    public ResultTemplate remove(
        @RequestBody @Validated(SensitiveWordValidateGroup.Remove.class) SensitiveWordEntity fields,
        BindingResult bindingResult
    ) throws ValidateFailedException, NotFoundException {
        ResultTemplate result = new ResultTemplate();

        if (bindingResult.hasErrors()) {
            throw new ValidateFailedException();
        }

        SensitiveWordEntity sensitiveWord = this.sensitiveWordService.fetchById(fields.getId()).orElseThrow(
            () -> new NotFoundException()
        );

        this.sensitiveWordService.remove(sensitiveWord);

        return result;
    }

    // 根据id查询敏感词
    @GetMapping("/{id}")
    public ResultTemplate fetch(
        @PathVariable String id
    ) throws NotFoundException {
        ResultTemplate result = new ResultTemplate();

        SensitiveWordEntity sensitiveWord = this.sensitiveWordService.fetchById(id).orElseThrow(
            () -> new NotFoundException()
        );

        result.putPayload("sensitiveWord", sensitiveWord);

        return result;
    }
}
