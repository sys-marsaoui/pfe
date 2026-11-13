package com.example.hiring.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_progress")
public class UserProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @Column(name = "stage")
    private String stage; // e.g., "REGISTERED", "CV_UPLOADED", "CV_VERIFIED", "QUIZ_ACCEPTED", "QUIZ_PASSED", "ENTRETIEN"

    @Column(name = "points")
    private int points; // Total points earned based on stage

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public UserProgress() {
        this.updatedAt = LocalDateTime.now();
    }

    public UserProgress(Users user, String stage, int points) {
        this.user = user;
        this.stage = stage;
        this.points = points;
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Users getUser() { return user; }
    public void setUser(Users user) { this.user = user; }
    public String getStage() { return stage; }
    public void setStage(String stage) { this.stage = stage; }
    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}