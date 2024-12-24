package com.example.hiring.authModule.controller;


import com.example.hiring.authModule.dto.AddUserDto;
import com.example.hiring.authModule.dto.Response;
import com.example.hiring.authModule.entity.Users;
import com.example.hiring.authModule.service.UserService;
import com.example.hiring.authModule.service.EmailServiceImpl;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {
    @Autowired
    UserService userService;

    @Autowired
    EmailServiceImpl emailService;


@PostMapping("/add_user")
@CrossOrigin("*")
    public ResponseEntity<Response> addUser(@RequestBody AddUserDto userDto){
    try{
        if(userService.getUserByEmail(userDto.getEmail())==null){
        userService.createUser(userDto);
        return ResponseEntity.ok(new Response("200","utilisateur Crée"));
        }else {
            return ResponseEntity.ok(new Response("400","utilisateur existe"));
        }
    }catch (Exception e){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(HttpStatus.BAD_REQUEST.toString(),"utilisateur déjà désactivé"));
    }
}

    @PostMapping("/post/{postId}/register")
    public ResponseEntity<Users> registerCandidate(@PathVariable String postId, @RequestBody Users users) {
        Users registeredCandidate = userService.registerCandidateForPost(postId, users);
        return ResponseEntity.ok(registeredCandidate);
    }

}
