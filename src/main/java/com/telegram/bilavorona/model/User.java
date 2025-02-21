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
    private Boolean embedeJoke;
    private String phoneNumber;
    private java.sql.Timestamp registeredAt;
    private String firstName;
    private String lastName;
    private String userName;
    @Enumerated(EnumType.STRING)
    private Role role;
}
