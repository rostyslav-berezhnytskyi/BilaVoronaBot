package com.telegram.bilavorona.controler;

import com.telegram.bilavorona.service.UserService;
import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

@Slf4j
@Service
public class BotCommandHandlerImpl implements BotCommandHandler {
    private final UserService userService;

    @Autowired
    public BotCommandHandlerImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public SendMessage start(Message msg) {
        String name = msg.getChat().getFirstName();
        log.info("Invoke /start command for user {} in chatId {}", name, msg.getChatId());
        boolean isNewUser = userService.registerUser(msg);
        String greeting = "Hi, " + name + ", nice to meet you! " + EmojiParser.parseToUnicode(":blush:");
        String answer =  isNewUser ? greeting : "Welcome back, " + name + "!";
        return setMessage(msg.getChatId(), answer);
    }

    @Override
    public SendMessage help(Message msg) {
        log.info("Invoke /help command. Providing help message for user {} in chatId {}", msg.getChat().getUserName(), msg.getChatId());
        String answer = """
               This bot demonstrates Java Spring Boot with Telegram API.
               Commands:
               
               /start - Welcome message
               
               /help - Help info
               """;;
        return setMessage(msg.getChatId(), answer);
    }

    @Override
    public SendMessage defaultCom(Message msg) {
        log.info("Invoke unknown command. Providing default message for user {} in chatId {}", msg.getChat().getUserName(), msg.getChatId());
        String answer = "Unknown command. Use /help to see available commands.";
        return setMessage(msg.getChatId(), answer);
    }


    private SendMessage setMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(textToSend);
        return message;
    }
}
