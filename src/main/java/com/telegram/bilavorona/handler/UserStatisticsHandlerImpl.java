package com.telegram.bilavorona.handler;

import com.telegram.bilavorona.model.User;
import com.telegram.bilavorona.service.UserService;
import com.telegram.bilavorona.service.UserStatisticsService;
import com.telegram.bilavorona.util.MyBotSender;
import com.telegram.bilavorona.util.RoleValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

@Slf4j
@Service
public class UserStatisticsHandlerImpl implements UserStatisticsHandler {
    private final UserStatisticsService userStatisticsService;
    private final RoleValidator roleValidator;
    private final MyBotSender botSender;
    private final UserService userService;

    @Autowired
    public UserStatisticsHandlerImpl(UserStatisticsService userStatisticsService, RoleValidator roleValidator, MyBotSender botSender, UserService userService) {
        this.userStatisticsService = userStatisticsService;
        this.roleValidator = roleValidator;
        this.botSender = botSender;
        this.userService = userService;
    }

    @Override
    public void recordUserActivity(Long userId) {
        userStatisticsService.recordUserActivity(userId);
    }

    @Override
    public void getDailyUniqueUsers(Long userId) {
        if(!roleValidator.checkRoleOwnerOrAdmin(userId)) return;
        botSender.sendMessage(userId, "📊 Кількість користувачів сьогодні: " + userStatisticsService.getDailyUniqueUsers());
    }

    @Override
    public void getWeeklyUniqueUsers(Long userId) {
        if(!roleValidator.checkRoleOwnerOrAdmin(userId)) return;
        botSender.sendMessage(userId, "📊 Кількість користувачів цього тижня: " + userStatisticsService.getWeeklyUniqueUsers());
    }

    @Override
    public void getMonthlyUniqueUsers(Long userId) {
        if(!roleValidator.checkRoleOwnerOrAdmin(userId)) return;
        botSender.sendMessage(userId, "📊 Кількість користувачів цього місяця: " + userStatisticsService.getMonthlyUniqueUsers());
    }

    @Override
    public void sendUserStatisticsForDayToAllManagers() {
        List<User> admins = userService.findAllAdmins();
        for (User manager : admins) {
            try {
                getDailyUniqueUsers(manager.getChatId());
            } catch (Exception e) {
                log.error("Cant send message with user statistic for day to manager with id {}", manager.getChatId());
            }
        }
    }

    @Override
    public void sendUserStatisticsForWeekToAllManagers() {
        List<User> admins = userService.findAllAdmins();
        for (User manager : admins) {
            try {
                getWeeklyUniqueUsers(manager.getChatId());
            } catch (Exception e) {
                log.error("Cant send message with user statistic for week to manager with id {}", manager.getChatId());
            }
        }
    }

    @Override
    public void sendUserStatisticsForMonthToAllManagers() {
        List<User> admins = userService.findAllAdmins();
        for (User manager : admins) {
            try {
                getMonthlyUniqueUsers(manager.getChatId());
            } catch (Exception e) {
                log.error("Cant send message with user statistic for month to manager with id {}", manager.getChatId());
            }
        }
    }
}
