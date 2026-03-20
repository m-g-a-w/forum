package com.knowledge.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // 为了 KISS 原则，我们禁用 Security 的拦截，改用自定义 JWT Interceptor
        http.csrf().disable()
            .authorizeRequests().anyRequest().permitAll()
            .and()
            .formLogin().disable()
            .httpBasic().disable()
            .cors(); // 允许跨域

        return http.build();
    }
}
