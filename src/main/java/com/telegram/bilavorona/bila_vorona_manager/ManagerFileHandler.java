package com.telegram.bilavorona.bila_vorona_manager;

import com.telegram.bilavorona.config.BotConfig;
import com.telegram.bilavorona.model.FileEntity;
import com.telegram.bilavorona.service.FileService;
import com.telegram.bilavorona.util.ButtonsSender;
import com.telegram.bilavorona.util.CommandValidator;
import com.telegram.bilavorona.util.MyBotSender;
import com.telegram.bilavorona.util.RoleValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDateTime;

@Slf4j
@Component
public class ManagerFileHandler {
    private final FileService fileService;
    private final BotConfig botConfig;
    private final ManagerBotSender managerBotSender;

    @Autowired
    public ManagerFileHandler(FileService fileService, BotConfig botConfig, ManagerBotSender managerBotSender) {
        this.fileService = fileService;
        this.botConfig = botConfig;
        this.managerBotSender = managerBotSender;
    }

    public void saveFile(Message msg) {
        Long chatId = msg.getChatId();

        if (msg.hasDocument()) {
            saveDocument(msg);
        } else if (msg.hasPhoto()) {
            saveImage(msg);
        } else if (msg.hasVideo()) {
            saveVideo(msg);
        }
    }

    private void saveDocument(Message msg) {
        Document doc = msg.getDocument();
        String fileId = doc.getFileId();
        String fileName = doc.getFileName();
        Long fileSize = doc.getFileSize();
        String mimeType = doc.getMimeType();
        Long uploadedBy = msg.getFrom().getId();

        saveAndStoreFile(fileId, fileName, fileSize, mimeType, uploadedBy);
    }

    private void saveImage(Message msg) {
        // Get the highest quality photo
        PhotoSize photo = msg.getPhoto().get(msg.getPhoto().size() - 1);
        String fileId = photo.getFileId();
        String fileName = "image_TEMP" + msg.getFrom().getUserName() + "_" + System.currentTimeMillis() + ".jpg";
        String mimeType = "image/jpeg"; // Default mime type for images
        Long fileSize = (long) photo.getFileSize();
        Long uploadedBy = msg.getFrom().getId();

        saveAndStoreFile(fileId, fileName, fileSize, mimeType, uploadedBy);
    }

    // Save video (e.g., MP4)
    private void saveVideo(Message msg) {
        Video video = msg.getVideo();
        String fileId = video.getFileId();
        String fileName = "video_TEMP" + msg.getFrom().getUserName() + "_" + System.currentTimeMillis() + ".mp4";
        String mimeType = "video/mp4"; // Default mime type for video
        Long fileSize = video.getFileSize();
        Long uploadedBy = msg.getFrom().getId();

        saveAndStoreFile(fileId, fileName, fileSize, mimeType, uploadedBy);
    }

    private void saveAndStoreFile(String fileId, String fileName, Long fileSize, String mimeType, Long uploadedBy) {
        try {
            // Get file path from Telegram API
            GetFile getFile = new GetFile();
            getFile.setFileId(fileId);
            File file = managerBotSender.execute(getFile);

            // Download file content
            byte[] fileBytes = downloadFile(file.getFilePath());

            // Save the file to the database via the file service
            fileService.saveFile(fileName, mimeType, fileSize, fileBytes, uploadedBy, LocalDateTime.now());

        } catch (TelegramApiException | IOException e) {
            log.error("Помилка завантаження файлу: {}", e.getMessage());
        }
    }

    // Download file from Telegram API using filePath
    private byte[] downloadFile(String filePath) throws IOException {
        // Construct the URL to download the file
        URL fileUrl = new URL("https://api.telegram.org/file/bot" + botConfig.getToken() + "/" + filePath);

        // Open connection to the URL and read the bytes
        try (InputStream inputStream = fileUrl.openStream()) {
            return inputStream.readAllBytes();
        }
    }

    public void sendFile(Long chatId, FileEntity file) {
        String mimeType = file.getFileType(); // Наприклад, image/jpg, video/mp4, application/pdf
        InputStream fileStream = new ByteArrayInputStream(file.getFileData());
        InputFile inputFile = new InputFile(fileStream, file.getFileName());

        try {
            if (mimeType.startsWith("image/")) {  // Якщо файл є зображенням
                SendPhoto sendPhoto = new SendPhoto();
                sendPhoto.setChatId(chatId);
                sendPhoto.setPhoto(inputFile);
                sendPhoto.setCaption(file.getFileName() + " (" + (file.getFileSize() / 1024) + " KB) ID: " + file.getId());
                managerBotSender.execute(sendPhoto);
            } else if (mimeType.startsWith("video/")) {  // Якщо файл є відео
                SendVideo sendVideo = new SendVideo();
                sendVideo.setChatId(chatId);
                sendVideo.setVideo(inputFile);
                sendVideo.setCaption(file.getFileName() + " (" + (file.getFileSize() / 1024) + " KB) ID: " + file.getId());
                managerBotSender.execute(sendVideo);
            } else {  // Всі інші файли відправляємо як документи
                SendDocument sendDocument = new SendDocument();
                sendDocument.setChatId(chatId);
                sendDocument.setDocument(inputFile);
                sendDocument.setCaption(file.getFileName() + " (" + (file.getFileSize() / 1024) + " KB) ID: " + file.getId());
                managerBotSender.execute(sendDocument);
            }
        } catch (TelegramApiException e) {
            log.error("❌ Помилка надсилання файлу: {}", e.getMessage());
        }
    }
}
