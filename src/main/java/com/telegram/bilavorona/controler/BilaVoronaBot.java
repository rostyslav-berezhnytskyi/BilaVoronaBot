package com.telegram.bilavorona.controler;

import com.telegram.bilavorona.config.BotConfig;
import com.telegram.bilavorona.util.ButtonsSender;
import com.telegram.bilavorona.util.MyBotSender;
import com.telegram.bilavorona.handler.BotCommandHandler;
import com.telegram.bilavorona.handler.FileHandler;
import com.telegram.bilavorona.handler.UserHandler;
import com.telegram.bilavorona.model.FileGroup;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class BilaVoronaBot implements LongPollingBot {
    private final BotConfig config;
    private final BotCommandHandler botCommandHandler;
    private final FileHandler fileCommandHandler;
    private final UserHandler userHandler;
    private final MyBotSender botSender;
    private final ButtonsSender buttonsSender;

    private final Map<Long, Boolean> sendForAllUserState = new HashMap<>(); // Track if a user is in the process of sending a message to all users

    @Autowired
    public BilaVoronaBot(BotConfig config, BotCommandHandler botCommandHandler, FileHandler fileCommandHandler, UserHandler userHandler, MyBotSender botSender, ButtonsSender buttonsSender) {
        this.config = config;
        this.botCommandHandler = botCommandHandler;
        this.fileCommandHandler = fileCommandHandler;
        this.userHandler = userHandler;
        this.botSender = botSender;
        this.buttonsSender = buttonsSender;
        createListOfCommands();
    }

    @Override
    public void onUpdateReceived(Update update) {
        log.info("Received update: {}", update);

        if (update.hasCallbackQuery()) {
            Long chatId = update.getCallbackQuery().getMessage().getChatId();
            String data = update.getCallbackQuery().getData();

            if (data.startsWith("file_group_")) {
                fileCommandHandler.assignFileGroup(update.getCallbackQuery());
                return;  // Exit early if handled
            }

            switch (data) {
                case "/documentation" -> fileCommandHandler.sendFilesByGroup(chatId, FileGroup.DOCUMENTATION);
                case "/examples" -> fileCommandHandler.sendFilesByGroup(chatId, FileGroup.EXAMPLES);
                case "/contacts" -> botCommandHandler.contacts(chatId);
                default -> userHandler.handleRoleSelection(update.getCallbackQuery());
            }
            return;
        }

        Message msg = update.getMessage();
        if (msg == null) return;  // Check for null to avoid NullPointerException
        Long chatId = msg.getChatId();

        if (sendForAllUserState.getOrDefault(chatId, false)) { // Check if user is in the state to send message to all users
            userHandler.sendForAllUsers(msg);  // Send the message to all users
            sendForAllUserState.put(chatId, false);  // Reset the state
            return;
        }

        if (msg.hasDocument() || msg.hasPhoto() || msg.hasVideo()) {
            fileCommandHandler.saveFile(msg);
            return;
        }

        if (msg.hasText()) {
            String[] commandParts = msg.getText().split(" ");

            switch (commandParts[0]) {
                // General
                case "/start" -> botCommandHandler.start(msg);
                case "/help" -> botCommandHandler.help(chatId);
                case "/help_admin" -> botCommandHandler.helpAdmin(chatId);

                // Users
                case "/get_all_users" -> userHandler.getAllUsers(chatId);
                case "/delete_user" -> userHandler.deleteUser(chatId, commandParts);
                case "/change_role" -> buttonsSender.sendRoleSelectionButtons(chatId, commandParts);
                case "/send_for_all_user" -> {
                    sendForAllUserState.put(chatId, true);  // Mark that this user wants to send a message to all users
                    botSender.sendMessage(chatId, "Вкажіть текст чи файл що буде надісланий всім користувачам");
                }
                // Files
                case "/get_all_files" -> fileCommandHandler.getAllFiles(chatId);
                case "/change_file_group_by_id" -> fileCommandHandler.changeFileGroupById(chatId, commandParts);
                case "/change_file_group_by_name" -> fileCommandHandler.changeFileGroupByName(chatId, commandParts);
                case "/change_file_name_by_id" -> fileCommandHandler.changeFileNameById(chatId, commandParts);
                case "/change_file_name_by_name" -> fileCommandHandler.changeFileNameByName(chatId, commandParts);
                case "/delete_file_by_id" -> fileCommandHandler.deleteFileById(chatId, commandParts);
                case "/delete_file_by_name" -> fileCommandHandler.deleteFileByName(chatId, commandParts);

                // Handle persistent button presses
                case "/documentation", "📄" -> fileCommandHandler.sendFilesByGroup(chatId, FileGroup.DOCUMENTATION);
                case "/examples", "📋" -> fileCommandHandler.sendFilesByGroup(chatId, FileGroup.EXAMPLES);
                case "/contacts", "\uD83D\uDCDE" -> botCommandHandler.contacts(chatId);

                default -> botCommandHandler.defaultCom(chatId);
            }
        }
    }

    private void createListOfCommands() {
        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "Почати роботу з ботом і отримати привітання"));
        listOfCommands.add(new BotCommand("/help", "Отримати інформацію по роботі з ботом"));
        // 📄 Documentation and Examples
        listOfCommands.add(new BotCommand("/documentation", "Отримати документи з розділу Документація"));
        listOfCommands.add(new BotCommand("/examples", "Отримати приклади виконаних робіт"));
        // 📞 Contacts
        listOfCommands.add(new BotCommand("/contacts", "Отримати контактну інформацію"));
        listOfCommands.add(new BotCommand("/help_admin", "Отримати каманди адміністратора"));
//        listOfCommands.add(new BotCommand("/contact_manager", "Написати нашому менеджеру"));
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
