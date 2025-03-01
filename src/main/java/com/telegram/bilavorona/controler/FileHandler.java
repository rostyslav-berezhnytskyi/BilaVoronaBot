package com.telegram.bilavorona.controler;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface FileHandler {
    void saveFile(Message msg);
    void getAllFiles(Message msg);
    void assignFileGroup(CallbackQuery callbackQuery);
}
