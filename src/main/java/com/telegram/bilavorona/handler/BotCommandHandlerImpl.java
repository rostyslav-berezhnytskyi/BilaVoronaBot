package com.telegram.bilavorona.handler;

import com.telegram.bilavorona.bila_vorona_manager.ManagerBotSender;
import com.telegram.bilavorona.model.User;
import com.telegram.bilavorona.service.UserStateService;
import com.telegram.bilavorona.util.*;
import com.telegram.bilavorona.service.UserService;
import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@Slf4j
@Service
public class BotCommandHandlerImpl implements BotCommandHandler {
    private final UserService userService;
    private final MyBotSender botSender;
    private final RoleValidator roleValidator;
    private final ButtonsSender buttonsSender;
    private final UserStateService userStateService;
    private final ManagerBotSender managerBotSender;

    @Autowired
    public BotCommandHandlerImpl(UserService userService, MyBotSender botSender, RoleValidator roleValidator, ButtonsSender buttonsSender, UserStateService userStateService, ManagerBotSender managerBotSender) {
        this.userService = userService;
        this.botSender = botSender;
        this.roleValidator = roleValidator;
        this.buttonsSender = buttonsSender;
        this.userStateService = userStateService;
        this.managerBotSender = managerBotSender;
    }

    @Override
    public void start(Message msg) {
        String name = msg.getChat().getFirstName();
        Long chatId = msg.getChatId();
        log.info("Invoke /start command for user {} in chatId {}", name, chatId);
        boolean isNewUser = userService.saveUser(msg);
        String greetingForNewUser = "Вітаю , " + name + ", приємно познайомитись! " + EmojiParser.parseToUnicode(":blush:");
        String greetingForOldUser = "Вітаю , " + name + "! " + EmojiParser.parseToUnicode(":wave:");
        String answer = isNewUser ? greetingForNewUser : greetingForOldUser;
        botSender.sendMessage(chatId, answer);
        buttonsSender.sendPersistentButtons(chatId);
        buttonsSender.sendInlinePersistentButtons(chatId);
    }

    @Override
    public void help(long chatId) {
        log.info("Invoke /help command in chatId {}", chatId);
        botSender.sendMessage(chatId, TextConstants.HELP_TEXT);
    }

    @Override
    public void helpAdmin(long chatId) {
        if (!roleValidator.checkRoleOwnerOrAdmin(chatId)) return;
        log.info("Invoke /helpAdmin command in chatId {}", chatId);
        botSender.sendMessage(chatId, TextConstants.HELP_TEXT_ADMIN);
    }

    @Override
    public void contacts(long chatId) {
        log.info("Invoke contacts command in chatId {}", chatId);
        botSender.sendMessage(chatId, TextConstants.CONTACTS_TEXT);
    }

    @Override
    public void defaultCom(long chatId) {
        log.info("Invoke unknown command. Providing default message in chatId {}", chatId);
        String answer = "Невідома команда. Використовуйте /help, щоб побачити доступні команди.";
        botSender.sendMessage(chatId, answer);
    }

    @Override
    public void exit(long chatId) {
        userStateService.clearCommandState(chatId);
        botSender.sendMessage(chatId, "Відправка всіх активних команд скасована");
    }

    @Override
    public void sendToManager(Message msg) {
        Long chatId = msg.getChatId();
        log.info("Sending message to manager from chatId = {}", chatId);

        try {
            User user = userService.findById(chatId).get();
            boolean usernameFlag = user.getUserName() != null && !user.getUserName().isEmpty();
            boolean textMessage = msg.hasText();
            if(textMessage && msg.getText().equals("/exit")) {
                botSender.sendMessage(chatId, "Надсилання повідомлення менеджеру відмінено");
                return;
            }

            String userInfo = formTextMessage(user, usernameFlag, msg);
            List<User> admins = userService.findAllAdmins();
            for (User admin : admins) {
                managerBotSender.sendMessage(admin.getChatId(), userInfo);  // Send message to manager
                if (!textMessage) managerBotSender.sendFileToManager(admin.getChatId(), msg);
                if (!usernameFlag) buttonsSender.sendContactUserButton(chatId, admin.getChatId());
            }

            botSender.sendMessage(chatId, "Повідомлення успішно надіслано менеджеру");
        } finally {
            userStateService.clearCommandState(chatId);
        }
    }



    private String formTextMessage(User user, boolean usernameFlag, Message msg) {
        String textOfMessage = msg.hasText() ? "\uD83D\uDCE9 *Надіслане повідомлення:* \n" + msg.getText(): "\uD83D\uDCE9 *Надісланий файл:*";

        String userInfo = String.format(
                "🧑‍💼 *Користувач:*\n" +
                        "Ім'я: %s %s\n" +
                        "Username: %s\n" +
                        "ID: %d\n\n" +
                        "%s",
                user.getFirstName() != null ? user.getFirstName() : "Невідомий",
                user.getLastName() != null ? user.getLastName() : "",
                usernameFlag ? "@" + user.getUserName() : "Невідомий",
                user.getChatId(),
                textOfMessage);

        return userInfo;
    }
}
