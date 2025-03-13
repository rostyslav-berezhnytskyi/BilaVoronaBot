package com.telegram.bilavorona.handler;

import com.telegram.bilavorona.model.Role;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface UserHandler {
    void saveUser(Message msg);

    void getAllUsers(long chatId);

    void deleteUser(long chatId, String[] commandParts);

    void changeRole(long chatId, String username, Role role);

    void handleRoleSelection(CallbackQuery callbackQuery);

    void sendForAllUsers(Message msg);

    void sendForUsername(Message msg, String username);

    void savePhoneNumber(Message msg);
}
