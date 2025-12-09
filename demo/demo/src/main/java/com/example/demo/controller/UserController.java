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

    // 管理所有用户
    @GetMapping("")
    public ResultTemplate index(
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer pageSize,
        @RequestParam(required = false) String keywords,
        @RequestParam(required = false) String role
    ) {
        ResultTemplate result   = new ResultTemplate();
        Map<String, Object> filter  = new HashMap<>();

        if (keywords != null) {
            filter.put("keywords", keywords);
        }

        if (role != null) {
            filter.put("role", role);
        }

        Integer total       = this.userService.count(filter);

        Pagination pagination   = Pagination.paginate(total, pageSize, page);

        filter.put("offset", pagination.getOffset());
        filter.put("limit", pagination.getLimit());
        result.putPayload("users", this.userService.fetch(filter));

        result.putPayload("pagination", pagination);

        return result;
    }

    // 创建用户
    @PostMapping("/create")
    public ResultTemplate create(
        @RequestBody @Validated({UserValidateGroup.Create.class}) UserEntity user, 
        BindingResult bindingResult
    ) throws ValidateFailedException {
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

    // 当前登录用户信息
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

    // 更新用户信息
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
        UserEntity User = this.userService.fetchById(user.getId()).orElseThrow(
            () -> new NotFoundException()
        );

        // 更新姓名
        if (user.getName() != null) {
            User.setName(user.getName());
        }

        // 如果提供了新密码，则更新密码
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            User.setPassword(this.passwordEncoder.encode(user.getPassword()));
        }

        // 如果提供了新头像，则更新头像
        if (user.getAvatar() != null) {
            User.setAvatar(user.getAvatar());
        }

        // 如果提供了新主题背景图片，则更新主题背景图片
        if (user.getThemeImage() != null) {
            User.setThemeImage(user.getThemeImage());
        }

        // 更新邮箱
        if (user.getEmail() != null) {
            User.setEmail(user.getEmail());
        }

        // 更新手机号
        if (user.getPhone() != null) {
            User.setPhone(user.getPhone());
        }

        // 更新生日
        if (user.getBirthday() != null) {
            User.setBirthday(user.getBirthday());
        }

        // 更新性别
        if (user.getGender() != null) {
            User.setGender(user.getGender());
        }

        // 更新省份
        if (user.getProvince() != null) {
            User.setProvince(user.getProvince());
        }

        // 更新城市
        if (user.getCity() != null) {
            User.setCity(user.getCity());
        }

        // 更新角色
        if (user.getRole() != null) {
            User.setRole(user.getRole());
        }

        // 更新账号状态
        if (user.getStatus() != null) {
            User.setStatus(user.getStatus());
        }

        this.userService.update(User);

        return result;
    }

    // 软删除用户信息
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

    // 根据id查询用户信息
    @GetMapping("/{id}")
    public ResultTemplate fetch(
        @PathVariable String id
    ) throws NotFoundException {
        ResultTemplate result   = new ResultTemplate();

    UserEntity user  = this.userService.fetchById(id).orElseThrow(
            () -> new NotFoundException()
        );

        result.putPayload("user", user);

        return result;
    }

}
