package com.telegram.bilavorona.manager_bot;

import org.telegram.telegrambots.meta.api.objects.Message;

public interface ManagerCommandHandler {
    void start(Message msg);

    void help(long chatId);

    void defaultCom(long chatId);
}
