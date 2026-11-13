package com.example.hiring.controllers;

import com.example.hiring.entities.quiz.Quiz;
import com.example.hiring.entities.quiz.QuizAnswer;
import com.example.hiring.entities.quiz.Technology;
import com.example.hiring.entities.quiz.UserAnswer;
import com.example.hiring.services.QuizService;
import com.example.hiring.services.TechnologyServiceImpl;
import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RequestMapping("/api/technologies")
@RestController
@RequiredArgsConstructor
public class TechnologyController {

    private final TechnologyServiceImpl technologyService;

    // 1. Obtenir tous les technologies
    @GetMapping
    public List<Technology> getAll(@RequestParam(name = "search", required = false) String search) {
        return technologyService.getAll(search);
    }

    // . Créer un nouveau technology
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public Technology createQuiz(@RequestBody Technology technology) {
        return technologyService.create(technology);
    }

}
