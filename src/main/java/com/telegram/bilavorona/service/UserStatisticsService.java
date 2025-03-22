package com.telegram.bilavorona.service;

public interface UserStatisticsService {

    void recordUserActivity(Long userId);

    int getDailyUniqueUsers();

    int getWeeklyUniqueUsers();

    int getMonthlyUniqueUsers();
}
