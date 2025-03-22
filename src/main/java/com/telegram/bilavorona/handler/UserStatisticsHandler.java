package com.telegram.bilavorona.handler;

import java.time.DayOfWeek;
import java.time.LocalDate;

public interface UserStatisticsHandler {

    void recordUserActivity(Long userId);

    void getDailyUniqueUsers(Long userId);

    void getWeeklyUniqueUsers(Long userId);

    void getMonthlyUniqueUsers(Long userId);

    void sendUserStatisticsForDayToAllManagers();

    void sendUserStatisticsForWeekToAllManagers();

    void sendUserStatisticsForMonthToAllManagers();
}
