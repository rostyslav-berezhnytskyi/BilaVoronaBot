package com.telegram.bilavorona.service;

import com.telegram.bilavorona.model.Role;
import com.telegram.bilavorona.model.User;
import com.telegram.bilavorona.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class UserServiceImpl implements UserService{
    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean saveUser(Message msg) {
        if(userRepository.findById(msg.getChatId()).isEmpty()) {
            String languageCode = msg.getFrom().getLanguageCode();
            Long chatId = msg.getChatId();
            Chat chat = msg.getChat();

            User user = new User();
            user.setTelegramId(msg.getFrom().getId());
            user.setChatId(chatId);
            user.setFirstName(chat.getFirstName());
            user.setLastName(chat.getLastName());
            user.setUserName(chat.getUserName());
            user.setRole(Role.USER);
            user.setLanguageCode(languageCode);
            user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));

            userRepository.save(user);
            return true;
        }
        return false;
    }

    @Override
    public User saveUser(User user) {
        return userRepository.save(user);
    }


    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    public List<User> findAllAdmins() {
        return userRepository.findAllAdmins();
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        username = username.startsWith("@") ? username.substring(1) : username;
        return userRepository.findByUserName(username);
    }

    @Override
    public boolean deleteById(Long id) {
        userRepository.deleteById(id);
        return true;
    }

    @Override
    public boolean deleteByUsername(String username) {
        Optional<User> userOpt = userRepository.findByUserName(username);
        if(userOpt.isPresent()) {
            Long chatId = userOpt.get().getChatId();
            userRepository.deleteById(chatId);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean updateUserRole(String username, Role newRole) {
        Optional<User> userOpt = userRepository.findByUserName(username);
        if(userOpt.isPresent()) {
            User user = userOpt.get();
            user.setRole(newRole);
            userRepository.save(user);
            return true;
        } else {
            return false;
        }
    }
}
