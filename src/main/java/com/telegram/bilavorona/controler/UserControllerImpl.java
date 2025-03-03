package com.telegram.bilavorona.controler;

import com.telegram.bilavorona.config.MyBotSender;
import com.telegram.bilavorona.model.Role;
import com.telegram.bilavorona.model.User;
import com.telegram.bilavorona.service.UserService;
import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Slf4j
@Component
public class UserControllerImpl implements UserController {
    private final UserService userService;
    private final RoleController roleController;
    private final MyBotSender botSender;

    @Autowired
    public UserControllerImpl(UserService userService, RoleController roleController, MyBotSender botSender) {
        this.userService = userService;
        this.roleController = roleController;
        this.botSender = botSender;
    }

    @Override
    public void saveUser(Message msg) {
        log.info("Called the command to save the user in chatId = {}", msg.getChatId());
        userService.saveUser(msg);
    }

    @Override
    public void getAllUsers(Message msg) {
        Long chatId = msg.getChatId();
        log.info("Called the command to get all the users in chatId = {}", chatId);
        if (!roleController.checkRole(chatId, new Role[]{Role.OWNER, Role.ADMIN})) return;

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
            if(count == 10 || i == allUsers.size() - 1) {
                botSender.sendMessage(chatId, builder.toString());
                count = 0;
                builder = new StringBuilder();
            }
        }
    }


    @Override
    public void deleteUser(Message msg, String username) {
        Long chatId = msg.getChatId();
        log.info("Called the command to delete the user role by username (delete_user) in chatId = {}", chatId);

        if (!roleController.checkRole(chatId, new Role[]{Role.OWNER, Role.ADMIN})) return;
        username = username.startsWith("@") ? username.substring(1) : username;
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
    public void changeRole(Message msg, String username, Role role) {
        Long chatId = msg.getChatId();
        log.info("Called the command to change the user role by username (change_role) in chatId = {}", chatId);

        if (!roleController.checkRole(chatId, new Role[]{Role.OWNER, Role.ADMIN})) return;

        Optional<User> userOpt = userService.findByUsername(username);
        if (userOpt.isEmpty()) {
            botSender.sendMessage(chatId, "Користувача з таким юзернеймом не має в базі");
            log.error("The user with this username does not exist in the database in chatId = {}", chatId);
            return;
        }

        User user = userOpt.get();
        if (user.getRole() == role) {
            botSender.sendMessage(chatId, "Цей користувач вже має таку роль");
            log.info("The user already has the role you wanted to assign to him in chatId = {}", chatId);
            return;
        }

        if (userService.updateUserRole(username, role)) {
            botSender.sendMessage(chatId, "Роль користувача іспішно змінена");
            log.info("User role successfully changed in chatId = {}", chatId);
            return;
        }
        botSender.sendMessage(chatId, "Щось пішло не так");
        log.error("Some error occurred  in chatId = {}", chatId);
    }

    @Override
    public void showRoleSelectionButtons(Message msg, String username) {
        Long chatId = msg.getChatId();
        log.info("Showing role selection buttons for username = {} in chatId = {}", username, chatId);

        // Create buttons for selecting USER or ADMIN roles
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        InlineKeyboardButton userButton = new InlineKeyboardButton("USER");
        userButton.setCallbackData("change_role:" + username + ":USER");

        InlineKeyboardButton adminButton = new InlineKeyboardButton("ADMIN");
        adminButton.setCallbackData("change_role:" + username + ":ADMIN");

        rows.add(List.of(userButton, adminButton));
        markup.setKeyboard(rows);

        botSender.sendInlineKeyboardMarkupMessage(chatId, "Виберіть нову роль для користувача " + username + ":", markup);
    }

    @Override
    public void handleRoleSelection(CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();
        if (data.startsWith("change_role:")) {
            String[] parts = data.split(":");
            if (parts.length == 3) {
                String username = parts[1];
                Role selectedRole = Role.valueOf(parts[2]);
                changeRole((Message) callbackQuery.getMessage(), username, selectedRole);
            }
        }
    }

    @Override
    public void sendForAllUsers(Message msg) {
        Long chatId = msg.getChatId();
        String text = msg.getText();
        if (!roleController.checkRole(chatId, new Role[]{Role.OWNER, Role.ADMIN})) return;

        List<User> users = userService.findAll();  // Get all users from DB

        if (msg.hasText()) {
            for (User user : users) {
                botSender.sendMessage(user.getChatId(), msg.getText());
            }
        } else if (msg.hasDocument()) {
            for (User user : users) {
                botSender.sendDocument(user.getChatId(), msg.getDocument().getFileId(), msg.getCaption());
            }
        } else if (msg.hasPhoto()) {
            String fileId = msg.getPhoto().get(msg.getPhoto().size() - 1).getFileId();
            for (User user : users) {
                botSender.sendPhoto(user.getChatId(), fileId, msg.getCaption());
            }
        } else if (msg.hasVideo()) {
            for (User user : users) {
                botSender.sendVideo(user.getChatId(), msg.getVideo().getFileId(), msg.getCaption());
            }
        }
    }

    //        String textToSen = EmojiParser.parseToUnicode(text.substring(text.indexOf(" ")));
//        List<User> users = userService.findAll();
//        users.forEach(u -> botSender.sendMessage(u.getChatId(), textToSen));
}
