package com.telegram.bilavorona.controler;

import com.telegram.bilavorona.config.BotConfig;
import com.telegram.bilavorona.config.MyBotSender;
import com.telegram.bilavorona.model.FileEntity;
import com.telegram.bilavorona.model.Role;
import com.telegram.bilavorona.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

@Service
@Slf4j
public class FileHandlerImpl implements FileHandler {
    private final FileService fileService;
    private final MyBotSender botSender;
    private final BotConfig botConfig;
    private final RoleController roleController;

    @Autowired
    public FileHandlerImpl(FileService fileService, MyBotSender botSender, BotConfig botConfig, RoleController roleController) {
        this.fileService = fileService;
        this.botSender = botSender;
        this.botConfig = botConfig;
        this.roleController = roleController;
    }


    @Override
    public void saveFile(Message msg) {
        Long chatId = msg.getChatId();
        log.info("Called the command to save file in chatId = {}", chatId);
        if (!roleController.checkRole(msg.getChatId(), new Role[]{Role.OWNER, Role.ADMIN})) return;

        if (msg.hasDocument()) {
            saveDocument(msg);
        } else if (msg.hasPhoto()) {
            saveImage(msg);
        } else if (msg.hasVideo()) {
            saveVideo(msg);
        } else {
            botSender.sendMessage(msg.getChatId(), "❌ Невідомий тип файлу!");
        }
    }

    // Save document (e.g., PDF, Word, etc.)
    private void saveDocument(Message msg) {
        Document doc = msg.getDocument();
        String fileId = doc.getFileId();
        String fileName = doc.getFileName();
        Long fileSize = doc.getFileSize();
        String mimeType = doc.getMimeType();
        Long uploadedBy = msg.getFrom().getId();

        saveAndStoreFile(fileId, fileName, fileSize, mimeType, uploadedBy);
        log.info("Successfully saved document file to the database in chatId = {}", uploadedBy);
    }

    // Save image (e.g., PNG, JPG)
    private void saveImage(Message msg) {
        // Get the highest quality photo
        PhotoSize photo = msg.getPhoto().get(msg.getPhoto().size() - 1);
        String fileId = photo.getFileId();
        String fileName = "image.jpg";  // Default name
        String mimeType = "image/jpeg"; // Default mime type for images
        Long fileSize = (long) photo.getFileSize();
        Long uploadedBy = msg.getFrom().getId();

        saveAndStoreFile(fileId, fileName, fileSize, mimeType, uploadedBy);
        log.info("Successfully saved image to the database in chatId = {}", uploadedBy);
    }

    // Save video (e.g., MP4)
    private void saveVideo(Message msg) {
        Video video = msg.getVideo();
        String fileId = video.getFileId();
        String fileName = "video.mp4";  // Default name for video
        String mimeType = "video/mp4"; // Default mime type for video
        Long fileSize = video.getFileSize();
        Long uploadedBy = msg.getFrom().getId();

        saveAndStoreFile(fileId, fileName, fileSize, mimeType, uploadedBy);
        log.info("Successfully saved vide to the database in chatId = {}", uploadedBy);
    }

    private void saveAndStoreFile(String fileId, String fileName, Long fileSize, String mimeType, Long uploadedBy) {
        try {
            // Get file path from Telegram API
            GetFile getFile = new GetFile();
            getFile.setFileId(fileId);
            File file = botSender.execute(getFile);

            // Download file content
            byte[] fileBytes = downloadFile(file.getFilePath());

            // Save the file to the database via the file service
            fileService.saveFile(fileName, mimeType, fileSize, fileBytes, uploadedBy);

            botSender.sendMessage(uploadedBy, "✅ Файл успішно збережено у базі даних!");
        } catch (TelegramApiException | IOException e) {
            log.error("Помилка завантаження файлу: {}", e.getMessage());
            botSender.sendMessage(uploadedBy, "❌ Не вдалося зберегти файл.");
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

    @Override
    public void getAllFiles(Message msg) {
        Long chatId = msg.getChatId();
        log.info("Called the command to get all files from DB in chatId = {}", chatId);
        List<FileEntity> files = fileService.getAllFiles();

        if (files.isEmpty()) {
            botSender.sendMessage(msg.getChatId(), "📂 Немає збережених файлів.");
            return;
        }

        for (FileEntity file : files) {
            InputFile inputFile = new InputFile(new ByteArrayInputStream(file.getFileData()), file.getFileName());
            SendDocument sendDocument = new SendDocument();
            sendDocument.setChatId(msg.getChatId());
            sendDocument.setDocument(inputFile);
            sendDocument.setCaption("📜 " + file.getFileName() + " (" + (file.getFileSize() / 1024) + " KB)");

            try {
                botSender.execute(sendDocument);
            } catch (TelegramApiException e) {
                log.error("❌ Помилка надсилання файлу: {}", e.getMessage());
                botSender.sendMessage(msg.getChatId(), "❌ Не вдалося надіслати файл: " + file.getFileName());
            }
        }
    }
}
