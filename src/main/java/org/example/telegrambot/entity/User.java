package org.example.telegrambot.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Entity
@Table(name = "users")
public class User {
    @Id
    private Long id;

    private String firstName;   // Telegram firstName
    private String lastName;    // Telegram lastName
    private String username;    // Telegram username

    private String formName;    // имя введённое в форме
    private String email;
    private String currentState;

    public User() {}

    public User(Long id, String firstName, String lastName, String username) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.currentState = null;
    }
}
