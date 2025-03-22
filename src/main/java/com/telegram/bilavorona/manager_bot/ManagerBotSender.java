package com.telegram.bilavorona.manager_bot;

import com.telegram.bilavorona.service.TelegramFileService;
import com.telegram.bilavorona.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Optional;

@Slf4j
@Service
public class ManagerBotSender extends DefaultAbsSender {
    private final String managerBotToken;
    private final UserService userService;
    private final TelegramFileService telegramFileService;

    @Autowired
    public ManagerBotSender(BotManagerConfig botManagerConfig, UserService userService, TelegramFileService telegramFileService) {
        super(new DefaultBotOptions());
        this.managerBotToken = botManagerConfig.getToken();
        this.userService = userService;
        this.telegramFileService = telegramFileService;
    }

    @Override
    public String getBotToken() {
        return managerBotToken;
    }

    public void sendMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
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

    private void executeMessage(BotApiMethod<?> message) {
        try {
            this.execute(message);
        } catch (TelegramApiException e) {
            log.error("Failed to execute message: {}", e.getMessage());
        }
    }

    // 📌 Determine file type and call respective send method
    public void sendFileToManager(Long chatId, Message msg) {
        if (msg.hasPhoto()) {
            sendPhotoToManager(chatId, msg);
        } else if (msg.hasDocument()) {
            sendDocumentToManager(chatId, msg);
        } else if (msg.hasVideo()) {
            sendVideoToManager(chatId, msg);
        }
    }

    // 📦 Send photo using TelegramFileService
    private void sendPhotoToManager(Long chatId, Message msg) {
        String fileId = msg.getPhoto().get(msg.getPhoto().size() - 1).getFileId();
        sendMedia(chatId, fileId, msg.getCaption(), "photo");
    }

    // 📄 Send document using TelegramFileService
    private void sendDocumentToManager(Long chatId, Message msg) {
        String fileId = msg.getDocument().getFileId();
        sendMedia(chatId, fileId, msg.getCaption(), "document");
    }

    // 🎥 Send video using TelegramFileService
    private void sendVideoToManager(Long chatId, Message msg) {
        String fileId = msg.getVideo().getFileId();
        sendMedia(chatId, fileId, msg.getCaption(), "video");
    }

    // 🌟 Reusable method to send any media type
    private void sendMedia(Long chatId, String fileId, String caption, String fileType) {
        InputFile inputFile = telegramFileService.downloadFile(fileId);
        if (inputFile != null) {
            try {
                switch (fileType) {
                    case "photo" -> {
                        SendPhoto sendPhoto = new SendPhoto(chatId.toString(), inputFile);
                        sendPhoto.setCaption(Optional.ofNullable(caption).orElse(""));
                        execute(sendPhoto);
                    }
                    case "document" -> {
                        SendDocument sendDocument = new SendDocument(chatId.toString(), inputFile);
                        sendDocument.setCaption(Optional.ofNullable(caption).orElse(""));
                        execute(sendDocument);
                    }
                    case "video" -> {
                        SendVideo sendVideo = new SendVideo(chatId.toString(), inputFile);
                        sendVideo.setCaption(Optional.ofNullable(caption).orElse(""));
                        execute(sendVideo);
                    }
                }
            } catch (TelegramApiException e) {
                log.error("Failed to send {}: {}", fileType, e.getMessage());
            } finally {
                telegramFileService.deleteTempFile(inputFile);
            }
        } else {
            log.error("Failed to download file for sending as {}", fileType);
        }
    }


}
