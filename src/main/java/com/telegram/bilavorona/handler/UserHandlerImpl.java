package com.telegram.bilavorona.handler;

import com.telegram.bilavorona.service.UserStateService;
import com.telegram.bilavorona.util.CommandValidator;
import com.telegram.bilavorona.util.MyBotSender;
import com.telegram.bilavorona.model.Role;
import com.telegram.bilavorona.model.User;
import com.telegram.bilavorona.service.UserService;
import com.telegram.bilavorona.util.RoleValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;
import java.util.Optional;


@Slf4j
@Component
public class UserHandlerImpl implements UserHandler {
    private final UserService userService;
    private final RoleValidator roleValidator;
    private final MyBotSender botSender;
    private final CommandValidator comValidator;
    private final UserStateService userStateService;

    @Autowired
    public UserHandlerImpl(UserService userService, RoleValidator roleValidator, MyBotSender botSender, CommandValidator comValidator, UserStateService userStateService) {
        this.userService = userService;
        this.roleValidator = roleValidator;
        this.botSender = botSender;
        this.comValidator = comValidator;
        this.userStateService = userStateService;
    }

    @Override
    public void saveUser(Message msg) {
        log.info("Called the command to save the user in chatId = {}", msg.getChatId());
        userService.saveUser(msg);
    }

    @Override
    public void getAllUsers(long chatId) {
        log.info("Called the command to get all the users in chatId = {}", chatId);
        if (!roleValidator.checkRoleOwnerOrAdmin(chatId)) return;

        List<User> allUsers = userService.findAll();
        int count = 0;
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < allUsers.size(); i++) {
            String userStr = "ID: " + allUsers.get(i).getChatId() +
                    ", user_name: " + allUsers.get(i).getUserName() +
                    ", first_name: " + (allUsers.get(i).getFirstName() == null ? "" : allUsers.get(i).getFirstName()) +
                    ", last_name:  " + (allUsers.get(i).getLastName() == null ? "" : allUsers.get(i).getLastName())
                    + "\n";
            builder.append(userStr);
            count++;
            if (count == 10 || i == allUsers.size() - 1) {
                botSender.sendMessage(chatId, builder.toString());
                count = 0;
                builder = new StringBuilder();
            }
        }
    }

    @Override
    public void deleteUser(long chatId, String[] commandParts) {
        if (!roleValidator.checkRoleOwnerOrAdmin(chatId)) return;
        if (!comValidator.checkCom(chatId, commandParts, 2, "Будь ласка вкажіть юзернейм. Приклад: /deleteUser @username"))
            return;

        String username = commandParts[1];
        String answer;

        if (userService.deleteByUsername(username)) {
            answer = "Користувача з юзернеймом " + username + " видалено з БД";
            log.info("User successfully deleted from DB in chatId = {}", chatId);
        } else {
            answer = "Не вдалось видалити користувача з БД";
            log.error("Some error occurred when deleted user from DB in chatId = {}", chatId);
        }

        botSender.sendMessage(chatId, answer);
    }

    @Override
    public void changeRole(long chatId, String username, Role role) {
        log.info("Called the command to change the user role by username (change_role) in chatId = {}", chatId);

        if (!roleValidator.checkRoleOwnerOrAdmin(chatId)) return;

        if(!checkUsernameInDB(chatId, username)) return;
        User user = userService.findByUsername(username).get();

        if (user.getRole() == role) {
            botSender.sendMessage(chatId, "Цей користувач вже має таку роль");
            log.info("The user already has the role you wanted to assign to him in chatId = {}", chatId);
            return;
        }

        if (userService.updateUserRole(username, role)) {
            botSender.sendMessage(chatId, "Роль користувача успішно змінена");
            log.info("User role successfully changed in chatId = {}", chatId);
            return;
        }
        botSender.sendMessage(chatId, "Щось пішло не так");
        log.error("Some error occurred  in chatId = {}", chatId);
    }

    @Override
    public void handleRoleSelection(CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();
        if (data.startsWith("change_role:")) {
            String[] parts = data.split(":");
            if (parts.length == 3) {
                String username = parts[1];
                Role selectedRole = Role.valueOf(parts[2]);
                changeRole(callbackQuery.getMessage().getChatId(), username, selectedRole);
            }
        }
    }

    @Override
    public void sendForAllUsers(Message msg) {
        Long chatId = msg.getChatId();
        log.info("Called the command to send text or file to all users of the bot, in chatId = {}", chatId);
        if (!roleValidator.checkRoleOwnerOrAdmin(chatId)) return;

        List<User> users = userService.findAll();  // Get all users from DB
        boolean flag = true;
        for (User user : users) {
            if(!botSender.sendAll(user.getChatId(), msg)) {
                botSender.sendMessage(chatId, "Невідомий тип файлу, який неможливо надіслати всім користувачам");
                flag = false;
                break;
            }
        }
        if(flag) botSender.sendMessage(chatId, "Повідомлення успішно надіслано всім користувачам боту");
        userStateService.clearCommandState(chatId);  // Reset state after processing
    }

    @Override
    public void sendForUsername(Message msg, String username) {
        Long chatId = msg.getChatId();
        log.info("Called the command to send text or file to user of the bot, in chatId = {}", chatId);
        if (!roleValidator.checkRoleOwnerOrAdmin(chatId)) return;

        if(!checkUsernameInDB(chatId, username)) return;
        Long chatIdUser = userService.findByUsername(username).get().getChatId();

        if(botSender.sendAll(chatIdUser, msg)) {
            botSender.sendMessage(chatId, "Повідомлення успішно надіслано користувачу " + username);
        } else {
            botSender.sendMessage(chatId, "Помилка у надсиланні повідомлення користувачу " + username + " невідомий тип файлу");
        }
        userStateService.clearCommandState(chatId);  // Reset state after processing
    }

    private boolean checkUsernameInDB(long chatId, String username) {
        Optional<User> userOpt = userService.findByUsername(username);
        if (userOpt.isEmpty()) {
            botSender.sendMessage(chatId, "Користувача з таким юзернеймом не має в базі");
            log.error("The user with this username does not exist in the database in chatId = {}", chatId);
            return false;
        }
        return true;
    }
}
