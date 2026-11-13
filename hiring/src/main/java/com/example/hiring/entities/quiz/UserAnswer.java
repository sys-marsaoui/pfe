package com.example.hiring.entities.quiz;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document
@AllArgsConstructor
@NoArgsConstructor
public class UserAnswer {
    @Id
    private String id;
    private int userId;
    private QuizAnswer quizAnswer;
    private String postId;
    private int score;
    private String scoreDescription;
    private LocalDateTime dateDePassage;
}
