package com.telegram.bilavorona.handler;

import org.telegram.telegrambots.meta.api.objects.Message;

public interface BotCommandHandler {
    void start(Message msg);

    void help(long chatId);

    void defaultCom(long chatId);

    void exit(long chatId);

    void contacts(long chatId);

    void helpAdmin(long chatId);

    void sendToManager(Message msg);

    void home(long chatId);
}
