package com.rgi.hiring.accountManagement.service.impl;


import com.rgi.hiring.accountManagement.modals.Token;
import com.rgi.hiring.accountManagement.modals.User;
import com.rgi.hiring.accountManagement.payload.AuthenticationResponse;
import com.rgi.hiring.accountManagement.repository.TokenRepository;
import com.rgi.hiring.accountManagement.repository.UserRepository;
import com.rgi.hiring.accountManagement.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.List;
import java.util.Optional;

@Service
public class AuthenticationService {

    @Autowired
     UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    private final TokenRepository tokenRepository;

    public AuthenticationService(PasswordEncoder passwordEncoder, JwtService jwtService, AuthenticationManager authenticationManager, TokenRepository tokenRepository) {
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.tokenRepository = tokenRepository;
    }

    public AuthenticationResponse register(User request) {

        // check if user already exist. if exist than authenticate the user
//        if (repository.findByUserName(request.getUserName()).isPresent()) {
//            return new AuthenticationResponse(null, null, "User already exist");
//        }

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setUserName(request.getUserName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());


        user = repository.save(user);


//        String jwt = jwtService.generateToken(user);

//        return new AuthenticationResponse(jwt);
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        saveUserToken(accessToken, user);

        return new AuthenticationResponse(accessToken, refreshToken);

    }


    public AuthenticationResponse authenticate(User request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUserName(),
                        request.getPassword()
                )
        );

        User user = repository.findByUserName(request.getUserName()).orElseThrow();
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

       // String token = jwtService.generateToken(user);

        revokeAllTokenByUser(user);
       // saveUserToken(accessToken, refreshToken, user);
        saveUserToken(accessToken, user);
        return new AuthenticationResponse(accessToken, refreshToken);


    }
    private void revokeAllTokenByUser(User user) {
        List<Token> validTokens = tokenRepository.findAllAccessTokensByUser(user.getId());
        if(validTokens.isEmpty()) {
            return;
        }

        validTokens.forEach(t-> {
            t.setLoggedOut(true);
        });

        tokenRepository.saveAll(validTokens);
    }

    private void saveUserToken(String accessToken,  User user  ){ //String refreshToken,) {
        Token token = new Token();
        token.setAccessToken(accessToken);
//        token.setRefreshToken(refreshToken);
        token.setLoggedOut(false);
        token.setUser(user);
        tokenRepository.save(token);
    }


}
