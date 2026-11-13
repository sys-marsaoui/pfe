package com.example.hiring.controllers;

import com.example.hiring.entities.Post;
import com.example.hiring.entities.quiz.Quiz;
import com.example.hiring.entities.quiz.QuizAnswer;
import com.example.hiring.entities.quiz.UserAnswer;
import com.example.hiring.entities.UserPostStat;
import com.example.hiring.exceptions.RGINotFoundException;
import com.example.hiring.services.QuizService;
import com.example.hiring.services.PostService;
import com.example.hiring.services.UserProgressService;
import com.example.hiring.entities.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/quizzes")
@RestController
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;
    private final PostService postService;
    private final UserProgressService userProgressService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<Quiz> getAllQuizzes(@RequestParam(name = "techno", required = false) List<String> technologies,
                                    @RequestParam(name = "level", required = false) String level) {
        return quizService.getAllQuizzes(technologies, level);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CANDIDATE')")
    public Quiz getQuizById(@PathVariable String id,
                            @RequestParam(required = false) String postId,
                            Authentication authentication) {
        Quiz quiz = quizService.getQuizById(id);
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
        if (!isAdmin) {
            if (postId == null || postId.isBlank()) {
                throw new SecurityException("A post is required to access this quiz");
            }
            Users user = (Users) authentication.getPrincipal();
            Post post = postService.getPostById(postId);
            if (!userProgressService.isUserAcceptedForQuiz(user, postId)
                    || !id.equals(post.getQuizId())) {
                throw new SecurityException("This quiz is not available for this candidate");
            }
        }
        if (!isAdmin && quiz.getQuestions() != null) {
            quiz.getQuestions().forEach(question -> {
                if (question != null && question.getOptions() != null) {
                    question.getOptions().forEach(option -> option.setCorrect(false));
                }
            });
        }
        return quiz;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public Quiz createQuiz(@RequestBody Quiz quiz) {
        return quizService.createQuiz(quiz);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteQuiz(@PathVariable String id) {
        quizService.deleteQuiz(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Quiz updateQuiz(@PathVariable String id, @RequestBody Quiz quiz) {
        return quizService.updateQuiz(id, quiz);
    }

    @PostMapping("/submit")
    @PreAuthorize("hasRole('CANDIDATE')")
    public UserAnswer submitQuizAnswers(@RequestBody QuizAnswer quiz) {
        return quizService.submit(quiz);
    }

    @PostMapping("/assign")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    public void assignQuizToPost(@RequestBody Map<String, String> request) {
        String postId = request.get("postId");
        String quizId = request.get("quizId");
        postService.assignQuizToPost(postId, quizId);
    }

    @GetMapping("/acceptance")
    @PreAuthorize("hasRole('CANDIDATE')")
    public boolean checkQuizAcceptance(@RequestParam String postId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Users user = (Users) authentication.getPrincipal();
        return userProgressService.isUserAcceptedForQuiz(user, postId);
    }



}
