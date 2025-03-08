package com.telegram.bilavorona.bila_vorona_manager;

import org.telegram.telegrambots.meta.api.objects.Message;

public interface ManagerCommandHandler {
    void start(Message msg);

    void help(long chatId);

    void defaultCom(long chatId);
}
