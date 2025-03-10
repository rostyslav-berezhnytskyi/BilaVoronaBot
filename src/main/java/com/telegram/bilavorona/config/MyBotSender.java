package com.telegram.bilavorona.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
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

    public void sendKeyboardMarkupMessage(Long chatId, String text, ReplyKeyboardMarkup keyboardMarkup) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        message.setReplyMarkup(keyboardMarkup);

        executeMessage(message);
    }

    public void sendInlineKeyboardMarkupMessage(Long chatId, String text, InlineKeyboardMarkup markup) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        message.setReplyMarkup(markup);

        executeMessage(message);
    }

    public void sendDocument(Long chatId, String fileId, String caption) {
        SendDocument document = new SendDocument();
        document.setChatId(chatId);
        document.setDocument(new InputFile(fileId));
        document.setCaption(caption);
        executeDocument(document);
    }

    public void sendPhoto(Long chatId, String fileId, String caption) {
        SendPhoto photo = new SendPhoto();
        photo.setChatId(chatId);
        photo.setPhoto(new InputFile(fileId));
        photo.setCaption(caption);
        executePhoto(photo);
    }

    public void sendVideo(Long chatId, String fileId, String caption) {
        SendVideo video = new SendVideo();
        video.setChatId(chatId);
        video.setVideo(new InputFile(fileId));
        video.setCaption(caption);
        executeVideo(video);
    }

    private void executeDocument(SendDocument document) {
        try {
            this.execute(document);
        } catch (TelegramApiException e) {
            log.error("Failed to send document: {}", e.getMessage());
        }
    }

    private void executePhoto(SendPhoto photo) {
        try {
            this.execute(photo);
        } catch (TelegramApiException e) {
            log.error("Failed to send photo: {}", e.getMessage());
        }
    }

    private void executeVideo(SendVideo video) {
        try {
            this.execute(video);
        } catch (TelegramApiException e) {
            log.error("Failed to send video: {}", e.getMessage());
        }
    }

    private void executeMessage(BotApiMethod<?> message) {
        try {
            this.execute(message);
        } catch (TelegramApiException e) {
            log.error("Failed to execute message: {}", e.getMessage());
        }
    }
}
