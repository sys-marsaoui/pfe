package com.example.hiring.quizModule.services;

import com.example.hiring.quizModule.entity.QuizModal;
import com.example.hiring.quizModule.repository.QuizRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class QuizServiceImpl implements QuizService {

    private final QuizRepository quizRepository;

    public QuizServiceImpl(QuizRepository quizRepository) {
        this.quizRepository = quizRepository;
    }

    @Override
    public List<QuizModal> getAllQuizzes() {
        return quizRepository.findAll();
    }

    @Override
    public QuizModal getQuizById(String id) {
        return quizRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));
    }

    @Override
    public QuizModal createQuiz(QuizModal quiz) {
        return quizRepository.save(quiz);
    }

    @Override
    public void deleteQuiz(String id) {
        quizRepository.deleteById(id);
    }


    @Override
    public QuizModal updateQuiz(String id, QuizModal quiz) {
        // Vérifier si le quiz existe dans la base de données
        Optional<QuizModal> existingQuizOptional = quizRepository.findById(id);

        if (existingQuizOptional.isPresent()) {
            QuizModal existingQuiz = existingQuizOptional.get();

            // Mettre à jour les propriétés du quiz
            existingQuiz.setTitle(quiz.getTitle());
            existingQuiz.setQuestions(quiz.getQuestions());

            // Enregistrer les modifications
            return quizRepository.save(existingQuiz);
        } else {
            // Si le quiz n'existe pas, lancer une exception
            throw new RuntimeException("Quiz not found with id: " + id);
        }
    }


}
