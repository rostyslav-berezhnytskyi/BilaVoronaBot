package com.telegram.bilavorona.controler;

import com.telegram.bilavorona.config.BotConfig;
import com.telegram.bilavorona.config.MyBotSender;
import com.telegram.bilavorona.model.FileEntity;
import com.telegram.bilavorona.model.FileGroup;
import com.telegram.bilavorona.model.Role;
import com.telegram.bilavorona.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
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

        // Send message to choose file group
        sendGroupSelectionButtons(chatId);
    }

    @Override
    public void assignFileGroup(CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();
        Long chatId = callbackQuery.getMessage().getChatId();
        String username = callbackQuery.getFrom().getUserName();

        // Extract file group from callback data
        String groupName = data.replace("file_group_", "");
        FileGroup selectedGroup = FileGroup.valueOf(groupName);

        // Get the latest file uploaded by this user (assuming we track it)
        FileEntity file = fileService.getLastUploadedFileByUser(chatId);
        if (file != null) {
            file.setFileGroup(selectedGroup);
            fileService.updateFile(file);
            botSender.sendMessage(chatId, "✅ Файл віднесено до групи: " + selectedGroup.getDisplayName());
        } else {
            botSender.sendMessage(chatId, "❌ Не вдалося знайти файл для оновлення групи.");
        }
    }

    @Override
    public void sendFilesByGroup(Long chatId, FileGroup group) {
        List<FileEntity> files = fileService.getFilesByGroup(group);

        if (files.isEmpty()) {
            botSender.sendMessage(chatId, "❌ Немає файлів у цій категорії.");
            return;
        }

        files.forEach(file -> sendFile(chatId, file));  // Використовуємо універсальний метод
    }

    @Override
    public void changeFileGroupById(Message msg, String id, String newGroup) {
        if (!roleController.checkRole(msg.getChatId(), new Role[]{Role.OWNER, Role.ADMIN})) return;
        try {
            Long fileId = Long.parseLong(id);
            FileGroup group = FileGroup.valueOf(newGroup.toUpperCase());
            if (fileService.changeFileGroupById(fileId, group)) {
                botSender.sendMessage(msg.getChatId(), "✅ Група файлу з ID " + fileId + " змінена на " + newGroup + ".");
            } else {
                botSender.sendMessage(msg.getChatId(), "❌ Файл з ID " + fileId + " не знайдено.");
            }
        } catch (NumberFormatException e) {
            botSender.sendMessage(msg.getChatId(), "❌ Невірний формат ID. Приклад: /change_file_group_by_id 123 DOCUMENTATION");
        } catch (IllegalArgumentException e) {
            botSender.sendMessage(msg.getChatId(), "❌ Невірна група файлів. Доступні групи: " + FileGroup.values());
        }
    }

    @Override
    public void changeFileGroupByName(Message msg, String fileName, String newGroup) {
        if (!roleController.checkRole(msg.getChatId(), new Role[]{Role.OWNER, Role.ADMIN})) return;
        try {
            FileGroup group = FileGroup.valueOf(newGroup.toUpperCase());
            if (fileService.changeFileGroupByName(fileName, group)) {
                botSender.sendMessage(msg.getChatId(), "✅ Група файлу '" + fileName + "' змінена на " + newGroup + ".");
            } else {
                botSender.sendMessage(msg.getChatId(), "❌ Файл з ім'ям '" + fileName + "' не знайдено.");
            }
        } catch (IllegalArgumentException e) {
            botSender.sendMessage(msg.getChatId(), "❌ Невірна група файлів. Доступні групи: " + FileGroup.values());
        }
    }

    @Override
    public void changeFileNameById(Message msg, String id, String newFileName) {
        if (!roleController.checkRole(msg.getChatId(), new Role[]{Role.OWNER, Role.ADMIN})) return;
        try {
            Long fileId = Long.parseLong(id);
            if (fileService.changeFileNameById(fileId, newFileName)) {
                botSender.sendMessage(msg.getChatId(), "✅ Назву файлу з ID " + fileId + " змінено на '" + newFileName + "'.");
            } else {
                botSender.sendMessage(msg.getChatId(), "❌ Файл з ID " + fileId + " не знайдено.");
            }
        } catch (NumberFormatException e) {
            botSender.sendMessage(msg.getChatId(), "❌ Невірний формат ID. Приклад: /change_file_name_by_id 123 new_name.docx");
        }
    }

    @Override
    public void changeFileNameByName(Message msg, String currentFileName, String newFileName) {
        if (!roleController.checkRole(msg.getChatId(), new Role[]{Role.OWNER, Role.ADMIN})) return;
        if (fileService.changeFileNameByName(currentFileName, newFileName)) {
            botSender.sendMessage(msg.getChatId(), "✅ Назву файлу '" + currentFileName + "' змінено на '" + newFileName + "'.");
        } else {
            botSender.sendMessage(msg.getChatId(), "❌ Файл з ім'ям '" + currentFileName + "' не знайдено.");
        }
    }

    @Override
    public void deleteFileById(Message msg, String id) {
        if (!roleController.checkRole(msg.getChatId(), new Role[]{Role.OWNER, Role.ADMIN})) return;
        try {
            Long fileId = Long.parseLong(id);
            if (fileService.deleteFileById(fileId)) {
                botSender.sendMessage(msg.getChatId(), "✅ Файл з ID " + fileId + " успішно видалено.");
            } else {
                botSender.sendMessage(msg.getChatId(), "❌ Файл з ID " + fileId + " не знайдено.");
            }
        } catch (NumberFormatException e) {
            botSender.sendMessage(msg.getChatId(), "❌ Невірний формат ID. Приклад: /delete_file_by_id 123");
        }
    }

    @Override
    public void deleteFileByName(Message msg, String fileName) {
        if (!roleController.checkRole(msg.getChatId(), new Role[]{Role.OWNER, Role.ADMIN})) return;
        if (fileService.deleteFileByName(fileName)) {
            botSender.sendMessage(msg.getChatId(), "✅ Файл з ім'ям '" + fileName + "' успішно видалено.");
        } else {
            botSender.sendMessage(msg.getChatId(), "❌ Файл з ім'ям '" + fileName + "' не знайдено.");
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
            fileService.saveFile(fileName, mimeType, fileSize, fileBytes, uploadedBy, LocalDateTime.now());

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
            botSender.sendMessage(chatId, "📂 Немає збережених файлів.");
            return;
        }

        files.forEach(file -> sendFile(chatId, file));  // Використовуємо універсальний метод
    }

    private void sendFile(Long chatId, FileEntity file) {
        String mimeType = file.getFileType(); // Наприклад, image/jpg, video/mp4, application/pdf
        InputStream fileStream = new ByteArrayInputStream(file.getFileData());
        InputFile inputFile = new InputFile(fileStream, file.getFileName());

        try {
            if (mimeType.startsWith("image/")) {  // Якщо файл є зображенням
                SendPhoto sendPhoto = new SendPhoto();
                sendPhoto.setChatId(chatId);
                sendPhoto.setPhoto(inputFile);
                sendPhoto.setCaption(file.getFileName() + " (" + (file.getFileSize() / 1024) + " KB) ID: " + file.getId());
                botSender.execute(sendPhoto);
            } else if (mimeType.startsWith("video/")) {  // Якщо файл є відео
                SendVideo sendVideo = new SendVideo();
                sendVideo.setChatId(chatId);
                sendVideo.setVideo(inputFile);
                sendVideo.setCaption(file.getFileName() + " (" + (file.getFileSize() / 1024) + " KB) ID: " + file.getId());
                botSender.execute(sendVideo);
            } else {  // Всі інші файли відправляємо як документи
                SendDocument sendDocument = new SendDocument();
                sendDocument.setChatId(chatId);
                sendDocument.setDocument(inputFile);
                sendDocument.setCaption(file.getFileName() + " (" + (file.getFileSize() / 1024) + " KB) ID: " + file.getId());
                botSender.execute(sendDocument);
            }
        } catch (TelegramApiException e) {
            log.error("❌ Помилка надсилання файлу: {}", e.getMessage());
            botSender.sendMessage(chatId, "❌ Не вдалося надіслати файл: " + file.getFileName());
        }
    }

    private void sendGroupSelectionButtons(Long chatId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (FileGroup group : FileGroup.values()) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(group.getDisplayName());
            button.setCallbackData("file_group_" + group.name());
            rows.add(Collections.singletonList(button));
        }
        markup.setKeyboard(rows);

        botSender.sendInlineKeyboardMarkupMessage(chatId, "До якої групи ви хочете віднести цей файл?", markup);
    }
}
