package com.example.hiring.quizModule.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAnswer {
    private String quizId;
    private Map<String, Object> answers;
}

