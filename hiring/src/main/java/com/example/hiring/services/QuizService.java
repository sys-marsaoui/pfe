package com.example.hiring.services;

import com.example.hiring.entities.quiz.Quiz;
import com.example.hiring.entities.quiz.QuizAnswer;
import com.example.hiring.entities.quiz.UserAnswer;

import java.util.List;

public interface QuizService {

    // Récupère tous les quiz
    List<Quiz> getAllQuizzes(List<String> technologies, String level);

    // Récupère un quiz par son ID
    Quiz getQuizById(String id);

    // Crée un nouveau quiz
    Quiz createQuiz(Quiz quiz);

    // Supprime un quiz par son ID
    void deleteQuiz(String id);

    // Met à jour un quiz existant
    Quiz updateQuiz(String id, Quiz quiz);


    UserAnswer submit(QuizAnswer quizAnswer);


}
