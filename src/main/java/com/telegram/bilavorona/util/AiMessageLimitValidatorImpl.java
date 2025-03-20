package com.telegram.bilavorona.util;

import com.telegram.bilavorona.model.User;
import com.telegram.bilavorona.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

@Service
public class AiMessageLimitValidatorImpl implements AiMessageLimitValidator {
    private final UserService userService;
    private final MyBotSender botSender;

    @Autowired
    public AiMessageLimitValidatorImpl(UserService userService, MyBotSender botSender) {
        this.userService = userService;
        this.botSender = botSender;
    }

    @Override
    public boolean checkAIMessageLimit(User user, int limit) {
        Long chatId = user.getChatId();

        userService.updateAiMessageCount(chatId);

        if(user.getAiMessageCount() > limit && user.getFirstAiMessageDate().equals(LocalDate.now())) {
            botSender.sendMessage(chatId, TextConstants.AI_BOT_LIMIT_MESSAGE + "\n" +
                    "⏳ До оновлення ліміту залишилося: " + getTimeRemainingUntilMidnight());
            return false;
        }

        return true;
    }

    private static String getTimeRemainingUntilMidnight() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime midnight = now.toLocalDate().atTime(LocalTime.MIDNIGHT).plusDays(1);
        long hours = ChronoUnit.HOURS.between(now, midnight);
        long minutes = ChronoUnit.MINUTES.between(now, midnight) % 60;
        return String.format("%d год. %d хв.", hours, minutes);
    }
}
