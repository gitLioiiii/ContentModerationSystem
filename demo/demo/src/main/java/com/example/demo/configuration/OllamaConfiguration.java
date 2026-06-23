package com.example.demo.configuration;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import lombok.RequiredArgsConstructor;

// 配置本地和云端 Ollama
@Configuration
@RequiredArgsConstructor
public class OllamaConfiguration {

    @Value("${spring.ai.ollama.base-url}")
    private String cloudBaseUrl;

    @Value("${spring.ai.ollama.chat.options.model}")
    private String cloudModel;

    private final OllamaProperties ollamaProperties;

// 自定义OllamaApi Bean
    // 云端 OllamaApi（带认证）
    @Bean
    public OllamaApi ollamaApi() {
        RestClient.Builder restClientBuilder = RestClient.builder()
                .baseUrl(cloudBaseUrl);

        // 如果配置了 bearer token，则添加认证头
        String bearerToken = ollamaProperties.getAuth().getBearerToken();
        if (bearerToken != null && !bearerToken.isEmpty()) {
            restClientBuilder.defaultHeader("Authorization", "Bearer " + bearerToken);
        }

        // 使用 builder 模式创建 OllamaApi
        return OllamaApi.builder()
                .baseUrl(cloudBaseUrl)
                .restClientBuilder(restClientBuilder)
                .build();
    }

    // 云端 ChatClient（用于视频抽帧审核）
    @Bean("cloudChatClient")
    public ChatClient cloudChatClient(OllamaApi ollamaApi) {
        OllamaChatModel chatModel = OllamaChatModel.builder()
                .ollamaApi(ollamaApi)
                .defaultOptions(OllamaChatOptions.builder()
                        .model(cloudModel)
                        .build())
                .build();
        return ChatClient.builder(chatModel).build();
    }

    // 本地 ChatClient（用于文本和图片审核）
//     @Bean("localChatClient")
//     public ChatClient localChatClient() {
//         OllamaApi localApi = OllamaApi.builder()
//                 .baseUrl(ollamaProperties.getLocal().getBaseUrl())
//                 .build();
//         OllamaChatModel chatModel = OllamaChatModel.builder()
//                 .ollamaApi(localApi)
//                 .defaultOptions(OllamaChatOptions.builder()
//                         .model(ollamaProperties.getLocal().getModel())
//                         .build())
//                 .build();
//         return ChatClient.builder(chatModel).build();
//     }
}
