package com.telegram.bilavorona.util;

import com.telegram.bilavorona.model.Role;

public interface RoleValidator {
    boolean checkRoleOwnerOrAdmin(long chatId);

    boolean checkRoleOwner(long chatId);

    boolean checkRoleBanned(long chatId);
}
