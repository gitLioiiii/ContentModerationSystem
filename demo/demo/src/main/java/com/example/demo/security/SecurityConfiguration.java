package com.example.demo.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity(debug = true)
// (debug = true)作用: 开启 Spring Security 的调试模式。
// 打印过滤器链: 在日志中详细输出每个请求经过了哪些 Security 过滤器、哪些被跳过。
// 匹配结果可见: 显示 antMatcher/requestMatcher 的匹配与否、授权决策、是否需要认证等。
// 上下文细节: 可能包含 SecurityContext、会话、CSRF 等调试信息。
public class SecurityConfiguration {

    @Bean
    SecurityFilterChain securityFilterChain(
        HttpSecurity httpSecurity,
        JwtDecoder jwtDecoder
    ) throws Exception {

        httpSecurity
            .sessionManagement(session -> {
                session.disable(); // 禁用session
            })
            .cors(cors -> {
                CorsConfiguration config = new CorsConfiguration();
                config.applyPermitDefaultValues();

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", config);

                cors.configurationSource(source);
            })
            .csrf(csrf -> {
                csrf.disable();
            })
            .logout(logout -> {
                logout.disable(); // 禁用默认的logout配置
            })
            .authorizeHttpRequests(authorize -> {
                // 允许登录和注册接口无需认证（放行）
                authorize.requestMatchers("/login", "/register","/logout").permitAll();
                // 放行静态与上传的公开资源（<img> 标签不会带 Authorization）
                authorize.requestMatchers(
                    "/assets/**",
                    "/favicon.ico",
                    "/images/**",
                    "/avatars/**",
                    "/themes/**",
                    "/ai-images/**",
                    "/ai-videos/**",
                    "/covers/**",
                    "/videos/**"
                    // "/public/**",
                    // "/static/**"
                ).permitAll();
                // 放行用户匿名注册接口
				authorize.requestMatchers("/user/create").permitAll();
                // 其他所有请求都需要JWT认证
                authorize.anyRequest().authenticated();
            })
            // JwtDecoder，用于解码JWT token
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .decoder(jwtDecoder)
                )
            );

        return httpSecurity.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

}


// 特性	        第一个配置 (自定义Token认证)	第二个配置 (OAuth2资源服务器)
// 认证方式	    自定义Token认证	标准OAuth2      JWT认证
// 认证管理器	自定义 ProviderManager + TokenAuthenticationProvider	Spring Security 自动配置
// 过滤器	    自定义 TokenAuthenticationFilter	内置 BearerTokenAuthenticationFilter
// 架构层次	    较低层，手动实现	较高层，使用Spring Security抽象
// 配置复杂度	高，需要手动实现很多组件	     低，声明式配置
// 标准化	    非标准，自定义协议	            符合OAuth2标准

//所以认证管理器和过滤器改成只需要配置JWT解码器