package com.telegram.bilavorona.controler;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface BotCommandHandler {
    SendMessage start(Message msg);
    SendMessage help(Message msg);
    SendMessage defaultCom(Message msg);
}
