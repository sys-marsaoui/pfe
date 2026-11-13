package com.example.hiring.entities.quiz;

import lombok.Data;

import java.util.UUID;

@Data
public class AnswerOption {
    private String id = UUID.randomUUID().toString();
    private String text;
    private boolean isCorrect;
}
