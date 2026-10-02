package com.dangbooking.tour.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

// Chi dung de bam/kiem tra mat khau kieu bcrypt, tuong thich voi password_hash() cua PHP.
// Khong co Spring Security day du nen KHONG tu dong chan API nao ca.
@Configuration
public class PasswordConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
