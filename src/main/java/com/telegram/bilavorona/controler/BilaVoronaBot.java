package com.telegram.bilavorona.controler;

import com.telegram.bilavorona.config.BotConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Component
public class BilaVoronaBot extends TelegramLongPollingBot {
    private final BotConfig config;

    @Autowired
    public BilaVoronaBot(BotConfig config) {
        this.config = config;
    }


    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            switch (messageText) {
                case "/start" -> showStart(chatId);
                case "/help" -> sendMessage(chatId, "Here are the available commands:\n/start - Start the bot\n/help - Get help");
                default -> sendMessage(chatId, "Unknown command. Use /help to see available commands.");
            }
        }
    }

    private void showStart(long chatId) {
        sendMessage(chatId, "Welcome to BilaVorona Bot! Use /help for more info.");
    }

    private void sendMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Failed to send message: {}", e.getMessage());
        }
    }


    @Override
    public String getBotUsername() {
        return config.getBotUserName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }
}
