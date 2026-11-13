package com.example.hiring.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "candidate_events")
public class CandidateEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @Column(name = "post_id")
    private String postId;

    @Column(nullable = false)
    private String stage;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public CandidateEvent() {}

    public CandidateEvent(Users user, String postId, String stage, String description) {
        this.user = user;
        this.postId = postId;
        this.stage = stage;
        this.description = description;
        this.createdAt = LocalDateTime.now();
    }

    public Integer getId() { return id; }
    public Users getUser() { return user; }
    public String getPostId() { return postId; }
    public String getStage() { return stage; }
    public String getDescription() { return description; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
