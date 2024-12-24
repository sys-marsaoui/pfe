package com.example.hiring.quizModule.services;

import com.example.hiring.quizModule.entity.QuizModal;

import java.util.List;

public interface QuizService {

    // Récupère tous les quiz
    List<QuizModal> getAllQuizzes();

    // Récupère un quiz par son ID
    QuizModal getQuizById(String id);

    // Crée un nouveau quiz
    QuizModal createQuiz(QuizModal quiz);

    // Supprime un quiz par son ID
    void deleteQuiz(String id);

    // Met à jour un quiz existant
    QuizModal updateQuiz(String id, QuizModal quiz);


}
