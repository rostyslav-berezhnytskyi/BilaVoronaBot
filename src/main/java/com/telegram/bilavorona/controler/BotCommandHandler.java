package com.telegram.bilavorona.controler;

import org.telegram.telegrambots.meta.api.objects.Message;

public interface BotCommandHandler {
    void start(Message msg);
    void help(Message msg);
    void defaultCom(Message msg);
    void deleteUser(Message msg, String username);
    void sendMessage(Long chatId, String text);
    void saveFile(Message msg);
    void getAllFiles(Message msg);
}
