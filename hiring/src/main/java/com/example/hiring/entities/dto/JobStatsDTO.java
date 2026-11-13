package com.example.hiring.entities.dto;

public record JobStatsDTO(
        String postId,
        String title,
        String contractType,
        Integer totalApplicants,
        Integer accepted,
        Integer rejected,
        Double acceptanceRate,
        Double avgQuizPercentage,
        Double avgAtsScore,
        Double avgTimeToHireDays,
        Integer openPositions
) {}
