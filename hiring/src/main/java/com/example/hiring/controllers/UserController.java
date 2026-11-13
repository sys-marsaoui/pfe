package com.example.hiring.controllers;

import com.example.hiring.entities.Users;
import com.example.hiring.entities.dto.AddUserDto;
import com.example.hiring.entities.dto.Response;
import com.example.hiring.services.UserService;
import com.example.hiring.services.EmailServiceImpl;
import com.example.hiring.services.UserProgressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private EmailServiceImpl emailService;

    @Autowired
    private UserProgressService userProgressService;

    @PostMapping("/add_user")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response> addUser(@RequestBody AddUserDto userDto, @RequestParam String post) {
        try {
            if (userService.getUserByEmail(userDto.getEmail()) == null) {
                userService.createUser(userDto, post);
                // Update progress to REGISTERED after user creation
                Users user = userService.getUserByEmail(userDto.getEmail());
                if (user != null) {
                    userProgressService.updateProgress(user, "REGISTERED");
                }
                return ResponseEntity.ok(new Response("200", "utilisateur Crée"));
            } else {
                return ResponseEntity.ok(new Response("400", "utilisateur existe"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(HttpStatus.BAD_REQUEST.toString(), "utilisateur déjà désactivé"));
        }
    }

    @PostMapping("/add_admin")
    public ResponseEntity<Response> addAdmin(@RequestBody AddUserDto userDto) {
        try {
            if (!userService.findAllAdmins().isEmpty()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new Response("403", "Un administrateur existe déjà"));
            }
            if (userService.getUserByEmail(userDto.getEmail()) == null) {
                userService.createAdminUser(userDto);
                // Update progress to REGISTERED for admin (if applicable)
                Users user = userService.getUserByEmail(userDto.getEmail());
                if (user != null) {
                    userProgressService.updateProgress(user, "REGISTERED");
                }
                return ResponseEntity.ok(new Response("200", "Admin utilisateur Crée"));
            } else {
                return ResponseEntity.ok(new Response("400", "utilisateur existe"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(HttpStatus.BAD_REQUEST.toString(), e.getMessage()));
        }
    }

    @PostMapping("/post/{postId}/register")
    public ResponseEntity<?> registerCandidate(@PathVariable String postId, @RequestBody Users users) {
        try {
            Users registeredCandidate = userService.registerCandidateForPost(postId, users);
            // Update progress to REGISTERED after registration
            if (registeredCandidate != null) {
                userProgressService.updateProgress(registeredCandidate, "REGISTERED");
            }
            return ResponseEntity.ok(new Response("200", "Registration successful"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new Response("400", e.getMessage()));
        }
    }

    @GetMapping("/candidates")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Users>> getAllCandidates() {
        List<Users> candidates = userService.findAllCandidates();
        return ResponseEntity.ok(candidates);
    }

    @GetMapping("/admins")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Users>> getAllAdmins() {
        List<Users> admins = userService.findAllAdmins();
        return ResponseEntity.ok(admins);
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Users>> getAllUsers() {
        List<Users> users = userService.findAll();
        return ResponseEntity.ok(users);
    }
    @GetMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Users> getUserById(@PathVariable Integer id) {
        Users user = userService.getUserById(id);
        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
