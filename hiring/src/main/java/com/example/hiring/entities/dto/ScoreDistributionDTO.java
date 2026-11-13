package com.example.hiring.entities.dto;

public record ScoreDistributionDTO(
        String range, // "0-20%", "20-40%", etc.
        Integer quizCount,
        Integer atsCount,
        Integer acceptedCount
) {}
