package com.example.demo.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.example.demo.validator.UserValidateGroup;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

// 用户表
@Data
@Document(collection = "user")
public class UserEntity {

    @Id
    private String id;

    @NotBlank(
        groups = {
        UserValidateGroup.Create.class,
        UserValidateGroup.Login.class
    })
    @Size(
        min = 2,
        max = 16,
        groups = {
        UserValidateGroup.Create.class,
        UserValidateGroup.Login.class
    })
    @Indexed(unique = true)
    private String username;

    @Size(
        max = 32,
        groups = {
        UserValidateGroup.Create.class,
        UserValidateGroup.Update.class
    })
    private String name;

    @NotBlank(
        groups = {
        UserValidateGroup.Create.class,
        UserValidateGroup.Login.class
	})
	@Size(
        min = 6,
        max = 16,
        groups = {
		UserValidateGroup.Create.class,
		UserValidateGroup.Login.class
	})
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    //角色
    //用户或者管理员
    @NotBlank(
        groups = {
        UserValidateGroup.Login.class    
    })
    @Indexed
    private String role = "user";

    private String avatar;

    @Field("backgroundImage")
    private String themeImage;

    @Indexed(unique = true, sparse = true)
    private String phone;

    @Email(
        groups = {
        UserValidateGroup.Create.class,
        UserValidateGroup.Update.class
    })
    @Size(
        max = 100,
        groups = {
        UserValidateGroup.Create.class,
        UserValidateGroup.Update.class
    })
    @Indexed(unique = true, sparse = true)
    private String email;

    private LocalDate birthday;

    private String gender = "none"; // man, woman, none

    private String province; // 省份

    private String city; // 城市

    // 登录状态可登录和不可登录
    private String status = "active"; // active 或 ban

    private LocalDateTime deletedAt;

	private LocalDateTime registeredAt;

}
