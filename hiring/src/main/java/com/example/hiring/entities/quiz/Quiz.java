package com.example.hiring.entities.quiz;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(collection = "quizzes")
public class Quiz {
        @Id
        private String id;
        private String title;
        private List<Question> questions;
        private ELevel level;
}