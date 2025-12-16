package com.example.demo.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

// 自定义Ollama配置属性
// 用于定义云端 Ollama 认证相关的配置
@Data
@Component
@ConfigurationProperties(prefix = "ollama.auth")
public class OllamaProperties {

// Bearer Token，用于云端 Ollama 认证
    private String bearerToken;
}
