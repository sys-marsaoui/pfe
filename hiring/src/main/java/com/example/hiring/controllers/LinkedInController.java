package com.example.hiring.controllers;

import com.example.hiring.services.LinkedInSharingService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@RestController
@RequestMapping("/api/integrations/linkedin")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class LinkedInController {
    private final LinkedInSharingService linkedInSharingService;

    @Value("${app.linkedin.frontend-url:http://localhost:4200}")
    private String frontendUrl;

    @PostMapping("/offers/{postId}/authorize")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, String> authorizeOfferShare(@PathVariable String postId) {
        return Map.of("authorizationUrl", linkedInSharingService.createAuthorizationUrl(postId));
    }

    @GetMapping("/callback")
    public RedirectView callback(@RequestParam(required = false) String code,
                                 @RequestParam(required = false) String state,
                                 @RequestParam(required = false) String error) {
        String result = "error";
        if (error == null && code != null && state != null) {
            try {
                linkedInSharingService.completeAuthorizationAndPublish(code, state);
                result = "published";
            } catch (Exception e) {
                result = "error";
            }
        }
        return new RedirectView(UriComponentsBuilder.fromUriString(frontendUrl.replaceAll("/$", "") + "/postes")
                .queryParam("linkedin", result).build().encode().toUriString());
    }
}
