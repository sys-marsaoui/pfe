package com.example.hiring.quizModule.contollers;

import com.example.hiring.quizModule.entity.QuizModal;
import com.example.hiring.quizModule.entity.UserAnswer;
import com.example.hiring.quizModule.services.QuizService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/quizzes")
@CrossOrigin(origins = "http://localhost:4200") // Autoriser Angular à accéder
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    // 1. Obtenir tous les quiz
    @GetMapping
    public ResponseEntity<List<QuizModal>> getAllQuizzes() {
        List<QuizModal> quizzes = quizService.getAllQuizzes();
        return ResponseEntity.ok(quizzes);
    }

    // 2. Obtenir un quiz par ID
    @GetMapping("/{id}")
    public ResponseEntity<QuizModal> getQuizById(@PathVariable String id) {
        try {
            QuizModal quiz = quizService.getQuizById(id);
            return ResponseEntity.ok(quiz);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null);
        }
    }

    // 3. Créer un nouveau quiz
    @PostMapping
    public ResponseEntity<QuizModal> createQuiz(@RequestBody QuizModal quiz) {
        QuizModal createdQuiz = quizService.createQuiz(quiz);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdQuiz);
    }

    // 4. Supprimer un quiz par ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuiz(@PathVariable String id) {
        try {
            quizService.deleteQuiz(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // 5. Mettre à jour un quiz par ID
    @PutMapping("/{id}")
    public ResponseEntity<QuizModal> updateQuiz(@PathVariable String id, @RequestBody QuizModal quiz) {
        try {
            QuizModal updatedQuiz = quizService.updateQuiz(id, quiz);
            return ResponseEntity.ok(updatedQuiz);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    // 6. Soumettre les réponses d'un quiz
    @PostMapping("/{id}/submit")
    public ResponseEntity<Map<String, Object>> submitQuizAnswers(
            @PathVariable String id, @RequestBody UserAnswer userAnswer) {

        QuizModal quiz = quizService.getQuizById(id);
        int totalScore = 0;
        int userScore = 0;

        for (QuizModal.Question question : quiz.getQuestions()) {
            totalScore += question.getPoints();
            Object userAnswerValue = userAnswer.getAnswers().get(question.getId());

            if (userAnswerValue != null && isAnswerCorrect(question, userAnswerValue)) {
                userScore += question.getPoints();
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("quizId", id);
        response.put("totalScore", totalScore);
        response.put("userScore", userScore);
        response.put("success", true);

        return ResponseEntity.ok(response);
    }

    private boolean isAnswerCorrect(QuizModal.Question question, Object userAnswer) {
        switch (question.getType()) {
            case "radio":
                // Vérification pour une seule réponse
                return question.getOptions().stream()
                        .anyMatch(option -> option.isCorrect() && option.getId().equals(userAnswer));

            case "checkbox":
                // Vérification pour plusieurs réponses
                if (userAnswer instanceof List<?>) {
                    List<?> userAnswers = (List<?>) userAnswer;
                    return question.getOptions().stream()
                            .filter(QuizModal.Question.Option::isCorrect)
                            .map(QuizModal.Question.Option::getId)
                            .collect(Collectors.toSet())
                            .equals(userAnswers.stream().collect(Collectors.toSet()));
                }
                break;

            case "text":
                // Vérification pour une réponse textuelle
                String correctAnswer = question.getOptions().stream()
                        .filter(QuizModal.Question.Option::isCorrect)
                        .map(QuizModal.Question.Option::getLabel)
                        .findFirst()
                        .orElse("");
                return userAnswer instanceof String && correctAnswer.equalsIgnoreCase((String) userAnswer);

            default:
                return false;
        }
        return false;
    }
}

