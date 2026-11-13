package com.example.hiring.entities.dto;

import java.time.LocalDateTime;
import java.util.List;

public record CandidateStatsDTO(
        String candidateId,
        String fullName,
        String email,
        String postTitle,
        String status, // PENDING, ACCEPTED, REJECTED
        Integer quizScore,
        Integer totalQuizScore,
        Double quizPercentage,
        Double atsScore,
        Long durationSeconds,
        Integer targetAchievement,
        String currentStage,
        Integer points,
        LocalDateTime appliedAt,
        LocalDateTime interviewAt,
        Boolean cvValidated,
        List<String> skills
) {}