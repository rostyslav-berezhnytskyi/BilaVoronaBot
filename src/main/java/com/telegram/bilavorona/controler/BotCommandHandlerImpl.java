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
            📋 *Команди:*
            
            🔹 /start - Вітальне повідомлення.
            🔹 /help - Довідкова інформація.
            
            📄 *Робота з файлами:*
            🔹 /get_all_files - Отримати всі файли.
            🔹 /documentation - Отримати документи з розділу Документація.
            🔹 /examples - Отримати приклади виконаних робіт.

            🛠 *Команди для адміністратора:*
            🔹 /delete_user - Видаляє користувача за його юзернеймом. Приклад: `/delete_user @username`
            🔹 /change_role - Змінює роль користувача за його юзернеймом. Приклад: `/change_role @username`
            🔹 /send_for_all_user - Відправляє повідомлення всім користувачам. Приклад: `/send_for_all_user текст повідомлення`
            🔹 /change_file_group_by_id - Змінити групу файлу за його ID. Приклад: `/change_file_group_by_id 123 DOCUMENTATION`
            🔹 /change_file_group_by_name - Змінити групу файлу за його назвою. Приклад: `/change_file_group_by_name file_name.docx EXAMPLES`
            🔹 /change_file_name_by_id - Змінити назву файлу за його ID. Приклад: `/change_file_name_by_id 123 new_name.docx`
            🔹 /change_file_name_by_name - Змінити назву файлу за його поточною назвою. Приклад: `/change_file_name_by_name old_name.docx new_name.docx`
            🔹 /delete_file_by_id - Видалити файл за його ID. Приклад: `/delete_file_by_id 123`
            🔹 /delete_file_by_name - Видалити файл за його назвою. Приклад: `/delete_file_by_name file_name.docx`


            📌 *Примітка:* Завантажувати файли в БД мають право тільки *АДМІНІСТРАТОРИ*.
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
