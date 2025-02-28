package com.telegram.bilavorona.controler;

import com.telegram.bilavorona.model.Role;
import com.telegram.bilavorona.model.User;
import com.telegram.bilavorona.service.FileService;
import com.telegram.bilavorona.service.UserService;
import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Optional;

@Slf4j
@Service
public class BotCommandHandlerImpl implements BotCommandHandler {
    private final UserService userService;
    private final FileService fileService;

    @Autowired
    public BotCommandHandlerImpl(UserService userService, FileService fileService) {
        this.userService = userService;
        this.fileService = fileService;
    }

    @Override
    public SendMessage start(Message msg) {
        String name = msg.getChat().getFirstName();
        log.info("Invoke /start command for user {} in chatId {}", name, msg.getChatId());
        boolean isNewUser = userService.registerUser(msg);
        String greetingForNewUser = "Вітаю , " + name + ", приємно познайомитись! " + EmojiParser.parseToUnicode(":blush:");
        String greetingForOldUser = "Вітаю , " + name + "! " + EmojiParser.parseToUnicode(":wave:");
        String answer =  isNewUser ? greetingForNewUser : greetingForOldUser;
        return setMessage(msg.getChatId(), answer);
    }

    @Override
    public SendMessage help(Message msg) {
        log.info("Invoke /help command. Providing help message for user {} in chatId {}", msg.getChat().getUserName(), msg.getChatId());
        String answer = """
                Команди:
               
                /start - вітальне повідомлення
               
                /help - довідкова інформація
               """;;
        return setMessage(msg.getChatId(), answer);
    }

    @Override
    public SendMessage defaultCom(Message msg) {
        log.info("Invoke unknown command. Providing default message for user {} in chatId {}", msg.getChat().getUserName(), msg.getChatId());
        String answer = "Невідома команда. Використовуйте /help, щоб побачити доступні команди.";
        return setMessage(msg.getChatId(), answer);
    }

    public SendMessage deleteUser(Message msg, String username) {
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
        return setMessage(chatId, answer);
    }

    @Override
    public SendMessage setMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        return message;
    }

    @Override
    public SendMessage saveFile(Message msg) {
        if (!msg.hasDocument()) {
            return setMessage(msg.getChatId(), "❌ Ви не надіслали документ!");
        }

        Document doc = msg.getDocument();
        String fileId = doc.getFileId();
        String fileName = doc.getFileName();
        Long fileSize = doc.getFileSize();
        String mimeType = doc.getMimeType();
        Long uploadedBy = msg.getFrom().getId();

        try {
            // Отримуємо шлях до файлу
            GetFile getFile = new GetFile();
            getFile.setFileId(fileId);
            File file = botSender.execute(getFile);

            // Завантажуємо файл
            byte[] fileBytes = downloadFile(file.getFilePath());

            // Зберігаємо у БД через FileService
            fileService.saveFile(fileName, mimeType, fileSize, fileBytes, uploadedBy);

            return setMessage(msg.getChatId(), "✅ Файл успішно збережено у базі даних!");
        } catch (TelegramApiException | IOException e) {
            log.error("Помилка завантаження файлу: {}", e.getMessage());
            return setMessage(msg.getChatId(), "❌ Не вдалося зберегти файл.");
        }
    }

    private byte[] downloadFile(String filePath) throws IOException {
        URL fileUrl = new URL("https://api.telegram.org/file/bot" + botConfig.getToken() + "/" + filePath);
        return fileUrl.openStream().readAllBytes();
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
}
