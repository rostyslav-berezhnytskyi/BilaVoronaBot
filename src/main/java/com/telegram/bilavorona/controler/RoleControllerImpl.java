package com.telegram.bilavorona.controler;

import com.telegram.bilavorona.config.MyBotSender;
import com.telegram.bilavorona.model.Role;
import com.telegram.bilavorona.model.User;
import com.telegram.bilavorona.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
public class RoleControllerImpl implements RoleController {
    private final UserService userService;
    private final MyBotSender botSender;

    public RoleControllerImpl(UserService userService, MyBotSender botSender) {
        this.userService = userService;
        this.botSender = botSender;
    }

    @Override
    public boolean checkRole(long chatId, Role[] roles) {
        Optional<User> user = userService.findById(chatId);
        if (user.isPresent()) {
            for (Role role : roles) {
                if (user.get().getRole() == role) return true;
            }
            return false;
        } else {
            botSender.sendMessage(chatId, "У вас немає дозволу на таку команду");
            log.info("Try to delete user command without ADMIN or OWNER role in chatId = {}", chatId);
            return false;
        }
    }
}
