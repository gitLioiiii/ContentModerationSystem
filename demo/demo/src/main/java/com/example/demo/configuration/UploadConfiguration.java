package com.example.demo.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

//配置绑定类
@ConfigurationProperties(prefix = "application")
public class UploadConfiguration {

    private String uploadRoot;

    public String getUploadRoot() {
        return uploadRoot;
    }

    public void setUploadRoot(String uploadRoot) {
        this.uploadRoot = uploadRoot;
    }

}
