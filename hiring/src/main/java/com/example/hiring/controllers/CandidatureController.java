package com.example.hiring.controllers;

import com.example.hiring.entities.Candidature;
import com.example.hiring.entities.Users;
import com.example.hiring.services.CandidatureService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/candidatures")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class CandidatureController {

    private final CandidatureService candidatureService;

    @PostMapping("/apply/{postId}")
    @PreAuthorize("hasRole('CANDIDATE')")
    @ResponseStatus(HttpStatus.CREATED)
    public Candidature applyWithCv(@PathVariable String postId,
                                  @AuthenticationPrincipal Users currentUser) {
        try {
            return candidatureService.applyWithValidatedCv(postId, currentUser);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/post/{postId}")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Candidature> getPassed(@PathVariable String postId) {
        return candidatureService.getPassedByPost(postId);
    }

    @PatchMapping("/{id}/accept")
    @PreAuthorize("hasRole('ADMIN')")
    public Candidature accept(@PathVariable String id, @RequestBody(required = false) Map<String, LocalDateTime> body) throws MessagingException {
        return candidatureService.updateStatus(id, "ACCEPTED", body != null ? body.get("interviewDateTime") : null);
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public Candidature reject(@PathVariable String id) throws MessagingException {
        return candidatureService.updateStatus(id, "REJECTED", null);
    }
    @GetMapping("/all-posts")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, List<Candidature>> getAllPassedByPosts() {
        return candidatureService.getAllPassedByPosts();
    }
}
