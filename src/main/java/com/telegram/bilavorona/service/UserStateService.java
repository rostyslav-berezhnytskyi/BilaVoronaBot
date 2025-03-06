package com.telegram.bilavorona.service;

public interface UserStateService {
    void setCommandState(Long chatId, String command);

    String getCommandState(Long chatId);

    void clearCommandState(Long chatId);

    boolean hasActiveCommand(Long chatId);
}
