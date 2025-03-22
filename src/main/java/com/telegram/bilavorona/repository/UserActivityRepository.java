package com.telegram.bilavorona.repository;

import com.telegram.bilavorona.model.UserActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface UserActivityRepository extends JpaRepository<UserActivity, Long> {
    @Query("SELECT COUNT(DISTINCT ua.userId) FROM UserActivity ua WHERE ua.firstDailyUse = :date")
    int countDailyUniqueUsers(@Param("date") LocalDate date);

    @Query("SELECT COUNT(DISTINCT ua.userId) FROM UserActivity ua WHERE ua.firstWeeklyUse BETWEEN :startOfWeek AND :endOfWeek")
    int countWeeklyUniqueUsers(@Param("startOfWeek") LocalDate startOfWeek, @Param("endOfWeek") LocalDate endOfWeek);

    @Query("SELECT COUNT(DISTINCT ua.userId) FROM UserActivity ua WHERE ua.firstMonthlyUse BETWEEN :startOfMonth AND :endOfMonth")
    int countMonthlyUniqueUsers(@Param("startOfMonth") LocalDate startOfMonth, @Param("endOfMonth") LocalDate endOfMonth);

    Optional<UserActivity> findByUserId(Long userId);
}
