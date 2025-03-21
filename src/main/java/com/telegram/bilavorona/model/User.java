package com.telegram.bilavorona.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "usersDataTable")
@Getter
@Setter
@NoArgsConstructor
public class User {
    @Id
    private Long chatId;
    private Long telegramId;
    private java.sql.Timestamp registeredAt;
    private String firstName;
    private String lastName;
    private String userName;
    @Enumerated(EnumType.STRING)
    private Role role;
    private String languageCode;
    private String phoneNumber;
    private int discount = 0;    // Default discount is 0%
}
