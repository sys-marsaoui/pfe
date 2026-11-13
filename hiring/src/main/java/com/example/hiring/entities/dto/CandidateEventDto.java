package com.example.hiring.entities.dto;

import java.time.LocalDateTime;

public record CandidateEventDto(Integer id, String postId, String stage, String description, LocalDateTime createdAt) {}
