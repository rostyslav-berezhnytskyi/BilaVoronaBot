package com.telegram.bilavorona.handler;

import com.telegram.bilavorona.config.BotConfig;
import com.telegram.bilavorona.util.ButtonsSender;
import com.telegram.bilavorona.util.CommandValidator;
import com.telegram.bilavorona.util.MyBotSender;
import com.telegram.bilavorona.model.FileEntity;
import com.telegram.bilavorona.model.FileGroup;
import com.telegram.bilavorona.service.FileService;
import com.telegram.bilavorona.util.RoleValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
import java.util.List;

@Service
@Slf4j
public class FileHandlerImpl implements FileHandler {
    private final FileService fileService;
    private final MyBotSender botSender;
    private final BotConfig botConfig;
    private final RoleValidator roleValidator;
    private final CommandValidator comValidator;
    private final ButtonsSender buttonsSender;

    @Autowired
    public FileHandlerImpl(FileService fileService, MyBotSender botSender, BotConfig botConfig, RoleValidator roleValidator, CommandValidator comValidator, ButtonsSender buttonsSender) {
        this.fileService = fileService;
        this.botSender = botSender;
        this.botConfig = botConfig;
        this.roleValidator = roleValidator;
        this.comValidator = comValidator;
        this.buttonsSender = buttonsSender;
    }

    @Override
    public void saveFile(Message msg) {
        Long chatId = msg.getChatId();
        log.info("Called the command to save file in chatId = {}", chatId);
        if (!roleValidator.checkRoleOwnerOrAdmin(chatId)) return;

        if (msg.hasDocument()) {
            saveDocument(msg);
        } else if (msg.hasPhoto()) {
            saveImage(msg);
        } else if (msg.hasVideo()) {
            saveVideo(msg);
        } else {
            botSender.sendMessage(msg.getChatId(), "❌ Невідомий тип файлу!");
        }
        buttonsSender.sendGroupSelectionButtons(chatId);
    }

    @Override
    public void assignFileGroup(CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();
        Long chatId = callbackQuery.getMessage().getChatId();

        // Extract file group from callback data
        String groupName = data.split(":")[1];
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

        files.forEach(file -> sendFile(chatId, file));
    }

    @Override
    public void changeFileGroupById(long chatId, String[] commandParts) {
        if (!roleValidator.checkRoleOwnerOrAdmin(chatId)) return;
        if (!comValidator.checkCom(chatId, commandParts, 3,
                "Будь ласка вкажіть id файлу та групу. Приклад: /change_file_group_by_id 123 DOCUMENTATION")) return;

        String newGroup = commandParts[2];
        try {
            Long fileId = Long.parseLong(commandParts[1]);
            FileGroup group = FileGroup.valueOf(newGroup.toUpperCase());
            if (fileService.changeFileGroupById(fileId, group)) {
                botSender.sendMessage(chatId, "✅ Група файлу з ID " + fileId + " змінена на " + newGroup + ".");
            } else {
                botSender.sendMessage(chatId, "❌ Файл з ID " + fileId + " не знайдено.");
            }
        } catch (NumberFormatException e) {
            botSender.sendMessage(chatId, "❌ Невірний формат ID. Приклад: /change_file_group_by_id 123 DOCUMENTATION");
        } catch (IllegalArgumentException e) {
            botSender.sendMessage(chatId, "❌ Невірна група файлів. Доступні групи: " + FileGroup.values());
        }
    }

    @Override
    public void changeFileGroupByName(long chatId, String[] commandParts) {
        if (!roleValidator.checkRoleOwnerOrAdmin(chatId)) return;
        if (!comValidator.checkCom(chatId, commandParts, 3,
                "Будь ласка вкажіть ім'я файлу та групу. Приклад: /change_file_group_by_name file_name.docx EXAMPLES")) return;

        String fileName = commandParts[1];
        String newGroup = commandParts[2];
        try {
            FileGroup group = FileGroup.valueOf(newGroup.toUpperCase());
            if (fileService.changeFileGroupByName(fileName, group)) {
                botSender.sendMessage(chatId, "✅ Група файлу '" + fileName + "' змінена на " + newGroup + ".");
            } else {
                botSender.sendMessage(chatId, "❌ Файл з ім'ям '" + fileName + "' не знайдено.");
            }
        } catch (IllegalArgumentException e) {
            botSender.sendMessage(chatId, "❌ Невірна група файлів. Доступні групи: " + FileGroup.values());
        }
    }

    @Override
    public void changeFileNameById(long chatId, String[] commandParts) {
        if (!roleValidator.checkRoleOwnerOrAdmin(chatId)) return;
        if (!comValidator.checkCom(chatId, commandParts, 3,
                "Будь ласка вкажіть id файлу та нове ім'я файлу. Приклад: /change_file_name_by_id 123 new_name.docx")) return;

        String id = commandParts[1];
        String newFileName = commandParts[2];
        try {
            Long fileId = Long.parseLong(id);
            if (fileService.changeFileNameById(fileId, newFileName)) {
                botSender.sendMessage(chatId, "✅ Назву файлу з ID " + fileId + " змінено на '" + newFileName + "'.");
            } else {
                botSender.sendMessage(chatId, "❌ Файл з ID " + fileId + " не знайдено.");
            }
        } catch (NumberFormatException e) {
            botSender.sendMessage(chatId, "❌ Невірний формат ID. Приклад: /change_file_name_by_id 123 new_name.docx");
        }
    }

    @Override
    public void changeFileNameByName(long chatId, String[] commandParts) {
        if (!roleValidator.checkRoleOwnerOrAdmin(chatId)) return;
        if (!comValidator.checkCom(chatId, commandParts, 3,
                "Будь ласка вкажіть старе ім'я файлу та нове ім'я файлу. Приклад: /change_file_name_by_name old_name.docx new_name.docx")) return;

        String currentFileName = commandParts[1];
        String newFileName = commandParts[2];
        if (fileService.changeFileNameByName(currentFileName, newFileName)) {
            botSender.sendMessage(chatId, "✅ Назву файлу '" + currentFileName + "' змінено на '" + newFileName + "'.");
        } else {
            botSender.sendMessage(chatId, "❌ Файл з ім'ям '" + currentFileName + "' не знайдено.");
        }
    }

    @Override
    public void deleteFileById(long chatId, String[] commandParts) {
        if (!roleValidator.checkRoleOwnerOrAdmin(chatId)) return;
        if (!comValidator.checkCom(chatId, commandParts, 2,
                "Будь ласка вкажіть id файлу для видалення. Приклад: /delete_file_by_id 123")) return;

        String id = commandParts[1];
        try {
            Long fileId = Long.parseLong(id);
            if (fileService.deleteFileById(fileId)) {
                botSender.sendMessage(chatId, "✅ Файл з ID " + fileId + " успішно видалено.");
            } else {
                botSender.sendMessage(chatId, "❌ Файл з ID " + fileId + " не знайдено.");
            }
        } catch (NumberFormatException e) {
            botSender.sendMessage(chatId, "❌ Невірний формат ID. Приклад: /delete_file_by_id 123");
        }
    }

    @Override
    public void deleteFileByName(long chatId, String[] commandParts) {
        if (!roleValidator.checkRoleOwnerOrAdmin(chatId)) return;
        if (!comValidator.checkCom(chatId, commandParts, 2,
                "Будь ласка вкажіть ім'я файлу для видалення. Приклад: /delete_file_by_name file_name.docx")) return;

        String fileName = commandParts[1];
        if (fileService.deleteFileByName(fileName)) {
            botSender.sendMessage(chatId, "✅ Файл з ім'ям '" + fileName + "' успішно видалено.");
        } else {
            botSender.sendMessage(chatId, "❌ Файл з ім'ям '" + fileName + "' не знайдено.");
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
        String fileName = "image_" + msg.getFrom().getUserName() + "_" + System.currentTimeMillis() + ".jpg";
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
        String fileName = "video_" + msg.getFrom().getUserName() + "_" + System.currentTimeMillis() + ".mp4";
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
            botSender.sendMessage(uploadedBy, "❌ Не вдалося зберегти файл. Помилка: " + e);
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
    public void getAllFiles(long chatId) {
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
}
