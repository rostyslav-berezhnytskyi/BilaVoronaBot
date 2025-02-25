package com.telegram.bilavorona.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.bots.DefaultBotOptions;

@Slf4j
@Component
public class MyBotSender extends DefaultAbsSender {
    private final String botToken;

    @Autowired
    public MyBotSender(BotConfig config) {
        super(new DefaultBotOptions()); // DefaultBotOptions is still used here
        this.botToken = config.getToken();
    }

    @Override
    public String getBotToken() {
        return botToken;
    }
}
