package com.telegram.bilavorona.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserActivity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private LocalDate firstDailyUse;

    @Column(nullable = false)
    private LocalDate firstWeeklyUse;

    @Column(nullable = false)
    private LocalDate firstMonthlyUse;

    public UserActivity(Long userId, LocalDate firstDailyUse, LocalDate firstWeeklyUse, LocalDate firstMonthlyUse) {
        this.userId = userId;
        this.firstDailyUse = firstDailyUse;
        this.firstWeeklyUse = firstWeeklyUse;
        this.firstMonthlyUse = firstMonthlyUse;
    }
}
