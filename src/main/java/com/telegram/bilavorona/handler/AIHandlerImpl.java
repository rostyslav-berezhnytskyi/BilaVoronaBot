package com.telegram.bilavorona.handler;

import com.telegram.bilavorona.model.User;
import com.telegram.bilavorona.service.AIChatService;
import com.telegram.bilavorona.service.UserService;
import com.telegram.bilavorona.util.AiMessageLimitValidator;
import com.telegram.bilavorona.util.MyBotSender;
import com.telegram.bilavorona.util.RoleValidator;
import com.telegram.bilavorona.util.TextConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Slf4j
@Service
public class AIHandlerImpl implements AIHandler {
    private final AIChatService aiChatService;
    private final MyBotSender botSender;
    private final RoleValidator roleValidator;
    private final UserService userService;
    private final AiMessageLimitValidator aiMessageLimitValidator;

    @Autowired
    public AIHandlerImpl(AIChatService aiChatService, MyBotSender botSender, RoleValidator roleValidator, UserService userService, AiMessageLimitValidator aiMessageLimitValidator) {
        this.aiChatService = aiChatService;
        this.botSender = botSender;
        this.roleValidator = roleValidator;
        this.userService = userService;
        this.aiMessageLimitValidator = aiMessageLimitValidator;
    }

    public void sendAIResponse(long chatId, String userRequest) {
        if(roleValidator.checkRoleBanned(chatId)) return;

        Optional<User> userOptional = userService.findById(chatId);
        User user = userOptional.get();

        if(roleValidator.checkRoleOwnerOrAdmin(chatId)) {
            if(!aiMessageLimitValidator.checkAIMessageLimit(user, 100)) return;
        }

        if(!aiMessageLimitValidator.checkAIMessageLimit(user, 20)) return;

        String aiResponse = aiChatService.getChatResponse(user, userRequest);
        botSender.sendMessage(chatId, aiResponse);
    }


}
