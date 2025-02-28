package com.telegram.bilavorona.controler;

import com.telegram.bilavorona.config.MyBotSender;
import com.telegram.bilavorona.model.Role;
import com.telegram.bilavorona.model.User;
import com.telegram.bilavorona.service.UserService;
import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Optional;

@Slf4j
@Service
public class BotCommandHandlerImpl implements BotCommandHandler {
    private final UserService userService;
    private final FileHandler fileHandler;
    private final MyBotSender botSender;

    @Autowired
    public BotCommandHandlerImpl(UserService userService, FileHandler fileHandler, MyBotSender botSender) {
        this.userService = userService;
        this.fileHandler = fileHandler;
        this.botSender = botSender;
    }

    @Override
    public void start(Message msg) {
        String name = msg.getChat().getFirstName();
        log.info("Invoke /start command for user {} in chatId {}", name, msg.getChatId());
        boolean isNewUser = userService.registerUser(msg);
        String greetingForNewUser = "Вітаю , " + name + ", приємно познайомитись! " + EmojiParser.parseToUnicode(":blush:");
        String greetingForOldUser = "Вітаю , " + name + "! " + EmojiParser.parseToUnicode(":wave:");
        String answer =  isNewUser ? greetingForNewUser : greetingForOldUser;
        sendMessage(msg.getChatId(), answer);
    }

    @Override
    public void help(Message msg) {
        log.info("Invoke /help command. Providing help message for user {} in chatId {}", msg.getChat().getUserName(), msg.getChatId());
        String answer = """
                Команди:
               
                /start - вітальне повідомлення
               
                /help - довідкова інформація
                
                /get_all_files - отримати всі файли
                
                /delete_user - видаляє вказаного користувача за його юзернеймом з БД (АДМІН)
                
                можливість завантажувати файли в БД мають право тільки АДМІНІСТРАТОРИ
               """;;
        sendMessage(msg.getChatId(), answer);
    }

    @Override
    public void defaultCom(Message msg) {
        log.info("Invoke unknown command. Providing default message for user {} in chatId {}", msg.getChat().getUserName(), msg.getChatId());
        String answer = "Невідома команда. Використовуйте /help, щоб побачити доступні команди.";
        sendMessage(msg.getChatId(), answer);
    }

    public void deleteUser(Message msg, String username) {
        username = username.startsWith("@") ? username.substring(1) : username;
        Long chatId = msg.getChatId();
        String answer;
        if(checkRole(chatId, new Role[]{Role.OWNER})) {
            if(userService.deleteByUsername(username)) {
                answer = "Користувача з юзернеймом " + username + " видалено з БД";
            } else {
                answer = "Не вдалось видалити користувача з БД";
            }
        } else {
             answer = "У вас немає дозволу на таку команду";
        }
        sendMessage(chatId, answer);
    }

    @Override
    public void sendMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        executeMessage(message);
    }

    @Override
    public void saveFile(Message msg) {
        if(checkRole(msg.getChatId(), new Role[]{Role.OWNER, Role.ADMIN})) {
            fileHandler.saveFile(msg);
        } else {
            sendMessage(msg.getChatId(), "У вас немає дозволу для виконання цієї операції");
        }
    }

    @Override
    public void getAllFiles(Message msg) {
        fileHandler.getAllFiles(msg);
    }

    private boolean checkRole(long chatId, Role[] roles) {
        Optional<User> user = userService.findById(chatId);
        if(user.isPresent()) {
            for(Role role: roles) {
                if(user.get().getRole() == role) return true;
            }
            return false;
        } else {
            return false;
        }
    }


    private void executeMessage(BotApiMethod<?> message) {
        try {
            botSender.execute(message);
        } catch (TelegramApiException e) {
            log.error("Failed to execute message: {}", e.getMessage());
        }
    }
}
