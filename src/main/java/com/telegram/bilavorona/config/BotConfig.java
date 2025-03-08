package com.telegram.bilavorona.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

@Data
@Configuration
@EnableScheduling
public class BotConfig {
    @Value("${bot.name}")
    private String botUserName;

    @Value("${bot.token}")
    private String token;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
