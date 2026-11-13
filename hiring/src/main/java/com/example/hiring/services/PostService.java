package com.example.hiring.services;

import com.example.hiring.entities.*;
import com.example.hiring.entities.quiz.UserAnswer;
import com.example.hiring.exceptions.RGINotFoundException;
import com.example.hiring.repositories.PostRepository;
import com.example.hiring.repositories.QuizRepository;
import com.example.hiring.repositories.UserAnswerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserAnswerRepository userAnswerRepository;
    private final QuizRepository quizRepository;
    private final UserProgressService userProgressService;

    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    public Post getPostById(String id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));
    }

    public Post createPost(Post post) {
        if (post.getResponsabilites() == null) {
            post.setResponsabilites(new ArrayList<>());
        }
        if (post.getCondidates() == null) {
            post.setCondidates(new ArrayList<>());
        }
        post.setStatus("Open");
        if (post.getExpireDate() == null) {
            post.setExpireDate(LocalDate.now().plusDays(30));
        }
        return postRepository.save(post);
    }

    public Post updatePost(String id, Post post) {
        return postRepository.findById(id).map(existingPost -> {
            existingPost.setTitle(post.getTitle());
            existingPost.setDescription(post.getDescription());
            existingPost.setContractType(post.getContractType());
            existingPost.setQuizId(post.getQuizId());
            existingPost.setStatus(post.getStatus() != null ? post.getStatus() : existingPost.getStatus());
            existingPost.setExpireDate(post.getExpireDate() != null ? post.getExpireDate() : existingPost.getExpireDate());
            existingPost.setNbrOpenPost(post.getNbrOpenPost());
            if (post.getResponsabilites() != null) {
                existingPost.setResponsabilites(post.getResponsabilites());
            }
            return postRepository.save(existingPost);
        }).orElseThrow(() -> new RuntimeException("Post not found with id: " + id));
    }

    public void deletePost(String id) {
        if (postRepository.existsById(id)) {
            postRepository.deleteById(id);
        } else {
            throw new RuntimeException("Post not found with id: " + id);
        }
    }

    public boolean checkPostValidity(String postId) {
        return postRepository.findById(postId).map(post ->
                "CLOSED".equals(post.getStatus()) && LocalDate.now().isAfter(post.getExpireDate())
        ).orElse(false);
    }

    public void assignQuizToPost(String postId, String quizId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));
        quizRepository.findById(quizId)
                .orElseThrow(() -> new RGINotFoundException("Quiz not found with id: " + quizId));
        post.setQuizId(quizId);
        postRepository.save(post);
    }

    public List<UserPostStat> getStat(String postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));
        List<Users> users = post.getCondidates().stream()
                .map(Candidature::getUser)
                .collect(Collectors.toList());
        return users.stream().map(user -> {
            UserAnswer userAnswer = userAnswerRepository.findAllByPostIdAndUserId(postId, user.getId());
            UserProgress progress = userProgressService.getOrCreateProgress(user);
            String name = String.format("%s %s", user.getFirstName(), user.getLastName());
            if (userAnswer != null) {
                String status = QuizScoring.isPassingScore(userAnswer.getScore(), extractTotalScore(userAnswer)) ? "PASSED" : "FAILED";
                return new UserPostStat(name, userAnswer.getScoreDescription(), calculePercentage(userAnswer.getScore(), userAnswer.getScoreDescription()), status, userAnswer.getDateDePassage());
            } else {
                return new UserPostStat(name, "0", 0.0, progress.getStage(), null);
            }
        }).filter(userPostStat -> userPostStat.getName() != null).toList();
    }

    public Map<String, Object> getPostStatistics(String postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));
        List<UserAnswer> answers = userAnswerRepository.findAllByPostId(postId);
        long totalCandidates = post.getCondidates().size();
        long completedQuizzes = answers.size();
        double completionRate = totalCandidates > 0 ? (double) completedQuizzes / totalCandidates * 100 : 0.0;
        double averageScore = answers.stream()
                .mapToInt(UserAnswer::getScore)
                .average()
                .orElse(0.0);
        long passedCandidates = answers.stream()
                .filter(ua -> QuizScoring.isPassingScore(ua.getScore(), extractTotalScore(ua)))
                .count();
        double passRate = completedQuizzes > 0 ? (double) passedCandidates / completedQuizzes * 100 : 0.0;

        return Map.of(
                "totalCandidates", totalCandidates,
                "completedQuizzes", completedQuizzes,
                "completionRate", completionRate,
                "averageScore", averageScore,
                "passedCandidates", passedCandidates,
                "passRate", passRate
        );
    }

    private Double calculePercentage(int score, String description) {
        try {
            return QuizScoring.percentage(score, Integer.parseInt(description.split(" / ")[1].trim()));
        } catch (Exception e) {
            return 0.0;
        }
    }

    private int extractTotalScore(UserAnswer answer) {
        try {
            return Integer.parseInt(answer.getScoreDescription().split(" / ")[1].trim());
        } catch (Exception e) {
            return 0;
        }
    }

    public UserAnswer getStat(String id, int userId) {
        return userAnswerRepository.findAllByPostIdAndUserId(id, userId);
    }

    public List<UserAnswer> findAllByPostId(String postId) {
        return userAnswerRepository.findAllByPostId(postId);
    }
    // PostService.java
    public Optional<Post> findPostByQuizId(String quizId) {
        return postRepository.findByQuizId(quizId).stream().findFirst();
    }
}
