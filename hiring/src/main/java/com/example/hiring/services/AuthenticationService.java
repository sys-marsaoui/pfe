package com.example.hiring.services;

import com.example.hiring.entities.Token;
import com.example.hiring.entities.Users;
import com.example.hiring.entities.dto.AuthenticationRequest;
import com.example.hiring.entities.dto.AuthenticationResponse;
import com.example.hiring.repositories.TokenRepository;
import com.example.hiring.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

@Service
public class AuthenticationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
            Users user = userRepository.findByEmail(request.getEmail());
            if (user == null) {
                return new AuthenticationResponse(null, null, null, null, "400");
            }
            Map<String, String> tokens = jwtService.generateTokens(user, user);
            tokenRepository.revokeTokensByUserId(user.getId());
            Token tokenEntity = new Token(
                    tokens.get("accessToken"),
                    tokens.get("refreshToken"),
                    user,
                    new Date(System.currentTimeMillis() + 1000 * 60 * 60),
                    new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 7),
                    false
            );
            tokenRepository.save(tokenEntity);
            return new AuthenticationResponse(
                    tokens.get("accessToken"),
                    tokens.get("refreshToken"),
                    user.getFirstName(),
                    user.getLastName(),
                    "200"
            );
        } catch (Exception e) {
            return new AuthenticationResponse(null, null, null, null, "400");
        }
    }
}