package com.example.hiring.entities;

import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "tokens")
public class Token {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "access_token", nullable = false, columnDefinition = "TEXT")
    private String accessToken;

    @Column(name = "refresh_token", nullable = false, columnDefinition = "TEXT")
    private String refreshToken;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @Column(name = "access_token_expiry")
    private Date accessTokenExpiry;

    @Column(name = "refresh_token_expiry")
    private Date refreshTokenExpiry;

    @Column(name = "revoked")
    private boolean revoked;

    public Token() {}

    public Token(String accessToken, String refreshToken, Users user, Date accessTokenExpiry, Date refreshTokenExpiry, boolean revoked) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.user = user;
        this.accessTokenExpiry = accessTokenExpiry;
        this.refreshTokenExpiry = refreshTokenExpiry;
        this.revoked = revoked;
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    public Users getUser() { return user; }
    public void setUser(Users user) { this.user = user; }
    public Date getAccessTokenExpiry() { return accessTokenExpiry; }
    public void setAccessTokenExpiry(Date accessTokenExpiry) { this.accessTokenExpiry = accessTokenExpiry; }
    public Date getRefreshTokenExpiry() { return refreshTokenExpiry; }
    public void setRefreshTokenExpiry(Date refreshTokenExpiry) { this.refreshTokenExpiry = refreshTokenExpiry; }
    public boolean isRevoked() { return revoked; }
    public void setRevoked(boolean revoked) { this.revoked = revoked; }
}