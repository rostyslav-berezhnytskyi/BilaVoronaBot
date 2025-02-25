package com.telegram.bilavorona.config;

import com.telegram.bilavorona.controler.BilaVoronaBot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Slf4j
@Component
public class BotInitializer {
    private final BilaVoronaBot bot;
    private final TelegramBotsApi telegramBotsApi;

    @Autowired
    public BotInitializer(BilaVoronaBot bot) throws TelegramApiException{
        this.bot = bot;
        this.telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        registerBot();
    }

    public void registerBot() {
        try {
            telegramBotsApi.registerBot(bot);
            log.info("Bot successfully registered!");
        } catch (TelegramApiException e) {
            log.error("Failed to register bot: {}", e.getMessage());
        }
    }
}
