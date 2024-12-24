package com.example.hiring.authModule.controller;

import com.example.hiring.authModule.dto.*;
import com.example.hiring.authModule.service.EmailServiceImpl;
import com.example.hiring.authModule.service.UserService;
import com.example.hiring.authModule.serviceImp.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
public class AuthController {

    private final AuthenticationService service;
    @Autowired
    private EmailServiceImpl emailService;

    @Autowired
    UserService userService;
    public final String url="http://localhost:4200/nouveau_mdp";
    public AuthController(AuthenticationService service) {
        this.service = service;
    }


    @PostMapping("/authenticate")
    @CrossOrigin("*")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody AuthenticationRequest request
    ) {
        return ResponseEntity.ok(service.authenticate(request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Response> forgotPassword(@RequestBody EmailRequest emailRequest) {
        try {
            userService.sendPasswordResetEmail(emailRequest.getEmail());
            return ResponseEntity.ok(new Response("200","mail send",null));
        }catch (Exception e){
            return ResponseEntity.ok(new Response("400","user doesnt exist",null));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Response> resetPassword(@RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(new Response("200","Mot de passe modifié",null));
    }

}
