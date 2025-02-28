package com.telegram.bilavorona.controler;

import com.telegram.bilavorona.config.BotConfig;
import com.telegram.bilavorona.config.MyBotSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.meta.generics.LongPollingBot;

@Slf4j
@Component
public class BilaVoronaBot implements LongPollingBot {
    private final BotConfig config;
    private final BotCommandHandler botCommandHandler;
    private final MyBotSender botSender;

    @Autowired
    public BilaVoronaBot(BotConfig config, BotCommandHandler botCommandHandler, MyBotSender botSender) {
        this.config = config;
        this.botCommandHandler = botCommandHandler;
        this.botSender = botSender;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Message msg = update.getMessage();
            String messageText = msg.getText();
            SendMessage answerMessage;
            String[] commandParts = messageText.split(" ");

            switch (commandParts[0]) {
                case "/start" -> answerMessage = botCommandHandler.start(msg);
                case "/help" -> answerMessage = botCommandHandler.help(msg);
                case "/deleteUser" -> {
                    if (commandParts.length > 1) {
                        answerMessage = botCommandHandler.deleteUser(msg, commandParts[1]);
                    } else {
                        answerMessage = botCommandHandler.setMessage(msg.getChatId(), "Будь ласка вкажіть юзернейм. Приклад: /deleteUser @username");
                    }
                }
                case "/saveFile" -> {
                    if (msg.hasDocument()) {
                        // Check if the message contains a file (document)
                        answerMessage = botCommandHandler.saveFile(msg);
                    } else {
                        // Ask the user to send a file
                        answerMessage = botCommandHandler.setMessage(msg.getChatId(), "Будь ласка, надішліть документ для збереження.");
                    }
                }
                default -> answerMessage = botCommandHandler.defaultCom(msg);
            }
            executeMessage(answerMessage);
        }
    }

    private void executeMessage(BotApiMethod<?> message) {
        try {
            botSender.execute(message);
        } catch (TelegramApiException e) {
            log.error("Failed to execute message: {}", e.getMessage());
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
