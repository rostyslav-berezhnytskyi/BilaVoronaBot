package com.telegram.bilavorona.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

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

    public void sendMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        executeMessage(message);
    }

    public void sendInlineKeyboardMarkupMessage(Long chatId, String text, InlineKeyboardMarkup markup) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        message.setReplyMarkup(markup);

        executeMessage(message);
    }

    private void executeMessage(BotApiMethod<?> message) {
        try {
            this.execute(message);
        } catch (TelegramApiException e) {
            log.error("Failed to execute message: {}", e.getMessage());
        }
    }
}
