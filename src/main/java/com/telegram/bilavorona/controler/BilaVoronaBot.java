package com.telegram.bilavorona.controler;

import com.telegram.bilavorona.config.BotConfig;
import com.telegram.bilavorona.config.MyBotSender;
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
public class BilaVoronaBot implements LongPollingBot {
    private final BotConfig config;
    private final BotCommandHandler botCommandHandler;
    private final FileHandler fileCommandHandler;
    private final UserController userController;
    private final MyBotSender botSender;

    @Autowired
    public BilaVoronaBot(BotConfig config, BotCommandHandler botCommandHandler, FileHandler fileCommandHandler, UserController userController, MyBotSender botSender) {
        this.config = config;
        this.botCommandHandler = botCommandHandler;
        this.fileCommandHandler = fileCommandHandler;
        this.userController = userController;
        this.botSender = botSender;
        createListOfCommands();
    }

    @Override
    public void onUpdateReceived(Update update) {
        log.info("Received update: {}", update);

        if (update.hasCallbackQuery()) {
            String data = update.getCallbackQuery().getData();
            if (data.startsWith("file_group_")) {
                fileCommandHandler.assignFileGroup(update.getCallbackQuery());
            } else {
                userController.handleRoleSelection(update.getCallbackQuery());
            }
            return;
        }

        Message msg = update.getMessage();
        if (msg == null) return;  // Check for null to avoid NullPointerException

        if (msg.hasDocument() || msg.hasPhoto() || msg.hasVideo()) {
            fileCommandHandler.saveFile(msg);
            return;
        }

        if (msg.hasText()) {
            String[] commandParts = msg.getText().split(" ");
            switch (commandParts[0]) {
                case "/start" -> botCommandHandler.start(msg);
                case "/help" -> botCommandHandler.help(msg);
                case "/delete_user" -> {
                    if (commandParts.length > 1) {
                        userController.deleteUser(msg, commandParts[1]);
                    } else {
                        botSender.sendMessage(msg.getChatId(), "Будь ласка вкажіть юзернейм. Приклад: /deleteUser @username");
                    }
                }
                case "/change_role" -> {
                    if (commandParts.length > 1) {
                        String username = commandParts[1];
                        userController.showRoleSelectionButtons(msg, username);
                    } else {
                        botSender.sendMessage(msg.getChatId(), "Будь ласка вкажіть юзернейм. Приклад: /change_role @username");
                    }
                }
                case "/get_all_files" -> fileCommandHandler.getAllFiles(msg);
                case "/send_for_all_user" -> {
                    if (commandParts.length > 1) {
                        userController.sendForAllUsers(msg);
                    } else {
                        botSender.sendMessage(msg.getChatId(), "Будь ласка вкажіть повідомлення для відправки всім користувачам. Приклад: /send_for_all_user текст для відпавки");
                    }
                }
                default -> botCommandHandler.defaultCom(msg);
            }
        }
    }

    private void createListOfCommands() {
        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "Почати роботу з ботом і отримати привітання"));
//        listOfCommands.add(new BotCommand("/delete_my_data", "видалити мої данні з БД бота - ще не реалізована"));
        listOfCommands.add(new BotCommand("/help", "отримати інформацію по роботі з ботом"));
        listOfCommands.add(new BotCommand("/delete_user", "видаляє вказаного користувача за його юзернеймом з БД (АДМІН)"));
        listOfCommands.add(new BotCommand("/get_all_files", "отримати всі файли"));
        listOfCommands.add(new BotCommand("/change_role", "Змінює роль вказаного користувача за його юзернеймом (АДМІН)"));
        listOfCommands.add(new BotCommand("/send_for_all_user", "Відправляє текстове повідомлення яке буде показано всім користувачам боту (АДМІН)"));
        try {
            botSender.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
            log.info("Bot commands successfully set.");
        } catch (TelegramApiException e) {
            log.error("Error getting bot`s command lis: " + e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return config.getBotUserName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public DefaultBotOptions getOptions() {
        return new DefaultBotOptions();
    }

    @Override
    public void clearWebhook() throws TelegramApiRequestException {

    }
}
