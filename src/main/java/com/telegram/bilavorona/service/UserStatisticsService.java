package com.telegram.bilavorona.service;

import java.time.LocalDate;

public interface UserStatisticsService {

    void recordUserActivity(Long userId);

    int getDailyUniqueUsers();

    int getWeeklyUniqueUsers();

    int getMonthlyUniqueUsers();

    int countDailyUniqueUsers(LocalDate date);
}
