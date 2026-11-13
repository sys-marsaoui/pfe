package com.example.hiring.controllers;

import com.example.hiring.entities.Token;
import com.example.hiring.entities.Users;
import com.example.hiring.entities.dto.*;
import com.example.hiring.repositories.TokenRepository;
import com.example.hiring.repositories.UserRepository;
import com.example.hiring.services.EmailServiceImpl;
import com.example.hiring.services.JwtService;
import com.example.hiring.services.UserService;
import com.example.hiring.services.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Map;


@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    private final AuthenticationService service;
    @Autowired
    private EmailServiceImpl emailService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private TokenRepository tokenRepository;

    public final String url = "http://localhost:4200/nouveau_mdp";

    public AuthController(AuthenticationService service) {
        this.service = service;
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(service.authenticate(request));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthenticationResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        Token tokenEntity = tokenRepository.findByToken(request.getRefreshToken());
        if (tokenEntity == null || tokenEntity.isRevoked() || tokenEntity.getRefreshTokenExpiry().before(new Date())) {
            return ResponseEntity.badRequest().body(new AuthenticationResponse(null, null, null, null, "400"));
        }
        Users user = tokenEntity.getUser();
        Map<String, String> tokens = jwtService.generateTokens(user, user);
        tokenEntity.setAccessToken(tokens.get("accessToken"));
        tokenEntity.setRefreshToken(tokens.get("refreshToken"));
        tokenEntity.setAccessTokenExpiry(new Date(System.currentTimeMillis() + 1000 * 60 * 60));
        tokenEntity.setRefreshTokenExpiry(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 7));
        tokenRepository.save(tokenEntity);
        return ResponseEntity.ok(new AuthenticationResponse(tokens.get("accessToken"), tokens.get("refreshToken"), user.getFirstName(), user.getLastName(), "200"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Response> forgotPassword(@RequestBody EmailRequest emailRequest) {
        try {
            if (emailRequest.getEmail() == null || emailRequest.getEmail().isEmpty()) {
                return ResponseEntity.badRequest().body(new Response("400", "Email is required", null));
            }
            userService.sendPasswordResetEmail(emailRequest.getEmail());
            return ResponseEntity.ok(new Response("200", "mail send", null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new Response("400", e.getMessage(), null));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Response> resetPassword(@RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(new Response("200", "Mot de passe modifié", null));
    }

    @GetMapping("/test-user/{email}")
    public ResponseEntity<String> testUser(@PathVariable String email) {
        Users user1 = userRepository.findByEmail(email);
        Users user2 = userRepository.findByEmailUser(email);
        return ResponseEntity.ok("findByEmail: " + (user1 != null ? user1.getEmail() : "null") +
                ", findByEmailUser: " + (user2 != null ? user2.getEmail() : "null"));
    }
}