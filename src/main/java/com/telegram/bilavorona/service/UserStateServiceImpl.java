package com.telegram.bilavorona.service;

import com.telegram.bilavorona.util.MyBotSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserStateServiceImpl implements UserStateService{
    private final MyBotSender botSender;

    private final Map<Long, String> userCommandState = new ConcurrentHashMap<>();

    @Autowired
    public UserStateServiceImpl(MyBotSender botSender) {
        this.botSender = botSender;
    }

    public void setCommandState(Long chatId, String command) { // Save command state for a user
        sendRespondToCommand(chatId, command);
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

    private void sendRespondToCommand(Long chatId, String command) {
        String[] commandPart = command.split(" ");
        switch (commandPart[0]) {
            case "sendForAllUsers" -> botSender.sendMessage(chatId, "Вкажіть текст чи файл що буде надісланий всім користувачам");
            case "sendForUsername" -> botSender.sendMessage(chatId, "Вкажіть текст чи файл що буде надісланий " + commandPart[1]);
        }
    }
}
