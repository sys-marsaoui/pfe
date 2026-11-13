package com.example.hiring.entities.quiz;

import com.example.hiring.entities.Users;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizAnswer {
    private String quizId;
    private String postId;
    private int userId;
    private List<QuestionAnswer> questionAnswers;
}
