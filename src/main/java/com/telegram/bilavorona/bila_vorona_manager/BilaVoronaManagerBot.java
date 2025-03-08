package com.telegram.bilavorona.bila_vorona_manager;

import com.telegram.bilavorona.handler.BotCommandHandler;
import com.telegram.bilavorona.handler.FileHandler;
import com.telegram.bilavorona.handler.UserHandler;
import com.telegram.bilavorona.model.FileGroup;
import com.telegram.bilavorona.service.UserStateService;
import com.telegram.bilavorona.util.ButtonsSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.meta.generics.LongPollingBot;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class BilaVoronaManagerBot implements LongPollingBot {
    private final BotManagerConfig managerBotConfig;
    private final BotCommandHandler botCommandHandler;
    private final FileHandler fileCommandHandler;
    private final UserHandler userHandler;
    private final ManagerBotSender managerBotSender;
    private final ButtonsSender buttonsSender;
    private final UserStateService userStateService;
    private final ManagerCommandHandler managerCommandHandler;

    @Autowired
    public BilaVoronaManagerBot(BotManagerConfig managerBotConfig, BotCommandHandler botCommandHandler, FileHandler fileCommandHandler, UserHandler userHandler, ManagerBotSender managerBotSender, ButtonsSender buttonsSender, UserStateService userStateService, ManagerCommandHandler managerCommandHandler) {
        this.managerBotConfig = managerBotConfig;
        this.botCommandHandler = botCommandHandler;
        this.fileCommandHandler = fileCommandHandler;
        this.userHandler = userHandler;
        this.managerBotSender = managerBotSender;
        this.buttonsSender = buttonsSender;
        this.userStateService = userStateService;
        this.managerCommandHandler = managerCommandHandler;
        createListOfCommands();
    }

    @Override
    public void onUpdateReceived(Update update) {
        log.info("Received update: {}", update);

        if (update.hasCallbackQuery()) {
            Long chatId = update.getCallbackQuery().getMessage().getChatId();
            String data = update.getCallbackQuery().getData();
            String[] dataSplit = data.split(":");

            switch (dataSplit[0]) {
                case "documentation" -> fileCommandHandler.sendFilesByGroup(chatId, FileGroup.DOCUMENTATION);
                default -> managerBotSender.sendMessage(chatId, "Невідома callback команда");
            }
            return;
        }

        Message msg = update.getMessage();
        if (msg == null) return;  // Check for null to avoid NullPointerException
        Long chatId = msg.getChatId();

        if (userStateService.hasActiveCommand(chatId)) { // Check if the user has an active command
            String[] command = userStateService.getCommandState(chatId).split(" ");  // Split command and parameters
            switch (command[0]) {
                case "sendForAllUsers" -> userHandler.sendForAllUsers(msg);
                default -> managerBotSender.sendMessage(chatId, "Невідома active команда");
            }
            return;
        }

        if (msg.hasDocument() || msg.hasPhoto() || msg.hasVideo()) { // if was sent file / image / video
            fileCommandHandler.saveFile(msg);
            return;
        }

        if (msg.hasText()) {
            String[] commandParts = msg.getText().split(" ");

            switch (commandParts[0]) {
                // General
                case "/start" -> managerCommandHandler.start(msg);
                case "/help" -> managerCommandHandler.help(chatId);

                default -> managerCommandHandler.defaultCom(chatId);
            }
        }
    }

    private void createListOfCommands() {
        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "Почати роботу з ботом і отримати привітання"));
        listOfCommands.add(new BotCommand("/help", "Отримати інформацію по роботі з ботом"));

        try {
            managerBotSender.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
            log.info("Bot commands successfully set.");
        } catch (TelegramApiException e) {
            log.error("Error getting bot`s command lis: " + e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return managerBotConfig.getBotUserName();
    }

    @Override
    public String getBotToken() {
        return managerBotConfig.getToken();
    }

    @Override
    public DefaultBotOptions getOptions() {
        return new DefaultBotOptions();
    }

    @Override
    public void clearWebhook() throws TelegramApiRequestException {

    }
}

