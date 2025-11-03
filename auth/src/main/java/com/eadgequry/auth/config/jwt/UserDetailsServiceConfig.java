package com.eadgequry.auth.config.jwt;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.eadgequry.auth.repository.UserRepository;
 

@Configuration
public class UserDetailsServiceConfig {

    @Bean
    public CustomUserDetailsService customUserDetailsService(UserRepository userRepository,PasswordEncoder passwordEncoder) {
        return new CustomUserDetailsService(userRepository, passwordEncoder);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}