package com.telegram.bilavorona.handler;

import com.telegram.bilavorona.service.ReportService;
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

import java.io.File;
import java.io.IOException;
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
    private final ReportService reportService;

    @Autowired
    public UserHandlerImpl(UserService userService, RoleValidator roleValidator, MyBotSender botSender, CommandValidator comValidator, UserStateService userStateService, ReportService reportService) {
        this.userService = userService;
        this.roleValidator = roleValidator;
        this.botSender = botSender;
        this.comValidator = comValidator;
        this.userStateService = userStateService;
        this.reportService = reportService;
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

        try {
            File reportFile = reportService.generateAllUsersReport();
            if(reportFile != null && reportFile.exists()) {
                botSender.sendDocumentFile(chatId, reportFile, "Файл з усіма користувачами боту");
            } else {
                log.error("Report file with all user doesnt exist when try to use the command to get all the users in chatId = {}", chatId);
                botSender.sendMessage(chatId, "Сталась помилка в отриманні списку всіх користувачів боту");
            }
        } catch (IOException e) {
            log.error("Got exception {} the command to get all the users in chatId = {}", e, chatId);
            botSender.sendMessage(chatId, "Сталась помилка в отриманні списку всіх користувачів боту");
        }
    }

    @Override
    public void getAllAdmins(long chatId) {
        log.info("Called the command to get all the admins in chatId = {}", chatId);
        if (!roleValidator.checkRoleOwnerOrAdmin(chatId)) return;

        List<User> allAdmins = userService.findAllAdmins();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < allAdmins.size(); i++) {
            builder.append(formUserInfo(allAdmins.get(i)));
        }
        botSender.sendMessage(chatId, builder.toString().trim());
    }

    @Override
    public void getAllBanned(long chatId) {
        log.info("Called the command to get all the banned users in chatId = {}", chatId);
        if (!roleValidator.checkRoleOwnerOrAdmin(chatId)) return;

        List<User> allAdmins = userService.findAllBanned();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < allAdmins.size(); i++) {
            builder.append(formUserInfo(allAdmins.get(i)));
        }
        botSender.sendMessage(chatId, builder.toString().trim());
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
        try {
            if (!roleValidator.checkRoleOwnerOrAdmin(chatId)) return;

            if (msg.getText().equals("/exit")) {
                botSender.sendMessage(chatId, "Надсилання повідомлення всім користувачам скасовано");
                return;
            }

            List<User> users = userService.findAll();  // Get all users from DB
            boolean flag = true;
            for (User user : users) {
                if (!botSender.sendAll(user.getChatId(), msg)) {
                    botSender.sendMessage(chatId, "Невідомий тип файлу, який неможливо надіслати всім користувачам");
                    flag = false;
                    break;
                }
            }
            if (flag) botSender.sendMessage(chatId, "Повідомлення успішно надіслано всім користувачам боту");
        } finally {
            userStateService.clearCommandState(chatId);  // Reset state after processing
        }
    }

    @Override
    public void sendForUsername(Message msg, String username) {
        Long chatId = msg.getChatId();
        log.info("Called the command to send text or file to user of the bot, in chatId = {}", chatId);
        try {
            if (!roleValidator.checkRoleOwnerOrAdmin(chatId)) return;

            if (msg.getText().equals("/exit")) {
                botSender.sendMessage(chatId, "Надсилання повідомлення всім користувачам скасовано");
                return;
            }

            if (!checkUsernameInDB(chatId, username)) return;
            Long chatIdUser = userService.findByUsername(username).get().getChatId();

            if (botSender.sendAll(chatIdUser, msg)) {
                botSender.sendMessage(chatId, "Повідомлення успішно надіслано користувачу " + username);
            } else {
                botSender.sendMessage(chatId, "Помилка у надсиланні повідомлення користувачу " + username + " невідомий тип файлу");
            }
        } finally {
            userStateService.clearCommandState(chatId);  // Reset state after processing
        }
    }

    @Override
    public void savePhoneNumber(Message msg) {
        String phoneNumber = msg.getText().trim();
        long chatId = msg.getChatId();

        if (isValidPhoneNumber(phoneNumber)) {
            Optional<User> optionalUser = userService.findById(chatId);
            if (optionalUser.isPresent()) {
                User user = optionalUser.get();
                user.setPhoneNumber(phoneNumber);
                user.setDiscount(5);
                userService.saveUser(user);

                botSender.sendMessage(chatId, "Дякую за наданий номер телефону. Ви отримали знижку у 5%!");
                userStateService.clearCommandState(chatId);
            }
        } else {
            botSender.sendMessage(chatId, "Будь ласка, введіть коректний номер телефону, або скасуйте цю дію скориставшись командою /exit");
        }
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

    private boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return false; // Handle null or empty input
        }
        phoneNumber = phoneNumber.trim(); // Trim leading/trailing whitespace
        return phoneNumber.matches("^(\\+?380|380|38|0)?\\s?(\\d{3})\\s?-?\\s?(\\d{3})\\s?-?\\s?(\\d{2})\\s?-?\\s?(\\d{2})$");
    }

    private String formUserInfo(User user) {
        return  "ID: " + user.getChatId() +
                ", user_name: " + (user.getUserName() == null ? "-" : user.getUserName()) +
                ", first_name: " + (user.getFirstName() == null ? "-" : user.getFirstName()) +
                ", last_name: " + (user.getLastName() == null ? "-" : user.getLastName()) +
                ", role: " + user.getRole() +
                ", phone: " + (user.getPhoneNumber() == null ? "-" : user.getPhoneNumber()) +
                ", discount: " + user.getDiscount() + "%\n\n";
    }
}
