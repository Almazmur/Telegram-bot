package org.example.telegrambot.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "survey_responses")
public class SurveyResponse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String name;
    private String email;
    private Integer rating;
    private LocalDateTime createdAt;

    public SurveyResponse() {}

    public SurveyResponse(User user, String name, String email, Integer rating) {
        this.user = user;
        this.name = name;
        this.email = email;
        this.rating = rating;
        this.createdAt = LocalDateTime.now();
    }
}
