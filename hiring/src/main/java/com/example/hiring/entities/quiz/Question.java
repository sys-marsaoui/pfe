package com.example.hiring.entities.quiz;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class Question {
    private String id = UUID.randomUUID().toString();
    private String text;
    private List<AnswerOption> options;
    private int points;
    private int time;
    private String technology;

}
