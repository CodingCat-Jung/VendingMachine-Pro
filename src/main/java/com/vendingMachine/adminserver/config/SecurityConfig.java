package com.vendingMachine.adminserver.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.context.annotation.Bean;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/admin").authenticated()
                        .anyRequest().permitAll()
                )
                .formLogin(form -> form
                        .loginPage("/login")                  // 커스텀 로그인 페이지
                        .defaultSuccessUrl("/admin", true)     // 로그인 성공 시 /admin으로 이동
                        .failureUrl("/login?error=true")       // 로그인 실패 시 ?error=true 전달
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout=true")  // 로그아웃 시 ?logout=true 전달
                        .permitAll()
                );

        return http.build();
    }
}
