package com.telegram.bilavorona.bila_vorona_manager;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Data
@Configuration
@EnableScheduling
public class BotManagerConfig {
    @Value("${manager.bot.name}")
    private String botUserName;

    @Value("${manager.bot.token}")
    private String token;
}
