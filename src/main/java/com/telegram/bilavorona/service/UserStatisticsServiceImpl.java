package com.telegram.bilavorona.service;

import com.telegram.bilavorona.model.UserActivity;
import com.telegram.bilavorona.repository.UserActivityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Optional;

@Service
public class UserStatisticsServiceImpl implements UserStatisticsService {
    private final UserActivityRepository userActivityRepository;

    @Autowired
    public UserStatisticsServiceImpl(UserActivityRepository userActivityRepository) {
        this.userActivityRepository = userActivityRepository;
    }

    @Override
    public void recordUserActivity(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
        LocalDate startOfMonth = today.withDayOfMonth(1);

        Optional<UserActivity> userActivityOptional = userActivityRepository.findByUserId(userId);

        if (userActivityOptional.isPresent()) {
            UserActivity userActivity = userActivityOptional.get();
            boolean updated = false;

            if (!userActivity.getFirstDailyUse().equals(today)) {
                userActivity.setFirstDailyUse(today);
                updated = true;
            }
            if (userActivity.getFirstWeeklyUse().isBefore(startOfWeek)) {
                userActivity.setFirstWeeklyUse(today);
                updated = true;
            }
            if (userActivity.getFirstMonthlyUse().isBefore(startOfMonth)) {
                userActivity.setFirstMonthlyUse(today);
                updated = true;
            }

            if (updated) {
                userActivityRepository.save(userActivity);
            }

        } else {
            // First interaction ever
            userActivityRepository.save(new UserActivity(userId, today, today, today));
        }
    }

    @Override
    public int getDailyUniqueUsers() {
        return userActivityRepository.countDailyUniqueUsers(LocalDate.now());
    }

    @Override
    public int getWeeklyUniqueUsers() {
        LocalDate startOfWeek = LocalDate.now().with(DayOfWeek.MONDAY);
        return userActivityRepository.countWeeklyUniqueUsers(startOfWeek, LocalDate.now());
    }

    @Override
    public int getMonthlyUniqueUsers() {
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        return userActivityRepository.countMonthlyUniqueUsers(startOfMonth, LocalDate.now());
    }

    @Override
    public int countDailyUniqueUsers(LocalDate date) {
        return userActivityRepository.countDailyUniqueUsers(date);
    }
}
