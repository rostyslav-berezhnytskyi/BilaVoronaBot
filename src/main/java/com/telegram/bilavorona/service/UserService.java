package com.telegram.bilavorona.service;

import com.telegram.bilavorona.model.Role;
import com.telegram.bilavorona.model.User;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;
import java.util.Optional;

public interface UserService {
    boolean saveUser(Message msg);

    List<User> findAll();

    List<User> findAllAdmins();

    Optional<User> findById(Long id);

    Optional<User> findByUsername(String username);

    boolean deleteById(Long id);

    boolean deleteByUsername(String username);

    boolean updateUserRole(String username, Role newRole);

    User saveUser(User user);
}
