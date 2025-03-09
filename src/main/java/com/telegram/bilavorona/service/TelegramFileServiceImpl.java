package com.telegram.bilavorona.service;

import com.telegram.bilavorona.util.MyBotSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@Service
public class TelegramFileServiceImpl implements TelegramFileService {
    private final MyBotSender botSender;

    @Autowired
    public TelegramFileServiceImpl(MyBotSender botSender) {
        this.botSender = botSender;
    }

    public InputFile downloadFile(String fileId) {
        try {
            GetFile getFile = new GetFile(fileId);
            File file = botSender.execute(getFile);

            if (file != null && file.getFilePath() != null) {
                URL url = new URL("https://api.telegram.org/file/bot" + botSender.getBotToken() + "/" + file.getFilePath());

                String originalFileName = file.getFilePath().substring(file.getFilePath().lastIndexOf('/') + 1);
                String uniqueFileName = UUID.randomUUID() + "_" + originalFileName;
                Path downloadPath = Paths.get("temp", uniqueFileName);

                Files.createDirectories(downloadPath.getParent());
                Files.copy(url.openStream(), downloadPath);

                return new InputFile(downloadPath.toFile());
            } else {
                log.error("File or file path is null");
            }
        } catch (TelegramApiException | MalformedURLException e) {
            log.error("Error downloading file: {}", e.getMessage());
        } catch (IOException e) {
            log.error("Error with IO operation {}", e.getMessage());
        }
        return null;
    }

    public void deleteTempFile(InputFile inputFile) {
        try {
            Path path = Paths.get(inputFile.getNewMediaFile().getAbsolutePath());
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.error("Failed to delete temporary file: {}", e.getMessage());
        }
    }
}