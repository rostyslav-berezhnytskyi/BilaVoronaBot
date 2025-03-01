package com.telegram.bilavorona.controler;

import com.telegram.bilavorona.config.MyBotSender;
import com.telegram.bilavorona.service.UserService;
import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;


@Slf4j
@Service
public class BotCommandHandlerImpl implements BotCommandHandler {
    private final UserService userService;
    private final MyBotSender botSender;

    @Autowired
    public BotCommandHandlerImpl(UserService userService, MyBotSender botSender) {
        this.userService = userService;
        this.botSender = botSender;
    }

    @Override
    public void start(Message msg) {
        String name = msg.getChat().getFirstName();
        log.info("Invoke /start command for user {} in chatId {}", name, msg.getChatId());
        boolean isNewUser = userService.saveUser(msg);
        String greetingForNewUser = "Вітаю , " + name + ", приємно познайомитись! " + EmojiParser.parseToUnicode(":blush:");
        String greetingForOldUser = "Вітаю , " + name + "! " + EmojiParser.parseToUnicode(":wave:");
        String answer = isNewUser ? greetingForNewUser : greetingForOldUser;
        botSender.sendMessage(msg.getChatId(), answer);
    }

    @Override
    public void help(Message msg) {
        log.info("Invoke /help command. Providing help message for user {} in chatId {}", msg.getChat().getUserName(), msg.getChatId());
        String answer = """
                 Команди:
                
                 /start - вітальне повідомлення
                
                 /help - довідкова інформація
                
                 /get_all_files - отримати всі файли
                
                 /delete_user - видаляє вказаного користувача за його юзернеймом з БД (АДМІН). Приклад /delete_user @username
                 
                 /change_role - Змінює роль вказаного користувача за його юзернеймом (АДМІН). Приклад /change_role @username
                 
                 /send_for_all_user - Відправляє текстове повідомлення яке буде показано всім користувачам боту. 
                 Вказати пілся команди через пробіл текст для відправки. Приклад /send_for_all_user текст воідомлення для всіх користувачів
                 
                 можливість завантажувати файли в БД мають право тільки АДМІНІСТРАТОРИ
                """;
        botSender.sendMessage(msg.getChatId(), answer);
    }

    @Override
    public void defaultCom(Message msg) {
        log.info("Invoke unknown command. Providing default message for user {} in chatId {}", msg.getChat().getUserName(), msg.getChatId());
        String answer = "Невідома команда. Використовуйте /help, щоб побачити доступні команди.";
        botSender.sendMessage(msg.getChatId(), answer);
    }
}
