package com.telegram.bilavorona.util;

import com.telegram.bilavorona.model.Role;
import com.telegram.bilavorona.model.User;
import com.telegram.bilavorona.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
public class RoleValidatorImpl implements RoleValidator {
    private final UserService userService;
    private final MyBotSender botSender;

    private final static Role[] ADMIN_AND_OWNER = new Role[]{Role.ADMIN, Role.OWNER};
    private final static Role[] OWNER = new Role[]{Role.OWNER};
    private final static Role[] BANNED = new Role[]{Role.BANNED};

    public RoleValidatorImpl(UserService userService, MyBotSender botSender) {
        this.userService = userService;
        this.botSender = botSender;
    }

    private boolean checkRoleCustom(long chatId, Role[] roles) {
        Optional<User> user = userService.findById(chatId);
        if (user.isPresent()) {
            for (Role role : roles) {
                if (user.get().getRole() == role) return true;
            }
            botSender.sendMessage(chatId, "У вас немає дозволу на таку команду");
            log.info("Try to delete user command without ADMIN or OWNER role in chatId = {}", chatId);
            return false;
        } else {
            log.info("There is no user with such chatId = {}", chatId);
            return false;
        }
    }

    @Override
    public boolean checkRoleOwnerOrAdmin(long chatId) {
        return checkRoleCustom(chatId, ADMIN_AND_OWNER);
    }

    @Override
    public boolean checkRoleOwner(long chatId) {
        return checkRoleCustom(chatId, OWNER);
    }

    @Override
    public boolean checkRoleBanned(long chatId) {
        return checkRoleCustom(chatId, BANNED);
    }
}
