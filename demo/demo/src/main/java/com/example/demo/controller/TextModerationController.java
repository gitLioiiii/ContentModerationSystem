package com.example.demo.controller;

import com.example.demo.dto.TextAutoRequest;
import com.example.demo.dto.TextAutoResponse;
import com.example.demo.exception.ValidateFailedException;
import com.example.demo.service.TextAutoService;
import com.example.demo.utils.ResultTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/moderation")
public class TextModerationController {

    private final TextAutoService textAutoService;

    public TextModerationController(TextAutoService textAutoService) {
        this.textAutoService = textAutoService;
    }
    
    @PostMapping("/text")
    public ResultTemplate match_Ai_Text(
            @RequestBody @Validated TextAutoRequest request,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            log.warn("文本审核请求参数校验失败: {}", bindingResult.getAllErrors());
            throw new ValidateFailedException();
        }

        log.info("接收到文本审核请求，内容长度: {} 字符", request.getContent().length());
        log.debug("审核文本内容: {}", request.getContent());

        // 调用 Spring AI Service 进行文本审核
        TextAutoResponse response = textAutoService.AiText(request);

        // 构建响应结果
        ResultTemplate result = new ResultTemplate();
        result.putPayload("moderation", response);

        log.info("文本审核完成 - 结果: {}, 风险等级: {}", response.getResult(), response.getRiskLevel());

        return result;
    }
}
