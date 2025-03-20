package com.telegram.bilavorona.service;

import com.telegram.bilavorona.model.User;

public interface AIChatService {
    String getChatResponse(User user, String userMessage);
}
