package com.eadgequry.chat_bot_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
@EnableConfigurationProperties
public class ChatBotServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChatBotServiceApplication.class, args);
	}

}
