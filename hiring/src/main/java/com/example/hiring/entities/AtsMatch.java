package com.example.hiring.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ats_matches")
public class AtsMatch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users user;

    @Column(name = "post_id")
    private String postId;

    @Column(name = "match_score")
    private double matchScore;

    @Column(name = "match_details", columnDefinition = "JSON")
    private String matchDetails;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public AtsMatch() {}

    public AtsMatch(Users user, String postId, double matchScore, String matchDetails) {
        this.user = user;
        this.postId = postId;
        this.matchScore = matchScore;
        this.matchDetails = matchDetails;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Users getUser() { return user; }
    public void setUser(Users user) { this.user = user; }
    public String getPostId() { return postId; }
    public void setPostId(String postId) { this.postId = postId; }
    public double getMatchScore() { return matchScore; }
    public void setMatchScore(double matchScore) { this.matchScore = matchScore; }
    public String getMatchDetails() { return matchDetails; }
    public void setMatchDetails(String matchDetails) { this.matchDetails = matchDetails; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}