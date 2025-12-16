package com.example.demo.configuration;

import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import lombok.RequiredArgsConstructor;

// 配置云端 Ollama 的认证
@Configuration
@RequiredArgsConstructor
public class OllamaConfiguration {

    @Value("${spring.ai.ollama.base-url}")
    private String baseUrl;

    private final OllamaProperties ollamaProperties;

    // 自定义OllamaApi Bean
    // 如果配置了 bearer token，则自动添加认证头
    @Bean
    public OllamaApi ollamaApi() {
        // 创建 RestClient.Builder 并添加认证头
        RestClient.Builder restClientBuilder = RestClient.builder()
                .baseUrl(baseUrl);

        // 如果配置了 bearer token，则添加认证头
        String bearerToken = ollamaProperties.getBearerToken();
        if (bearerToken != null && !bearerToken.isEmpty()) {
            restClientBuilder.defaultHeader("Authorization", "Bearer " + bearerToken);
        }

        // 使用 builder 模式创建 OllamaApi
        return OllamaApi.builder()
                .baseUrl(baseUrl)
                .restClientBuilder(restClientBuilder)
                .build();
    }
}
