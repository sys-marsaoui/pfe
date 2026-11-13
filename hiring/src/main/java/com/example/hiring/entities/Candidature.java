// src/main/java/com/example/hiring/entities/Candidature.java
package com.example.hiring.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Document(collection = "candidatures")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Candidature {

    @Id
    private String id;

    private Users user; // Kept for other services

    @Field("userId")
    private Integer userId;

    @Field("postId")
    private String postId;

    private int quizScore;
    private int totalQuizScore;
    private double atsScore; // From AtsService
    private String status;

    private LocalDate dateOfTest;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private LocalDateTime interviewDateTime;

    private long durationSeconds;
    private int targetAchievement;

    public void setUser(Users user) {
        this.user = user;
        this.userId = user != null ? user.getId() : null;
    }

    public Candidature(Users user, String postId, int quizScore, int totalQuizScore,
                       double atsScore, LocalDateTime startTime, LocalDateTime endTime,
                       int targetAchievement) {
        this.user = user;
        this.userId = user.getId();
        this.postId = postId;
        this.quizScore = quizScore;
        this.totalQuizScore = totalQuizScore;
        this.atsScore = atsScore;
        this.status = "PENDING";
        this.dateOfTest = endTime.toLocalDate();
        this.startTime = startTime;
        this.endTime = endTime;
        this.durationSeconds = java.time.Duration.between(startTime, endTime).getSeconds();
        this.targetAchievement = targetAchievement;
    }
}