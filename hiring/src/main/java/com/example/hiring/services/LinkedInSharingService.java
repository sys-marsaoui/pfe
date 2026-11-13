package com.example.hiring.services;

import com.example.hiring.entities.Post;
import com.example.hiring.repositories.PostRepository;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class LinkedInSharingService {
    private static final String AUTHORIZE_URL = "https://www.linkedin.com/oauth/v2/authorization";
    private static final String TOKEN_URL = "https://www.linkedin.com/oauth/v2/accessToken";
    private static final String USERINFO_URL = "https://api.linkedin.com/v2/userinfo";
    private static final String POSTS_URL = "https://api.linkedin.com/rest/posts";
    private static final long STATE_LIFETIME_SECONDS = 600;

    private final PostRepository postRepository;
    private final RestClient restClient = RestClient.create();
    private final ConcurrentHashMap<String, PendingShare> pendingShares = new ConcurrentHashMap<>();

    @Value("${app.linkedin.client-id:}") private String clientId;
    @Value("${app.linkedin.client-secret:}") private String clientSecret;
    @Value("${app.linkedin.redirect-uri:}") private String redirectUri;
    @Value("${app.linkedin.frontend-url:http://localhost:4200}") private String frontendUrl;
    @Value("${app.linkedin.api-version:202609}") private String apiVersion;

    public String createAuthorizationUrl(String postId) {
        requireConfiguration();
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Offer not found"));
        if (post.getExpireDate() != null && post.getExpireDate().isBefore(java.time.LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Expired offers cannot be shared");
        }
        pendingShares.entrySet().removeIf(entry -> Instant.now()
                .minusSeconds(STATE_LIFETIME_SECONDS).isAfter(entry.getValue().createdAt()));
        String state = UUID.randomUUID().toString() + UUID.randomUUID();
        pendingShares.put(state, new PendingShare(postId, Instant.now()));
        return UriComponentsBuilder.fromUriString(AUTHORIZE_URL)
                .queryParam("response_type", "code")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("state", state)
                .queryParam("scope", "openid profile w_member_social")
                .build().encode().toUriString();
    }

    public void completeAuthorizationAndPublish(String code, String state) {
        PendingShare pending = pendingShares.remove(state);
        if (pending == null || Instant.now().minusSeconds(STATE_LIFETIME_SECONDS).isAfter(pending.createdAt())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "LinkedIn authorization state is invalid or expired");
        }
        requireConfiguration();

        LinkedMultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("code", code);
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("redirect_uri", redirectUri);
        JsonNode token = restClient.post().uri(TOKEN_URL)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve().body(JsonNode.class);
        String accessToken = token == null ? "" : token.path("access_token").asText();
        if (accessToken.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "LinkedIn did not return an access token");
        }

        JsonNode profile = restClient.get().uri(USERINFO_URL)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve().body(JsonNode.class);
        String memberId = profile == null ? "" : profile.path("sub").asText();
        if (memberId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "LinkedIn did not return the member identifier");
        }
        publishOffer(pending.postId(), memberId, accessToken);
    }

    private void publishOffer(String postId, String memberId, String accessToken) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Offer not found"));
        if (post.getExpireDate() != null && post.getExpireDate().isBefore(java.time.LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This offer expired before it could be shared");
        }
        String offerUrl = frontendUrl.replaceAll("/$", "") + "/detaille-poste/" + post.getId();
        String description = post.getDescription() == null ? "" : post.getDescription().trim();
        if (description.length() > 900) description = description.substring(0, 900) + "…";
        String commentary = "Nous recrutons : " + post.getTitle() + "\n\n" + description +
                "\n\nType de contrat : " + (post.getContractType() == null ? "Non précisé" : post.getContractType()) +
                "\n\nConsultez l'offre et postulez ici : " + offerUrl;

        Map<String, Object> body = Map.of(
                "author", "urn:li:person:" + memberId,
                "commentary", commentary,
                "visibility", "PUBLIC",
                "distribution", Map.of(
                        "feedDistribution", "MAIN_FEED",
                        "targetEntities", new Object[0],
                        "thirdPartyDistributionChannels", new Object[0]),
                "lifecycleState", "PUBLISHED",
                "isReshareDisabledByAuthor", false
        );
        restClient.post().uri(POSTS_URL)
                .header("Authorization", "Bearer " + accessToken)
                .header("Linkedin-Version", apiVersion)
                .header("X-Restli-Protocol-Version", "2.0.0")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve().toBodilessEntity();
    }

    private void requireConfiguration() {
        if (clientId == null || clientId.isBlank() || clientSecret == null || clientSecret.isBlank()
                || redirectUri == null || redirectUri.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "LinkedIn is not configured. Set LINKEDIN_CLIENT_ID, LINKEDIN_CLIENT_SECRET and LINKEDIN_REDIRECT_URI.");
        }
    }

    private record PendingShare(String postId, Instant createdAt) {}
}
