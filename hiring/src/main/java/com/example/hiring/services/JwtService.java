package com.example.hiring.services;

import com.example.hiring.entities.Users;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;


@Service
public class JwtService {
    @Value("${app.jwtSecret}")
    private String secretKey;
    private static final long ACCESS_TOKEN_VALIDITY = 1000 * 60 * 60; // 1 hour
    private static final long REFRESH_TOKEN_VALIDITY = 1000 * 60 * 60 * 24 * 7; // 7 days

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public Map<String, String> generateTokens(UserDetails userDetails, Users user) {
        Map<String, Object> claims = new HashMap<>();
        Map<String, Object> userClaims = new HashMap<>();
        userClaims.put("id", user.getId());
        userClaims.put("email", user.getEmail());
        userClaims.put("firstName", user.getFirstName());
        userClaims.put("lastName", user.getLastName());
        userClaims.put("role", user.getRole().getName());
        userClaims.put("postId", user.getPostId() != null ? user.getPostId() : "");
        claims.put("user", userClaims);
        String accessToken = generateToken(claims, userDetails, ACCESS_TOKEN_VALIDITY);
        String refreshToken = generateToken(new HashMap<>(), userDetails, REFRESH_TOKEN_VALIDITY);
        return new HashMap<>(Map.of("accessToken", accessToken, "refreshToken", refreshToken));
    }

    private String generateToken(Map<String, Object> claims, UserDetails userDetails, long validity) {
        return Jwts
                .builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + validity))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
