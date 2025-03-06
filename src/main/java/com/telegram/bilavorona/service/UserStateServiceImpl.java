package com.telegram.bilavorona.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserStateServiceImpl implements UserStateService{
    private final Map<Long, String> userCommandState = new ConcurrentHashMap<>();

    public void setCommandState(Long chatId, String command) { // Save command state for a user
        userCommandState.put(chatId, command);
    }

    public String getCommandState(Long chatId) { // Get command state for a user
        return userCommandState.get(chatId);
    }

    public void clearCommandState(Long chatId) { // Remove command state for a user
        userCommandState.remove(chatId);
    }

    public boolean hasActiveCommand(Long chatId) { // Check if a user has an active command
        return userCommandState.containsKey(chatId);
    }
}
