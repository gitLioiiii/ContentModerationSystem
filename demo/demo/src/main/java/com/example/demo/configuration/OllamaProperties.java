package com.example.demo.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

// 自定义Ollama配置属性
@Data
@Component
@ConfigurationProperties(prefix = "ollama")
public class OllamaProperties {

    // 云端认证配置
    private Auth auth = new Auth();

    // 本地 Ollama 配置
    private Local local = new Local();

    @Data
    public static class Auth {
        // Bearer Token，用于云端 Ollama 认证
        private String bearerToken;
    }

    @Data
    public static class Local {
        // 本地 Ollama 地址
        private String baseUrl;
        // 本地模型名称
        private String model;
    }
}
