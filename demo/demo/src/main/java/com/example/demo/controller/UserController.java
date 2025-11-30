package com.example.demo.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.UserEntity;
import com.example.demo.exception.NotFoundException;
import com.example.demo.exception.ValidateFailedException;
import com.example.demo.service.UserService;
import com.example.demo.utils.Pagination;
import com.example.demo.utils.ResultTemplate;
import com.example.demo.validator.UserValidateGroup;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    private final PasswordEncoder passwordEncoder;

    public UserController(
        UserService userService, 
        PasswordEncoder passwordEncoder
    ) {
        this.userService        = userService;
        this.passwordEncoder    = passwordEncoder;
    }

    //mark 分页显示用户
    @GetMapping("")
    public ResultTemplate index(
        @RequestParam(name = "page", required = false) Integer page, 
        @RequestParam(name = "pageSize", required = false) Integer pageSize, 
        @RequestParam(name = "keywords", required = false) String keywords
    ) {
        ResultTemplate result   = new ResultTemplate();
        Map<String, Object> filter  = new HashMap<>();

        if (keywords != null) {
            filter.put("keywords", keywords);
        }

        Integer total       = this.userService.count(filter);

        Pagination pagination   = Pagination.paginate(total, pageSize, page);

        filter.put("offset", pagination.getOffset());
        filter.put("limit", pagination.getLimit());
        result.putPayload("users", this.userService.fetch(filter));

        result.putPayload("pagination", pagination);

        return result;
    }

    // mark 创建用户
    @PostMapping("/create")
    public ResultTemplate create(
        @RequestBody @Validated({UserValidateGroup.Create.class}) UserEntity user, 
        BindingResult bindingResult
    ) {
        ResultTemplate result   = new ResultTemplate();

        if (bindingResult.hasErrors()) {
            throw new ValidateFailedException();
        }

        // 检查用户名是否已存在
        if (this.userService.fetchByUsername(user.getUsername()).isPresent()) {
            result.setStatus(false);
            result.setMessage("用户名已存在，请选择其他用户名");
            return result;
        }

        user.setPassword(
            this.passwordEncoder.encode(user.getPassword())
        );
        user.setRegisteredAt(LocalDateTime.now());
        this.userService.create(user);

        return result;
    }

    //mark 当前登录用户信息
    @GetMapping("/current")
    public ResultTemplate current() throws NotFoundException {
        ResultTemplate result = new ResultTemplate();

        // 从 SecurityContext 获取当前认证用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new NotFoundException();
        }

        // 获取用户名
        String username = authentication.getName();

        // 根据用户名查询用户信息
        UserEntity user = this.userService.fetchByUsername(username).orElseThrow(
            () -> new NotFoundException()
        );

        result.putPayload("user", user);

        return result;
    }

    // mark 更新用户信息
    @PostMapping("/update")
    public ResultTemplate update(
        @RequestBody @Validated(UserValidateGroup.Update.class) UserEntity user,
        BindingResult bindingResult
    ) throws ValidateFailedException, NotFoundException {
        ResultTemplate result   = new ResultTemplate();

        if (bindingResult.hasErrors()) {
            throw new ValidateFailedException();
        }

        // 验证用户是否存在
        UserEntity exist_User = this.userService.fetchById(user.getId()).orElseThrow(
            () -> new NotFoundException()
        );

        // 更新姓名
        if (user.getName() != null) {
            exist_User.setName(user.getName());
        }

        // 如果提供了新密码，则更新密码
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            exist_User.setPassword(this.passwordEncoder.encode(user.getPassword()));
        }

        // 如果提供了新头像，则更新头像
        if (user.getAvatar() != null) {
            exist_User.setAvatar(user.getAvatar());
        }

        // 如果提供了新主题背景图片，则更新主题背景图片
        if (user.getThemeImage() != null) {
            exist_User.setThemeImage(user.getThemeImage());
        }

        // 更新邮箱
        if (user.getEmail() != null) {
            exist_User.setEmail(user.getEmail());
        }

        // 更新手机号
        if (user.getPhone() != null) {
            exist_User.setPhone(user.getPhone());
        }

        // 更新生日
        if (user.getBirthday() != null) {
            exist_User.setBirthday(user.getBirthday());
        }

        // 更新性别
        if (user.getGender() != null) {
            exist_User.setGender(user.getGender());
        }

        this.userService.update(exist_User);

        // 返回更新后的用户信息
        result.putPayload("user", exist_User);

        return result;
    }

    // mark 软删除用户信息
    @PostMapping("/remove")
    public ResultTemplate remove(
        @RequestBody @Validated(UserValidateGroup.Remove.class) UserEntity fields, 
        BindingResult bindingResult
    ) throws ValidateFailedException, NotFoundException {
        ResultTemplate result   = new ResultTemplate();

        if (bindingResult.hasErrors()) {
            throw new ValidateFailedException();
        }

    UserEntity user  = this.userService.fetchById(fields.getId()).orElseThrow(
            () -> new NotFoundException()
        );

        user.setDeletedAt(LocalDateTime.now());
        this.userService.remove(user);

        return result;
    }

    // mark 根据id查询用户信息
    @GetMapping("/{id}")
    public ResultTemplate fetch(
        @PathVariable("id") String id
    ) throws NotFoundException {
        ResultTemplate result   = new ResultTemplate();

    UserEntity user  = this.userService.fetchById(id).orElseThrow(
            () -> new NotFoundException()
        );

        result.putPayload("user", user);

        return result;
    }

}
