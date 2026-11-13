package com.example.hiring.services;

import com.example.hiring.entities.Users;
import com.example.hiring.entities.quiz.*;
import com.example.hiring.exceptions.RGINotFoundException;
import com.example.hiring.repositories.QuizRepository;
import com.example.hiring.repositories.UserAnswerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements QuizService {

    private final QuizRepository quizRepository;
    private final UserAnswerRepository userAnswerRepository;
    private final UserProgressService userProgressService;
    private final CandidatureService candidatureService;

    @Override
    public List<Quiz> getAllQuizzes(List<String> technologies, String level) {
        return quizRepository.findAll();
    }

    @Override
    public Quiz getQuizById(String id) {
        return quizRepository.findById(id).orElseThrow(() -> new RGINotFoundException("Quiz not found"));
    }

    @Override
    public Quiz createQuiz(Quiz quiz) {
        return quizRepository.save(quiz);
    }

    @Override
    public void deleteQuiz(String id) {
        getQuizById(id);
        quizRepository.deleteById(id);
    }

    @Override
    public Quiz updateQuiz(String id, Quiz quiz) {
        Quiz existingQuiz = getQuizById(id);
        existingQuiz.setTitle(quiz.getTitle());
        existingQuiz.setQuestions(quiz.getQuestions());
        return quizRepository.save(existingQuiz);
    }

    @Override
    public UserAnswer submit(QuizAnswer quizAnswer) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Users user = (Users) auth.getPrincipal();

        if (!userProgressService.isUserAcceptedForQuiz(user, quizAnswer.getPostId())) {
            throw new SecurityException("Not authorized");
        }
        if (!user.getId().equals(quizAnswer.getUserId())) {
            throw new SecurityException("ID mismatch");
        }
        if (quizAnswer.getPostId() == null || quizAnswer.getPostId().isBlank()) {
            throw new IllegalArgumentException("Post is required to submit quiz answers");
        }

        Quiz quiz = getQuizById(quizAnswer.getQuizId());
        if (quiz.getQuestions() == null || quiz.getQuestions().isEmpty()) {
            throw new IllegalStateException("The quiz has no questions");
        }
        if (quizAnswer.getQuestionAnswers() == null) {
            throw new IllegalArgumentException("Quiz answers are required");
        }
        int totalScore = quiz.getQuestions().stream().mapToInt(Question::getPoints).sum();
        int userScore = extractCorrectAnswer(quiz).stream()
                .filter(pair -> quizAnswer.getQuestionAnswers().stream()
                        .anyMatch(qa -> pair.getFirst().equals(qa.getAnswerIds())))
                .mapToInt(Pair::getSecond).sum();

        UserAnswer userAnswer = userAnswerRepository
                .findByUserIdAndPostId(user.getId(), quizAnswer.getPostId())
                .orElseGet(UserAnswer::new);
        userAnswer.setUserId(user.getId());
        userAnswer.setQuizAnswer(quizAnswer);
        userAnswer.setPostId(quizAnswer.getPostId());
        userAnswer.setScore(userScore);
        userAnswer.setScoreDescription(String.format("%d / %d", userScore, totalScore));
        userAnswer.setDateDePassage(LocalDateTime.now());
        boolean passed = QuizScoring.isPassingScore(userScore, totalScore);
        userAnswer = userAnswerRepository.save(userAnswer);

        if (passed) {
            userProgressService.updateProgress(user, "QUIZ_PASSED");
        }

        candidatureService.processQuizResult(userAnswer, user, totalScore, passed);

        return userAnswer;
    }

    private List<Pair<String, Integer>> extractCorrectAnswer(Quiz quiz) {
        return quiz.getQuestions().stream().map(question -> Pair.of(this.extractCorrectAnswer(question), question.getPoints())).toList();
    }

    private String extractCorrectAnswer(Question question) {
        return question.getOptions().stream().filter(AnswerOption::isCorrect).map(AnswerOption::getId).findFirst().orElse("no Answer");
    }


}
