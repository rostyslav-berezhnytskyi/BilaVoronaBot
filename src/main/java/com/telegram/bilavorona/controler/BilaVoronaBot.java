package com.telegram.bilavorona.controler;

import com.telegram.bilavorona.config.BotConfig;
import com.telegram.bilavorona.handler.*;
import com.telegram.bilavorona.service.UserStateService;
import com.telegram.bilavorona.service.UserStatisticsService;
import com.telegram.bilavorona.util.ButtonsSender;
import com.telegram.bilavorona.util.CommandValidator;
import com.telegram.bilavorona.util.MyBotSender;
import com.telegram.bilavorona.model.FileGroup;
import com.telegram.bilavorona.util.TextConstants;
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

import java.util.*;

@Slf4j
@Component
public class BilaVoronaBot implements LongPollingBot {
    private final BotConfig config;
    private final BotCommandHandler botCommandHandler;
    private final FileHandler fileCommandHandler;
    private final UserHandler userHandler;
    private final MyBotSender botSender;
    private final ButtonsSender buttonsSender;
    private final UserStateService userStateService;
    private final CommandValidator commandValidator;
    private final AIHandler aiHandler;
    private final ReportHandler reportHandler;
    private final UserStatisticsHandler userStatisticsHandler;

    @Autowired
    public BilaVoronaBot(BotConfig config, BotCommandHandler botCommandHandler, FileHandler fileCommandHandler, UserHandler userHandler, MyBotSender botSender, ButtonsSender buttonsSender, UserStateService userStateService, CommandValidator commandValidator, AIHandler aiHandler, ReportHandler reportHandler, UserStatisticsHandler userStatisticsHandler) {
        this.config = config;
        this.botCommandHandler = botCommandHandler;
        this.fileCommandHandler = fileCommandHandler;
        this.userHandler = userHandler;
        this.botSender = botSender;
        this.buttonsSender = buttonsSender;
        this.userStateService = userStateService;
        this.commandValidator = commandValidator;
        this.aiHandler = aiHandler;
        this.reportHandler = reportHandler;
        this.userStatisticsHandler = userStatisticsHandler;
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
                case "examples" -> fileCommandHandler.sendFilesByGroup(chatId, FileGroup.EXAMPLES);
                case "contacts" -> botCommandHandler.contacts(chatId);
                case "file_group" -> fileCommandHandler.assignFileGroup(update.getCallbackQuery());
                case "change_role" -> userHandler.handleRoleSelection(update.getCallbackQuery());
                case "contactManager" -> userStateService.setCommandState(chatId, "contactManager");
                case "contactAIAssistant" -> aiHandler.sendAIResponse(chatId, "Користувач бота нажав кнопку зв'язатись з АІ асистентом і хоче з тобою поговорити");
                case "get_discount" -> userStateService.setCommandState(chatId, "waiting_for_phone");
                case "fq" -> botCommandHandler.frequentQuestions(chatId);
                case "home" -> botCommandHandler.home(chatId);
                default -> botSender.sendMessage(chatId, "Невідома callback команда");
            }
            return;
        }

        Message msg = update.getMessage();
        if (msg == null) return;  // Check for null to avoid NullPointerException
        Long chatId = msg.getChatId();
        userStatisticsHandler.recordUserActivity(chatId);

        if (msg.hasText() && msg.getText().equals("/exit")) {
            botCommandHandler.exit(chatId);
            return;
        }

        if (userStateService.hasActiveCommand(chatId)) { // Check if the user has an active command
            String[] command = userStateService.getCommandState(chatId).split(" ");  // Split command and parameters
            switch (command[0]) {
                case "sendForAllUsers" -> userHandler.sendForAllUsers(msg);
                case "sendForUsername" -> userHandler.sendForUsername(msg, command[1]);
                case "contactManager" -> botCommandHandler.sendToManager(msg);
                case "waiting_for_phone" -> userHandler.savePhoneNumber(msg);
                default -> botSender.sendMessage(chatId, "Невідома active команда");
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
                case "/start" -> botCommandHandler.start(msg);
                case "/help" -> botCommandHandler.help(chatId);
                case "/help_admin" -> botCommandHandler.helpAdmin(chatId);
                case "/contact_ai_assistant", "\uD83E\uDD16" -> aiHandler.sendAIResponse(chatId, "Користувач бота нажав кнопку зв'язатись з АІ асистентом і хоче з тобою поговорити");
                case "/contact_manager", "\uD83D\uDCE9" -> userStateService.setCommandState(chatId, "contactManager");
                case "/fq" -> botCommandHandler.frequentQuestions(chatId);
                case "/home", "\uD83C\uDFE0" -> botCommandHandler.home(chatId);

                // Users
                case "/get_all_users" -> userHandler.getAllUsers(chatId);
                case "/get_all_admins" -> userHandler.getAllAdmins(chatId);
                case "/get_all_banned" -> userHandler.getAllBanned(chatId);
                case "/delete_user" -> userHandler.deleteUser(chatId, commandParts);
                case "/change_role" -> buttonsSender.sendRoleSelectionButtons(chatId, commandParts);
                case "/send_for_all_user" -> userStateService.setCommandState(chatId, "sendForAllUsers");
                case "/send_for_username" -> {
                    if (!commandValidator.checkCom(chatId, commandParts, 2, "Будь ласка вкажіть юзернейм. Приклад: /send_for_username @username"))
                        return;
                    userStateService.setCommandState(chatId, "sendForUsername " + commandParts[1]);
                }

                //Reports
                case "/get_chat_history_report" -> reportHandler.sendChatHistoryReportToManager(chatId);
                case "/get_user_statistics_for_week_report" -> reportHandler.sendUserStatisticsForWeekToManager(chatId);
                case "/get_user_statistics_for_day" -> userStatisticsHandler.getDailyUniqueUsers(chatId);
                case "/get_user_statistics_for_week" -> userStatisticsHandler.getWeeklyUniqueUsers(chatId);
                case "/get_user_statistics_for_month" -> userStatisticsHandler.getMonthlyUniqueUsers(chatId);

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

                default -> aiHandler.sendAIResponse(chatId, msg.getText());
            }
        }
    }

    private void createListOfCommands() {
        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "Почати роботу з ботом і отримати привітання"));
        listOfCommands.add(new BotCommand("/help", "Отримати інформацію по роботі з ботом"));
        listOfCommands.add(new BotCommand("/contact_manager", "Зв'язатися з нашим менеджером"));
        listOfCommands.add(new BotCommand("/contact_ai_assistant", "Зв'язатися з нашим AI асистентом, який працює 24/7"));
        listOfCommands.add(new BotCommand("/fq", "Відповіді на поширені запитання"));
        // 📄 Documentation and Examples
        listOfCommands.add(new BotCommand("/documentation", "Отримати документи з розділу Документація"));
        listOfCommands.add(new BotCommand("/examples", "Отримати приклади виконаних робіт"));
        // 📞 Contacts
        listOfCommands.add(new BotCommand("/contacts", "Отримати контактну інформацію"));
        listOfCommands.add(new BotCommand("/help_admin", "Отримати каманди адміністратора"));
        listOfCommands.add(new BotCommand("/exit", "Скасування віправки всіх активних команд"));
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
