package com.telegram.bilavorona.util;

public interface ButtonsSender {
    void sendPersistentButtons(Long chatId);

    void sendInlinePersistentButtons(Long chatId);

    void sendRoleSelectionButtons(long chatId, String[] commandParts);

    void sendGroupSelectionButtons(Long chatId);

    void sendContactUserButton(long userId, long adminId);
}
