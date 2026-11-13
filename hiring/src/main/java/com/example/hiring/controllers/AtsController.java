package com.example.hiring.controllers;

import com.example.hiring.entities.AtsMatch;
import com.example.hiring.services.AtsService;
import com.example.hiring.services.AtsService.AtsMatchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/ats")
@CrossOrigin(origins = "http://localhost:4200")
@PreAuthorize("hasRole('ADMIN')")
public class AtsController {

    @Autowired
    private AtsService atsService;

    @GetMapping("/compare/{userId}")
    public ResponseEntity<List<AtsMatchResult>> compareCvToPost(@PathVariable Integer userId) {
        try {
            List<AtsMatchResult> results = atsService.compareCvToPost(userId);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    @GetMapping("/compare/post/{postId}")
    public ResponseEntity<List<AtsMatchResult>> compareCvsToPostByPostId(@PathVariable String postId) {
        try {
            List<AtsMatchResult> results = atsService.compareCvsToPostByPostId(postId);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/results/{postId}")
    public ResponseEntity<List<AtsMatchResult>> getAtsResultsByPostId(@PathVariable String postId) {
        try {
            List<AtsMatchResult> results = atsService.getAtsResultsByPostId(postId);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    @GetMapping
    public ResponseEntity<List<AtsMatch>> getAllAtsMatches() {
        List<AtsMatch> matches = atsService.findAll();
        return ResponseEntity.ok(matches);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AtsMatch> getAtsMatchById(@PathVariable Integer id) {
        Optional<AtsMatch> match = atsService.findById(id);
        return match.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AtsMatch>> getAtsMatchesByUserId(@PathVariable Integer userId) {
        List<AtsMatch> matches = atsService.findByUserId(userId);
        return ResponseEntity.ok(matches);
    }

}
