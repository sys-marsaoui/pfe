package com.example.hiring.entities.dto;

import java.time.LocalDateTime;
import java.util.List;

public record StatsFilter(
        List<String> postIds,
        LocalDateTime fromDate,
        LocalDateTime toDate,
        List<String> statuses,
        List<String> levels
) {}
