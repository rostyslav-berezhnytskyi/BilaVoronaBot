package com.telegram.bilavorona.handler;

import com.telegram.bilavorona.model.FileGroup;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface FileHandler {
    void saveFile(Message msg);

    void getAllFiles(long chatId);

    void assignFileGroup(CallbackQuery callbackQuery);

    void sendFilesByGroup(Long chatId, FileGroup group);

    void deleteFileByName(long chatId, String[] commandParts);

    void deleteFileById(long chatId, String[] commandParts);

    void changeFileGroupById(long chatId, String[] commandParts);

    void changeFileGroupByName(long chatId, String[] commandParts);

    void changeFileNameById(long chatId, String[] commandParts);

    void changeFileNameByName(long chatId, String[] commandParts);
}
