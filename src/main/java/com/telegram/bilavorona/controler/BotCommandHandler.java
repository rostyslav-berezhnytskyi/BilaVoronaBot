package com.telegram.bilavorona.controler;

import com.telegram.bilavorona.model.Role;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

public interface BotCommandHandler {
    void start(Message msg);

    void help(Message msg);

    void defaultCom(Message msg);

    void contacts(Long chatId);

    void helpAdmin(Message msg);
}
