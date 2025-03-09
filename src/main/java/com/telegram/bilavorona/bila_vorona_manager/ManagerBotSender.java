package com.telegram.bilavorona.bila_vorona_manager;

import com.telegram.bilavorona.config.BotConfig;
import com.telegram.bilavorona.model.FileEntity;
import com.telegram.bilavorona.model.User;
import com.telegram.bilavorona.service.FileService;
import com.telegram.bilavorona.service.TelegramFileService;
import com.telegram.bilavorona.service.UserService;
import com.telegram.bilavorona.util.MyBotSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class ManagerBotSender extends DefaultAbsSender {
    private final String managerBotToken;
    private final UserService userService;
    private final MyBotSender botSender;
    private final TelegramFileService telegramFileService;

    @Autowired
    public ManagerBotSender(BotManagerConfig botManagerConfig, UserService userService, MyBotSender botSender, TelegramFileService telegramFileService) {
        super(new DefaultBotOptions());
        this.managerBotToken = botManagerConfig.getToken();
        this.userService = userService;
        this.botSender = botSender;
        this.telegramFileService = telegramFileService;
    }

    @Override
    public String getBotToken() {
        return managerBotToken;
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

    public boolean sendAll(Long chatId, Message msg) {
        if (msg.hasText()) {
            sendMessage(chatId, msg.getText());
            return true;
        } else if (msg.hasVideo()) {
            msg.getVideo().getFileId();
            sendVideo(chatId, msg.getVideo().getFileId(), msg.getCaption());
            return true;
        } else if (msg.hasPhoto()) {
            String fileId = msg.getPhoto().get(msg.getPhoto().size() - 1).getFileId();
            sendPhoto(chatId, fileId, msg.getCaption());
            return true;
        } else if (msg.hasDocument()) {
            sendDocument(chatId, msg.getDocument().getFileId(), msg.getCaption());
            return true;
        } else {
            return false;
        }
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

    public void sendFileToManager(Long chatId, Message msg) {
        if (msg.hasPhoto()) {
            sendPhotoToManager(chatId, msg);
        } else if (msg.hasDocument()) {
            sendDocumentToManager(chatId, msg);
        } else if (msg.hasVideo()) {
            sendVideoToManager(chatId, msg);
        }
    }

    private void sendPhotoToManager(Long chatId, Message msg) {
        String fileId = msg.getPhoto().get(msg.getPhoto().size() - 1).getFileId();
        downloadAndUploadFile(chatId, fileId, msg.getCaption(),"photo");
    }

    private void sendDocumentToManager(Long chatId, Message msg) {
        String fileId = msg.getDocument().getFileId();
        downloadAndUploadFile(chatId, fileId, msg.getCaption(),"document");
    }

    private void sendVideoToManager(Long chatId, Message msg) {
        String fileId = msg.getVideo().getFileId();
        downloadAndUploadFile(chatId, fileId, msg.getCaption(),"video");
    }

    private void downloadAndUploadFile(Long chatId, String fileId, String caption, String fileType) {
        try {
            GetFile getFile = new GetFile(fileId);
            File file = botSender.execute(getFile);

            if (file != null && file.getFilePath() != null) {
                URL url = new URL("https://api.telegram.org/file/bot" + botSender.getBotToken() + "/" + file.getFilePath());

                String originalFileName = file.getFilePath().substring(file.getFilePath().lastIndexOf('/') + 1);
                String uniqueFileName = UUID.randomUUID() + "_" + originalFileName;
                Path downloadPath = Paths.get("temp", uniqueFileName);

                Files.createDirectories(Paths.get("temp"));
                Files.copy(url.openStream(), downloadPath);

                InputFile inputFile = new InputFile(downloadPath.toFile());

                if (fileType.equals("photo")) {
                    SendPhoto sendPhoto = new SendPhoto(String.valueOf(chatId), inputFile);
                    sendPhoto.setCaption(caption);
                    execute(sendPhoto);
                } else if (fileType.equals("document")) {
                    SendDocument sendDocument = new SendDocument(String.valueOf(chatId), inputFile);
                    sendDocument.setCaption(caption);
                    execute(sendDocument);
                } else if (fileType.equals("video")) {
                    SendVideo sendVideo = new SendVideo(String.valueOf(chatId), inputFile);
                    sendVideo.setCaption(caption);
                    execute(sendVideo);
                }

                Files.delete(downloadPath);
            } else {
                log.error("File or file path is null");
            }

        } catch (TelegramApiException | MalformedURLException e) {
            log.error("Error downloading or uploading file: {}", e.getMessage());
        } catch (IOException e) {
            log.error("Error with IO operation {}", e.getMessage());
        } catch (Exception e) {
            log.error("An unexpected error occurred: {}", e.getMessage());
        }
    }

    public void sendMessageToManager(String userInfo, Message msg) {
        List<User> admins = userService.findAllAdmins();  // Fetch managers from DB
        String textOfMessage;
        if(msg.hasText()) {
            textOfMessage = userInfo + "\uD83D\uDCE9 *Надіслане повідомлення:* \n" + msg.getText();
            for (User admin : admins) {
                sendMessage(admin.getChatId(), textOfMessage);
                log.info("Message sent to manager (admin={})", admin.getChatId());
            }
        } else {
            textOfMessage = userInfo + "\uD83D\uDCE9 *Надісланий файл:*";
            for (User admin : admins) {
                sendMessage(admin.getChatId(), textOfMessage);
                sendFileToManager(admin.getChatId(), msg);
                log.info("Message sent to manager (admin={})", admin.getChatId());
            }
        }

    }


}
