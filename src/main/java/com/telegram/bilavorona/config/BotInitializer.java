package com.telegram.bilavorona.config;

import com.telegram.bilavorona.controler.TelegramBot;
import com.telegram.bilavorona.manager_bot.TelegramManagerBot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Slf4j
@Component
public class BotInitializer {
    private final TelegramBot bot;
    private final TelegramManagerBot botManager;
    private final TelegramBotsApi telegramBotsApi;

    @Autowired
    public BotInitializer(TelegramBot bot, TelegramManagerBot botManager) throws TelegramApiException{
        this.bot = bot;
        this.botManager = botManager;
        this.telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        registerBot();
    }

    public void registerBot() {
        try {
            telegramBotsApi.registerBot(bot);
            telegramBotsApi.registerBot(botManager);
            log.info("Bot successfully registered!");
        } catch (TelegramApiException e) {
            log.error("Failed to register bot: {}", e.getMessage());
        }
    }
}
