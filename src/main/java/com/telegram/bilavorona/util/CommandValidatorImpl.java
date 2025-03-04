package com.telegram.bilavorona.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CommandValidatorImpl implements CommandValidator {
    private final MyBotSender botSender;

    @Autowired
    public CommandValidatorImpl(MyBotSender botSender) {
        this.botSender = botSender;
    }

    @Override
    public boolean checkCom(long chatId, String[] commandParts, int size, String errorText) {
        if(commandParts.length < size) {
            botSender.sendMessage(chatId, errorText);
            log.error("Wrong size of {} command in chatId = {}", commandParts[0], chatId);
            return false;
        }
        log.info("Command {} was called in chatId = {}", commandParts[0], chatId);
        return true;
    }
}
