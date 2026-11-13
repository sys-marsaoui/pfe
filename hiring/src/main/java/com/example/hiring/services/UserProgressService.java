package com.example.hiring.services;

import com.example.hiring.entities.UserProgress;
import com.example.hiring.entities.Users;
import com.example.hiring.entities.Post;
import com.example.hiring.repositories.CandidatureRepository;
import com.example.hiring.repositories.UserProgressRepository;
import com.example.hiring.repositories.PostRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserProgressService {

    @Autowired
    private UserProgressRepository userProgressRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private JavaMailSender mailSender;
    @Lazy
    @Autowired
    private CandidatureService candidatureService;

    @Autowired
    private CandidateHistoryService candidateHistoryService;

    private static final Map<String, Integer> STAGE_POINTS = new HashMap<>() {{
        put("REGISTERED", 20);
        put("CV_UPLOADED", 40);
        put("CV_VERIFIED", 60);
        put("QUIZ_ACCEPTED", 70);
        put("QUIZ_PASSED", 85);
        put("ENTRETIEN", 100);
    }};

    public UserProgress getOrCreateProgress(Users user) {
        return userProgressRepository.findByUser(user)
                .orElseGet(() -> {
                    UserProgress progress = new UserProgress(user, "REGISTERED", STAGE_POINTS.get("REGISTERED"));
                    return userProgressRepository.save(progress);
                });
    }

    public UserProgress updateProgress(Users user, String newStage) {
        UserProgress progress = getOrCreateProgress(user);
        String oldStage = progress.getStage();

        if (!isValidTransition(oldStage, newStage)) {
            System.out.println("[PROGRESS FAIL] Invalid transition: " + oldStage + " → " + newStage);
            return progress;
        }

        progress.setStage(newStage);
        progress.setPoints(STAGE_POINTS.getOrDefault(newStage, progress.getPoints()));
        progress.setUpdatedAt(LocalDateTime.now());
        userProgressRepository.save(progress);
        candidateHistoryService.record(user, newStage, stageDescription(newStage));

        System.out.println("[PROGRESS SUCCESS] " + user.getEmail() + " → " + newStage);

        if ("QUIZ_ACCEPTED".equals(newStage) || "QUIZ_PASSED".equals(newStage)) {
            String postId = user.getPostId();
            if (postId != null && !postId.isBlank()) {
                candidatureService.createOrUpdateCandidature(user, postId, newStage);
            } else {
                System.out.println("[CANDIDATURE FAIL] user.postId is NULL for user: " + user.getId());
            }
        }

        return progress;
    }

    private boolean isValidTransition(String current, String next) {
        if (current == null) current = "REGISTERED";

        return switch (current) {
            case "REGISTERED" -> "CV_UPLOADED".equals(next);
            case "CV_UPLOADED" -> "CV_VERIFIED".equals(next);
            case "CV_VERIFIED" -> "QUIZ_ACCEPTED".equals(next);
            case "QUIZ_ACCEPTED" -> "QUIZ_PASSED".equals(next);
            case "QUIZ_PASSED" -> "ENTRETIEN".equals(next);
            default -> false;
        };
    }

    private String stageDescription(String stage) {
        return switch (stage) {
            case "CV_UPLOADED" -> "Le candidat a ajouté son CV.";
            case "CV_VERIFIED" -> "Le CV du candidat a été vérifié.";
            case "QUIZ_ACCEPTED" -> "Le candidat a été invité au quiz.";
            case "QUIZ_PASSED" -> "Le candidat a réussi le quiz.";
            case "ENTRETIEN" -> "Un entretien a été planifié pour le candidat.";
            default -> "Étape de candidature mise à jour : " + stage + ".";
        };
    }

    public Map<String, Object> getProgressStatus(Users user) {
        UserProgress progress = getOrCreateProgress(user);
        Map<String, Object> status = new HashMap<>();
        status.put("stage", progress.getStage());
        status.put("points", progress.getPoints());
        status.put("percentage", (double) progress.getPoints());
        status.put("updatedAt", progress.getUpdatedAt());
        return status;
    }

    public UserProgress resetProgress(Users user, String newStage) {
        UserProgress progress = getOrCreateProgress(user);
        if (STAGE_POINTS.containsKey(newStage)) {
            progress.setStage(newStage);
            progress.setPoints(STAGE_POINTS.get(newStage));
            progress.setUpdatedAt(LocalDateTime.now());
            return userProgressRepository.save(progress);
        }
        return progress;
    }

    public UserProgress acceptUserForQuiz(Users user, String postId) throws MessagingException {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));

        if (user.getPostId() == null || !user.getPostId().equals(postId)) {
            throw new IllegalArgumentException("Candidate is not registered for this post");
        }
        UserProgress currentProgress = getOrCreateProgress(user);
        if (!"CV_VERIFIED".equals(currentProgress.getStage())
                && !"QUIZ_ACCEPTED".equals(currentProgress.getStage())) {
            throw new IllegalStateException("The candidate's CV must be verified before quiz acceptance");
        }

        UserProgress progress = "QUIZ_ACCEPTED".equals(currentProgress.getStage())
                ? currentProgress
                : updateProgress(user, "QUIZ_ACCEPTED");
        if (!"QUIZ_ACCEPTED".equals(progress.getStage())) {
            throw new IllegalStateException("Candidate could not be accepted for the quiz");
        }
        try {
            sendQuizAcceptanceEmail(user, post);
        } catch (MessagingException | RuntimeException e) {
            System.err.println("Quiz invitation email could not be sent: " + e.getMessage());
        }
        return progress;
    }

    public boolean isUserAcceptedForQuiz(Users user, String postId) {
        UserProgress progress = getOrCreateProgress(user);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));

        boolean isCandidate = user.getPostId() != null && user.getPostId().equals(postId)
                && post.getCondidates() != null
                && post.getCondidates().stream()
                .anyMatch(c -> c.getUser() != null && c.getUser().getId().equals(user.getId()));

        return isCandidate && "QUIZ_ACCEPTED".equals(progress.getStage());
    }

    private void sendQuizAcceptanceEmail(Users user, Post post) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        String postName = post.getTitle() != null ? post.getTitle() : "the position";
        LocalDate deadline = LocalDate.now().plusDays(3);
        String formattedDeadline = deadline.format(DateTimeFormatter.ofPattern("MMMM d, yyyy"));

        helper.setTo(user.getEmail());
        helper.setFrom("no-reply@company.com");
        helper.setSubject("Invitation to Take Quiz for " + postName);
        helper.setText(
                "<html>" +
                        "<body>" +
                        "<p>Dear " + user.getFirstName() + " " + user.getLastName() + ",</p>" +
                        "<p>We are pleased to inform you that you have been accepted to take the online quiz for the position of <strong>" + postName + "</strong>.</p>" +
                        "<p>Please complete the quiz by <strong>" + formattedDeadline + "</strong> (within 3 days from today).</p>" +
                        "<p>You can access the quiz from your profile.</p>" +
                        "<p>Best regards,<br>The Hiring Team</p>" +
                        "</body>" +
                        "</html>", true);

        mailSender.send(message);
        System.out.println("Quiz acceptance email sent to: " + user.getEmail());
    }

}
