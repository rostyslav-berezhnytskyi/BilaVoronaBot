package com.telegram.bilavorona.service;

import org.telegram.telegrambots.meta.api.objects.InputFile;

public interface TelegramFileService {
    InputFile downloadFile(String fileId);

    void deleteTempFile(InputFile inputFile);
}
