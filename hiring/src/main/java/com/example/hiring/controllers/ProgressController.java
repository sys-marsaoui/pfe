package com.example.hiring.controllers;

import com.example.hiring.entities.UserProgress;
import com.example.hiring.entities.Users;
import com.example.hiring.repositories.UserRepository;
import com.example.hiring.repositories.PostRepository;
import com.example.hiring.repositories.CvDataRepository;
import com.example.hiring.repositories.CandidatureRepository;
import com.example.hiring.entities.Post;
import com.example.hiring.entities.CvData;
import com.example.hiring.entities.Candidature;
import com.example.hiring.services.UserProgressService;
import com.example.hiring.services.CandidateHistoryService;
import com.example.hiring.entities.dto.CandidateEventDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.Map;

@RestController
@RequestMapping("/api/progress")
public class ProgressController {

    @Autowired
    private UserProgressService userProgressService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private CvDataRepository cvDataRepository;
    @Autowired
    private CandidatureRepository candidatureRepository;
    @Autowired
    private CandidateHistoryService candidateHistoryService;

    @GetMapping("/history")
    public java.util.List<CandidateEventDto> getCurrentUserHistory() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Users user = userRepository.findByEmail(authentication.getName());
        if (user == null) return java.util.List.of();
        return candidateHistoryService.getForUser(user.getId());
    }

    @GetMapping("/history/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public java.util.List<CandidateEventDto> getCandidateHistory(@PathVariable Integer userId) {
        return candidateHistoryService.getForUser(userId);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> getUserProgress(@PathVariable Integer userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        return userProgressService.getProgressStatus(user);
    }

    @GetMapping("/user")
    public ResponseEntity<Map<String, Object>> getCurrentUserProgress() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() instanceof String) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        String email = authentication.getName();
        Users user = userRepository.findByEmail(email);
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Authenticated user not found"));
        }

        Map<String, Object> progressStatus = userProgressService.getProgressStatus(user);
        progressStatus.put("userId", user.getId());
        progressStatus.put("candidateName", user.getFirstName() + " " + user.getLastName());
        progressStatus.put("postId", user.getPostId());

        CvData cv = cvDataRepository.findByUser(user);
        progressStatus.put("cvExists", cv != null);
        progressStatus.put("cvValidated", cv != null && cv.isIsValidated());

        Post post = user.getPostId() == null ? null : postRepository.findById(user.getPostId()).orElse(null);
        progressStatus.put("postTitle", post == null ? null : post.getTitle());
        progressStatus.put("quizId", post == null ? null : post.getQuizId());

        Candidature candidature = user.getPostId() == null ? null :
                candidatureRepository.findByUserIdAndPostId(user.getId(), user.getPostId()).orElse(null);
        progressStatus.put("applicationStatus", candidature == null ? null : candidature.getStatus());
        progressStatus.put("atsScore", candidature == null ? null : candidature.getAtsScore());
        progressStatus.put("quizScore", candidature == null ? null : candidature.getQuizScore());
        progressStatus.put("totalQuizScore", candidature == null ? null : candidature.getTotalQuizScore());
        progressStatus.put("interviewDateTime", candidature == null ? null : candidature.getInterviewDateTime());
        progressStatus.put("nextAction", getNextAction((String) progressStatus.get("stage"), cv, candidature, post));
        return ResponseEntity.ok(progressStatus);
    }

    private String getNextAction(String stage, CvData cv, Candidature candidature, Post post) {
        if (candidature != null && "REJECTED".equalsIgnoreCase(candidature.getStatus())) {
            return "Cette candidature est clôturée. Vous pouvez consulter les autres offres.";
        }
        if ("ENTRETIEN".equals(stage) && candidature != null && candidature.getInterviewDateTime() != null) {
            return "Préparez votre entretien et consultez la date indiquée ci-dessous.";
        }
        if ("ACCEPTED".equalsIgnoreCase(candidature == null ? null : candidature.getStatus())) {
            return "Votre candidature est retenue. L’équipe de recrutement vous contactera.";
        }
        if ("REJECTED".equalsIgnoreCase(candidature == null ? null : candidature.getStatus())) {
            return "Cette candidature est clôturée. Vous pouvez consulter les autres offres.";
        }
        if ("FAILED".equalsIgnoreCase(candidature == null ? null : candidature.getStatus())) {
            return "Votre résultat est sous le seuil. Consultez votre profil pour connaître les options de reprise.";
        }
        if ("PASSED".equalsIgnoreCase(candidature == null ? null : candidature.getStatus())) {
            return "Quiz réussi. Votre dossier attend la décision de l’équipe de recrutement.";
        }
        if (post == null) return "Consultez les offres et choisissez celle qui vous intéresse.";
        if (cv == null) return "Complétez votre profil ou importez un CV.";
        if (!cv.isIsValidated()) return "Relisez puis validez votre CV depuis votre profil.";
        if ("QUIZ_ACCEPTED".equals(stage)) return "Passez le quiz depuis votre profil.";
        if ("QUIZ_PASSED".equals(stage)) return "Quiz réussi. Votre dossier est en cours d’examen.";
        if ("CV_VERIFIED".equals(stage)) return "Votre CV est validé et attend l’examen de l’équipe.";
        return "Votre candidature est enregistrée et en cours de traitement.";
    }

    @PostMapping("/update/{userId}/{stage}")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> updateUserProgress(@PathVariable Integer userId, @PathVariable String stage) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        UserProgress progress = userProgressService.updateProgress(user, stage);
        return userProgressService.getProgressStatus(user);
    }

    @PostMapping("/reset/{userId}/{stage}")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> resetUserProgress(@PathVariable Integer userId, @PathVariable String stage) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        UserProgress progress = userProgressService.resetProgress(user, stage);
        return userProgressService.getProgressStatus(user);
    }

    @PostMapping("/accept-quiz/{userId}/{postId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> acceptUserForQuiz(@PathVariable Integer userId, @PathVariable String postId) {
        try {
            Users user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
            UserProgress progress = userProgressService.acceptUserForQuiz(user, postId);
            return ResponseEntity.ok(userProgressService.getProgressStatus(user));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }
}
