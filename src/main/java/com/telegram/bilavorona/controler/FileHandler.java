package com.telegram.bilavorona.controler;

import com.telegram.bilavorona.model.FileGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface FileHandler {
    void saveFile(Message msg);

    void getAllFiles(Message msg);

    void assignFileGroup(CallbackQuery callbackQuery);

    void sendFilesByGroup(Long chatId, FileGroup group);

    void deleteFileByName(Message msg, String fileName);

    void deleteFileById(Message msg, String id);

    void changeFileGroupById(Message msg, String id, String newGroup);

    void changeFileGroupByName(Message msg, String fileName, String newGroup);

    void changeFileNameById(Message msg, String id, String newFileName);

    void changeFileNameByName(Message msg, String currentFileName, String newFileName);
}
