package com.example.hiring.entities.dto;

public record FunnelStatsDTO(
        String stage,
        Integer count,
        Double percentageFromPrevious,
        Double percentageFromTotal
) {}
