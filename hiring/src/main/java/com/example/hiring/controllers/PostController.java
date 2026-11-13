package com.example.hiring.controllers;

import com.example.hiring.entities.Post;
import com.example.hiring.entities.UserPostStat;
import com.example.hiring.entities.Users;
import com.example.hiring.entities.quiz.UserAnswer;
import com.example.hiring.services.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/posts")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping
    public ResponseEntity<List<Post>> getAllPosts() {
        return ResponseEntity.ok(postService.getAllPosts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Post> getPostById(@PathVariable String id) {
        try {
            return ResponseEntity.ok(postService.getPostById(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/{id}/stat")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserPostStat>> getPostByIdPost(@PathVariable String id) {
        try {
            return ResponseEntity.ok(postService.getStat(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/{id}/stat/{user}")
    public ResponseEntity<UserAnswer> getPostByIdPostAndUserId(
            @PathVariable String id,
            @PathVariable Integer user,
            @AuthenticationPrincipal Users currentUser) {
        if (!currentUser.getId().equals(user)
                && currentUser.getAuthorities().stream().noneMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            return ResponseEntity.ok(postService.getStat(id, user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Post> createPost(@RequestBody Post post) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(postService.createPost(post));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Post> updatePost(@PathVariable String id, @RequestBody Post post) {
        try {
            return ResponseEntity.ok(postService.updatePost(id, post));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deletePost(@PathVariable String id) {
        try {
            postService.deletePost(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    @GetMapping("/{id}/user-stats")
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserPostStat> getUserStats(@PathVariable String id) {
        return postService.getStat(id);
    }

    // Get aggregated post statistics
    @GetMapping("/{id}/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> getPostStatistics(@PathVariable String id) {
        return postService.getPostStatistics(id);
    }
}
