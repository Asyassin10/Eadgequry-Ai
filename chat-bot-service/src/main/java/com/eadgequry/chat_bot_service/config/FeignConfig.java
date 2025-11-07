package com.eadgequry.chat_bot_service.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "com.eadgequry.chat_bot_service.client")
public class FeignConfig {
}
