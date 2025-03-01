package com.telegram.bilavorona.controler;

import com.telegram.bilavorona.model.Role;

public interface RoleController {
    boolean checkRole(long chatId, Role[] roles);
}
