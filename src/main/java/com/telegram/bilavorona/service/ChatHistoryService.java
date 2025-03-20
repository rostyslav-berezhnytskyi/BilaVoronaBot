package com.telegram.bilavorona.service;

import com.telegram.bilavorona.model.ChatHistory;
import com.telegram.bilavorona.model.User;

import java.util.List;

public interface ChatHistoryService {
    List<ChatHistory> getLastMessages(long chatId, int limit);

    void saveChatMessage(User user, String role, String message);
}
