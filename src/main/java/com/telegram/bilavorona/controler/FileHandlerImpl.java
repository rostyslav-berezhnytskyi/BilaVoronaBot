package com.telegram.bilavorona.controler;

import com.telegram.bilavorona.config.BotConfig;
import com.telegram.bilavorona.config.MyBotSender;
import com.telegram.bilavorona.model.FileEntity;
import com.telegram.bilavorona.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileHandlerImpl implements FileHandler{
    private final FileService fileService;
    private final MyBotSender botSender;
    private final BotConfig botConfig;

    @Override
    public void saveFile(Message msg) {
        if (!msg.hasDocument()) {
            sendMessage(msg.getChatId(), "❌ Ви не надіслали документ!");
            return;
        }

        Document doc = msg.getDocument();
        String fileId = doc.getFileId();
        String fileName = doc.getFileName();
        Long fileSize = doc.getFileSize();
        String mimeType = doc.getMimeType();
        Long uploadedBy = msg.getFrom().getId();

        try {
            // Отримуємо шлях до файлу
            GetFile getFile = new GetFile();
            getFile.setFileId(fileId);
            File file = botSender.execute(getFile);

            // Завантажуємо файл
            byte[] fileBytes = downloadFile(file.getFilePath());

            // Зберігаємо у БД через FileService
            fileService.saveFile(fileName, mimeType, fileSize, fileBytes, uploadedBy);

            sendMessage(msg.getChatId(), "✅ Файл успішно збережено у базі даних!");
        } catch (TelegramApiException | IOException e) {
            log.error("Помилка завантаження файлу: {}", e.getMessage());
            sendMessage(msg.getChatId(), "❌ Не вдалося зберегти файл.");
        }
    }

    @Override
    public void getAllFiles(Message msg) {
        List<FileEntity> files = fileService.getAllFiles();

        if (files.isEmpty()) {
            sendMessage(msg.getChatId(), "📂 Немає збережених файлів.");
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
                sendMessage(msg.getChatId(), "❌ Не вдалося надіслати файл: " + file.getFileName());
            }
        }
    }

    private byte[] downloadFile(String filePath) throws IOException {
        URL fileUrl = new URL("https://api.telegram.org/file/bot" + botConfig.getToken() + "/" + filePath);
        return fileUrl.openStream().readAllBytes();
    }

    private void sendMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        try {
            botSender.execute(message);
        } catch (TelegramApiException e) {
            log.error("Failed to execute message: {}", e.getMessage());
        }
    }
}
