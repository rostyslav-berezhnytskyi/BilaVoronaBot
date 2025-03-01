package com.telegram.bilavorona.controler;

import com.telegram.bilavorona.model.Role;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface UserController {
    void saveUser(Message msg);

    void deleteUser(Message msg, String username);

    void changeRole(Message msg, String username, Role role);

    void showRoleSelectionButtons(Message msg, String username);

    void handleRoleSelection(CallbackQuery callbackQuery);

    void sendForAllUsers(Message msg);
}
