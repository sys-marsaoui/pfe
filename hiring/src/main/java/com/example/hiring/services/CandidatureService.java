package com.example.hiring.services;

import com.example.hiring.entities.Candidature;
import com.example.hiring.entities.Post;
import com.example.hiring.entities.UserProgress;
import com.example.hiring.entities.Users;
import com.example.hiring.entities.CvData;
import com.example.hiring.entities.quiz.UserAnswer;
import com.example.hiring.repositories.*;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CandidatureService {

    private final PostRepository postRepository;
    private final CandidatureRepository candidatureRepository;
    private final UserAnswerRepository userAnswerRepository;
    private final UserProgressService userProgressService;
    private final UserRepository userRepository;
    private final EmailServiceImpl emailService;
    private final UserProgressRepository userProgressRepository;
    private final JdbcTemplate jdbcTemplate;
    private final CvDataRepository cvDataRepository;
    private final CandidateHistoryService candidateHistoryService;

    public Candidature postuler(String postId, Users user) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));
        Optional<Candidature> previous = candidatureRepository.findByUserIdAndPostId(user.getId(), postId);
        Candidature candidature = previous
                .orElseGet(() -> Candidature.builder()
                        .user(user)
                        .postId(postId)
                        .status("InProgress")
                        .quizScore(0)
                        .totalQuizScore(0)
                        .atsScore(0.0)
                        .startTime(LocalDateTime.now())
                        .endTime(LocalDateTime.now())
                        .dateOfTest(LocalDate.now())
                        .build());
        candidature.setUser(user);
        candidature.setPostId(postId);
        candidature = candidatureRepository.save(candidature);
        if (post.getCondidates() == null) {
            post.setCondidates(new ArrayList<>());
        }
        Candidature savedCandidature = candidature;
        if (post.getCondidates().stream().noneMatch(existing -> savedCandidature.getId().equals(existing.getId()))) {
            post.getCondidates().add(candidature);
        }
        postRepository.save(post);
        if (previous.isEmpty()) {
            candidateHistoryService.record(user, "APPLICATION_SUBMITTED", "Candidature envoyée pour « " + post.getTitle() + " ».");
            try {
                emailService.sendApplicationReceivedEmail(user, post);
            } catch (Exception e) {
                System.err.println("Application receipt email could not be sent: " + e.getMessage());
            }
        }
        return candidature;
    }

    public Candidature applyWithValidatedCv(String postId, Users user) {
        if (postId == null || postId.isBlank()) {
            throw new IllegalArgumentException("An offer is required");
        }
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));
        if (post.getExpireDate() != null && post.getExpireDate().isBefore(LocalDate.now())) {
            throw new IllegalStateException("This offer has expired");
        }
        CvData cvData = cvDataRepository.findByUser(user);
        if (cvData == null || !cvData.isIsValidated()) {
            throw new IllegalStateException("Complete and validate your CV before applying");
        }
        if (user.getPostId() != null && !user.getPostId().isBlank() && !user.getPostId().equals(postId)) {
            throw new IllegalStateException("Your account is already associated with another offer");
        }
        user.setPostId(postId);
        userRepository.save(user);
        return postuler(postId, user);
    }

    public void createOrUpdateCandidature(Users user, String postId, String stage) {
        System.out.println("[CANDIDATURE] Attempting to create/update for user: " + user.getEmail() + " | Stage: " + stage);

        if (postId == null || postId.isBlank()) {
            System.out.println("[CANDIDATURE FAIL] postId is NULL or blank");
            return;
        }

        Optional<Candidature> existing = candidatureRepository.findByUserIdAndPostId(user.getId(), postId);
        Candidature candidature;

        if (existing.isPresent()) {
            candidature = existing.get();
            System.out.println("[CANDIDATURE UPDATE] Found existing ID: " + candidature.getId());
        } else {
            candidature = new Candidature();
            candidature.setUser(user);
            candidature.setUserId(user.getId());
            candidature.setPostId(postId);
            candidature.setQuizScore(0);
            candidature.setTotalQuizScore(0);
            candidature.setStartTime(LocalDateTime.now());
            candidature.setEndTime(LocalDateTime.now());
            candidature.setDurationSeconds(0);
            candidature.setTargetAchievement(0);
            candidature.setDateOfTest(LocalDate.now());
            System.out.println("[CANDIDATURE CREATE] New for user: " + user.getEmail());
        }

        if ("QUIZ_ACCEPTED".equals(stage)) {
            candidature.setStatus("INVITED");
        } else if ("QUIZ_PASSED".equals(stage)) {
            candidature.setStatus("PASSED");
        } else if ("ENTRETIEN".equals(stage)) {
            candidature.setStatus("INTERVIEW_SCHEDULED");
        }

        try {
            Double atsScore = jdbcTemplate.queryForObject(
                    "SELECT match_score FROM ats_matches WHERE user_id = ? AND post_id = ?",
                    Double.class,
                    user.getId(),
                    postId
            );
            candidature.setAtsScore(atsScore != null ? atsScore : 0.0);
            System.out.println("[ATS SCORE] From MySQL: " + candidature.getAtsScore() + "%");
        } catch (Exception e) {
            candidature.setAtsScore(0.0);
            System.out.println("[ATS SCORE] Not found in MySQL → 0.0");
        }

        try {
            Candidature saved = candidatureRepository.save(candidature);
            System.out.println("[CANDIDATURE SUCCESS] Saved ID: " + saved.getId() +
                    " | Status: " + saved.getStatus() +
                    " | ATS: " + saved.getAtsScore() +
                    " | Stage: " + stage);
        } catch (Exception e) {
            System.out.println("[CANDIDATURE FAIL] Save error: " + e.getMessage());
        }
    }

    public void processQuizResult(UserAnswer userAnswer, Users user, int totalScore, boolean passed) {
        String postId = userAnswer.getPostId();
        if (postId == null || postId.isBlank()) {
            System.out.println("[QUIZ FAIL] postId missing in UserAnswer");
            return;
        }

        int userScore = userAnswer.getScore();
        Optional<Candidature> candOpt = candidatureRepository.findByUserIdAndPostId(user.getId(), postId);
        if (candOpt.isEmpty()) {
            System.out.println("[QUIZ FAIL] No candidature exists for user: " + user.getId() + " | postId: " + postId);
            return;
        }

        Candidature candidature = candOpt.get();
        candidature.setQuizScore(userScore);
        candidature.setTotalQuizScore(totalScore);
        candidature.setStatus(passed ? "PASSED" : "FAILED");
        candidature.setEndTime(userAnswer.getDateDePassage());

        LocalDateTime startTime = candidature.getStartTime();
        LocalDateTime endTime = userAnswer.getDateDePassage();
        if (startTime != null && endTime != null) {
            long seconds = Duration.between(startTime, endTime).getSeconds();
            candidature.setDurationSeconds(seconds);
            System.out.println("[QUIZ DURATION] " + seconds + " seconds");
        }

        boolean resultSaved = false;
        try {
            candidatureRepository.save(candidature);
            resultSaved = true;
            System.out.println("[QUIZ SUCCESS] Updated candidature ID: " + candidature.getId() +
                    " | Score: " + userScore + "/" + totalScore +
                    " | Duration: " + candidature.getDurationSeconds() + "s");
        } catch (Exception e) {
            System.out.println("[QUIZ FAIL] Save error: " + e.getMessage());
        }
        if (resultSaved) {
            candidateHistoryService.record(user, "QUIZ_RESULT",
                    "Résultat du quiz : " + userScore + " / " + totalScore + (passed ? " (réussi)." : " (non réussi)."));
            try {
                String postTitle = postRepository.findById(postId).map(Post::getTitle).orElse("l’offre sélectionnée");
                emailService.sendQuizResultEmail(user, postTitle, userScore, totalScore, passed);
            } catch (Exception e) {
                System.err.println("Quiz result email could not be sent: " + e.getMessage());
            }
        }
    }

    public List<Candidature> getPassedByPost(String postId) {
        List<Users> appliedUsers = userRepository.findByPostId(postId);
        List<Candidature> allCandidatures = candidatureRepository.findByPostId(postId);
        Map<Integer, Candidature> candMap = new HashMap<>();
        allCandidatures.forEach(c -> candMap.put(c.getUserId(), c));
        return appliedUsers.stream()
                .map(user -> candMap.get(user.getId()))
                .filter(Objects::nonNull)
                .toList();
    }

    private int extractTotal(UserAnswer ua) {
        try {
            return Integer.parseInt(ua.getScoreDescription().split(" / ")[1]);
        } catch (Exception e) {
            return 0;
        }
    }

    public Candidature updateStatus(String id, String status, LocalDateTime interviewDateTime) throws MessagingException {
        Candidature c = candidatureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Candidature not found"));

        Users user = c.getUser();
        if (user == null) {
            user = userRepository.findById(c.getUserId()).orElseThrow();
            c.setUser(user);
        }

        c.setStatus(status);
        if ("ACCEPTED".equals(status)) {
            if (!QuizScoring.isPassingScore(c.getQuizScore(), c.getTotalQuizScore())) {
                throw new IllegalStateException("Only candidates who pass the quiz can be scheduled for interview");
            }
            if (interviewDateTime == null) {
                throw new IllegalArgumentException("Interview date and time are required");
            }
            c.setInterviewDateTime(interviewDateTime);
        }

        Candidature saved = candidatureRepository.save(c);
        if ("ACCEPTED".equals(status)) {
            userProgressService.updateProgress(user, "ENTRETIEN");
            try {
                emailService.sendInterviewEmail(user, saved);
            } catch (Exception e) {
                System.err.println("Interview email could not be sent: " + e.getMessage());
            }
        } else if ("REJECTED".equals(status)) {
            try {
                emailService.sendRejectionEmail(user, saved);
            } catch (Exception e) {
                System.err.println("Rejection email could not be sent: " + e.getMessage());
            }
        }
        candidateHistoryService.record(user, status,
                "Décision de candidature : " + ("ACCEPTED".equals(status)
                        ? "entretien planifié le " + interviewDateTime
                        : "candidature refusée."));
        return saved;
    }


    public Map<String, List<Candidature>> getAllPassedByPosts() {
        System.out.println("\n=== ADMIN DASHBOARD – ALL POSTS ===");

        List<Post> allPosts = postRepository.findAll();
        System.out.println("Total posts found: " + allPosts.size());

        Map<String, List<Candidature>> result = new HashMap<>();

        for (Post post : allPosts) {
            String postId = post.getId();
            System.out.println("\n--- Processing Post: " + postId + " ---");

            List<Candidature> candidates = getPassedByPost(postId);

            if (!candidates.isEmpty()) {
                result.put(postId, candidates);
                System.out.println("Candidates for post " + postId + ": " + candidates.size());
            } else {
                System.out.println("No candidates for post " + postId);
            }
        }

        System.out.println("Posts with candidates: " + result.size() + "\n");
        return result;
    }
}
