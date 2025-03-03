package com.telegram.bilavorona.controler;

import com.telegram.bilavorona.config.BotConfig;
import com.telegram.bilavorona.config.MyBotSender;
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
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
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
    private final UserController userController;
    private final MyBotSender botSender;

    private final Map<Long, Boolean> sendForAllUserState = new HashMap<>(); // Track if a user is in the process of sending a message to all users

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
            Long chatId = update.getCallbackQuery().getMessage().getChatId();
            String data = update.getCallbackQuery().getData();
            if (data.startsWith("file_group_")) {
                fileCommandHandler.assignFileGroup(update.getCallbackQuery());
            } else if (data.equals("/documentation")) {
                fileCommandHandler.sendFilesByGroup(chatId, FileGroup.DOCUMENTATION);
            } else if (data.equals("/examples")) {
                fileCommandHandler.sendFilesByGroup(chatId, FileGroup.EXAMPLES);
            } else if (data.equals("/contacts")) {
                botCommandHandler.contacts(chatId);
            } else {
                userController.handleRoleSelection(update.getCallbackQuery());
            }
            return;
        }

        Message msg = update.getMessage();
        if (msg == null) return;  // Check for null to avoid NullPointerException
        Long chatId = msg.getChatId();

        if (sendForAllUserState.getOrDefault(chatId, false)) { // Check if user is in the state to send message to all users
            userController.sendForAllUsers(msg);  // Send the message to all users
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
                case "📄" -> commandParts[0] = "/documentation";
                case "📋" -> commandParts[0] = "/examples";
                case "\uD83D\uDCDE" -> commandParts[0] = "/contacts";
                default -> commandParts[0] = commandParts[0];
            }

            switch (commandParts[0]) {
                case "/start" -> {
                    botCommandHandler.start(msg);
                    sendPersistentButtons(chatId);
                    sendInlinePersistentButtons(chatId);
                }
                case "/help" -> botCommandHandler.help(msg);
                case "/help_admin" -> botCommandHandler.helpAdmin(msg);
                case "/contacts" -> botCommandHandler.contacts(chatId);
                case "/get_all_users" -> userController.getAllUsers(msg);
                case "/delete_user" -> {
                    if (commandParts.length > 1) {
                        userController.deleteUser(msg, commandParts[1]);
                    } else {
                        botSender.sendMessage(chatId, "Будь ласка вкажіть юзернейм. Приклад: /deleteUser @username");
                    }
                }
                case "/change_role" -> {
                    if (commandParts.length > 1) {
                        String username = commandParts[1];
                        userController.showRoleSelectionButtons(msg, username);
                    } else {
                        botSender.sendMessage(chatId, "Будь ласка вкажіть юзернейм. Приклад: /change_role @username");
                    }
                }
                case "/get_all_files" -> fileCommandHandler.getAllFiles(msg);
                case "/change_file_group_by_id" -> {
                    if (commandParts.length > 2) {
                        fileCommandHandler.changeFileGroupById(msg, commandParts[1], commandParts[2]);
                    } else {
                        botSender.sendMessage(chatId, "Приклад: /change_file_group_by_id 123 DOCUMENTATION");
                    }
                }
                case "/change_file_group_by_name" -> {
                    if (commandParts.length > 2) {
                        fileCommandHandler.changeFileGroupByName(msg, commandParts[1], commandParts[2]);
                    } else {
                        botSender.sendMessage(chatId, "Приклад: /change_file_group_by_name file_name.docx EXAMPLES");
                    }
                }
                case "/change_file_name_by_id" -> {
                    if (commandParts.length > 2) {
                        fileCommandHandler.changeFileNameById(msg, commandParts[1], commandParts[2]);
                    } else {
                        botSender.sendMessage(chatId, "Приклад: /change_file_name_by_id 123 new_name.docx");
                    }
                }
                case "/change_file_name_by_name" -> {
                    if (commandParts.length > 2) {
                        fileCommandHandler.changeFileNameByName(msg, commandParts[1], commandParts[2]);
                    } else {
                        botSender.sendMessage(chatId, "Приклад: /change_file_name_by_name old_name.docx new_name.docx");
                    }
                }
                case "/delete_file_by_id" -> {
                    if (commandParts.length > 1) {
                        fileCommandHandler.deleteFileById(msg, commandParts[1]);
                    } else {
                        botSender.sendMessage(chatId, "Приклад: /delete_file_by_id 123");
                    }
                }
                case "/delete_file_by_name" -> {
                    if (commandParts.length > 1) {
                        fileCommandHandler.deleteFileByName(msg, commandParts[1]);
                    } else {
                        botSender.sendMessage(chatId, "Приклад: /delete_file_by_name file_name.docx");
                    }
                }
                case "/send_for_all_user" -> {
                    sendForAllUserState.put(chatId, true);  // Mark that this user wants to send a message to all users
                    botSender.sendMessage(chatId, "Вкажіть текст чи файл що буде надісланий всім користувачам");
                }
                // Handle persistent button presses
                case "/documentation" -> fileCommandHandler.sendFilesByGroup(chatId, FileGroup.DOCUMENTATION);
                case "/examples" -> fileCommandHandler.sendFilesByGroup(chatId, FileGroup.EXAMPLES);
                default -> botCommandHandler.defaultCom(msg);
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

    private void sendPersistentButtons(Long chatId) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);

        KeyboardButton docsButton = new KeyboardButton("📄 Документація");
        KeyboardButton examplesButton = new KeyboardButton("📋 Приклади виконаних робіт");
        KeyboardButton contactsButton = new KeyboardButton("\uD83D\uDCDE Наші контакти");

        // Creating rows for buttons
        KeyboardRow row = new KeyboardRow();
        row.add(docsButton);
        row.add(examplesButton);
        row.add(contactsButton);

        List<KeyboardRow> keyboard = new ArrayList<>();
        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);

        botSender.sendKeyboardMarkupMessage(chatId,"Вітальне повідомлення Біла Ворона", keyboardMarkup);
    }

    private void sendInlinePersistentButtons(Long chatId) {
        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        InlineKeyboardButton docsButton = new InlineKeyboardButton("📄 Документація");
        docsButton.setCallbackData("/documentation");  // Command to be executed

        InlineKeyboardButton examplesButton = new InlineKeyboardButton("📋 Приклади виконаних робіт");
        examplesButton.setCallbackData("/examples");  // Command to be executed

        InlineKeyboardButton contactsButton = new InlineKeyboardButton("\uD83D\uDCDE Наші контакти");
        contactsButton.setCallbackData("/contacts");  // Command to be executed

        // Create a row for the buttons
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(docsButton);
        row.add(examplesButton);
        row.add(contactsButton);
        rows.add(row);

        inlineKeyboard.setKeyboard(rows);
        botSender.sendInlineKeyboardMarkupMessage(chatId, "⬇️ Виберіть одну з базових дій:", inlineKeyboard);
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
