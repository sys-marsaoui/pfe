package com.example.hiring.entities;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "posts")
@Data
@AllArgsConstructor
public class Post {
    @Id
    private String id;
    private String title;
    private String description;
    private String contractType; // CDI, CDD, Stage
    private String quizId; // ID du questionnaire associé
    private String status;
    private LocalDate expireDate;
    private int nbrOpenPost;
    private List<Candidature> condidates;
    private List<String> responsabilites = new ArrayList<>();
    public Post() {
        this.condidates = new ArrayList<>();
        this.responsabilites = new ArrayList<>();
    }
}
