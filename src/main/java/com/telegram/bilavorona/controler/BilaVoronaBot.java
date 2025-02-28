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
    private final MyBotSender botSender;

    @Autowired
    public BilaVoronaBot(BotConfig config, BotCommandHandler botCommandHandler, MyBotSender botSender) {
        this.config = config;
        this.botCommandHandler = botCommandHandler;
        this.botSender = botSender;
        createListOfCommands();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage()) return;

        Message msg = update.getMessage();

        if (msg.hasDocument()) {
            botCommandHandler.saveFile(msg);
            return;
        }

        if (msg.hasText()) {
            String[] commandParts = msg.getText().split(" ");
            switch (commandParts[0]) {
                case "/start" -> botCommandHandler.start(msg);
                case "/help" -> botCommandHandler.help(msg);
                case "/delete_user" -> {
                    if (commandParts.length > 1) {
                        botCommandHandler.deleteUser(msg, commandParts[1]);
                    } else {
                        botCommandHandler.sendMessage(msg.getChatId(), "Будь ласка вкажіть юзернейм. Приклад: /deleteUser @username");
                    }
                }
                case "/get_all_files" -> botCommandHandler.getAllFiles(msg);
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
