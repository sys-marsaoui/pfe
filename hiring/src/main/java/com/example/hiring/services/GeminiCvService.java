package com.example.hiring.services;

import com.example.hiring.entities.CvData;
import com.example.hiring.entities.Post;
import com.example.hiring.entities.Users;
import com.example.hiring.entities.dto.CvDataDto;
import com.example.hiring.repositories.CvDataRepository;
import com.example.hiring.repositories.PostRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class GeminiCvService {
    private final CvDataRepository cvDataRepository;
    private final PostRepository postRepository;
    private final ObjectMapper objectMapper;
    private final RestClient restClient = RestClient.create();

    @Value("${app.gemini.api-key:}")
    private String apiKey;

    @Value("${app.gemini.model:gemini-3.8-flash}")
    private String model;

    public CvDataDto generateDraft(Users user, String requestedPostId) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Gemini is not configured. Set GEMINI_API_KEY on the backend.");
        }
        CvData data = cvDataRepository.findByUser(user);
        if (data == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Add your personal and professional information before generating a CV.");
        }
        if (data.isIsValidated()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "A validated CV cannot be changed. Contact the administrator if it needs correction.");
        }

        String postId = requestedPostId != null && !requestedPostId.isBlank()
                ? requestedPostId : user.getPostId();
        Post post = postId == null || postId.isBlank() ? null : postRepository.findById(postId).orElse(null);
        String prompt = buildPrompt(data, post);
        try {
            JsonNode request = objectMapper.valueToTree(Map.of(
                    "contents", new Object[]{Map.of("parts", new Object[]{Map.of("text", prompt)})},
                    "generationConfig", Map.of("responseMimeType", "application/json")
            ));
            JsonNode response = restClient.post()
                    .uri("https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent", model)
                    .header("x-goog-api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(JsonNode.class);
            String generatedJson = response.path("candidates").path(0).path("content")
                    .path("parts").path(0).path("text").asText();
            JsonNode generated = objectMapper.readTree(generatedJson);

            data.setName(user.getFirstName() + " " + user.getLastName());
            data.setEmail(user.getEmail());
            data.setPhone(user.getTel());
            data.setAddress(user.getAddress());
            data.setExperiences(asJson(generated.path("experiences"), data.getExperiences()));
            data.setProjects(asJson(generated.path("projects"), data.getProjects()));
            data.setSkills(asJson(generated.path("skills"), data.getSkills()));
            data.setEducation(asJson(generated.path("education"), data.getEducation()));
            data.setIsValidated(false);
            cvDataRepository.save(data);
            return toDto(data);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "CV generation failed. Check the Gemini API key and try again.");
        }
    }

    private String buildPrompt(CvData data, Post post) {
        String target = post == null ? "General professional CV" :
                "Target role: " + post.getTitle() + "\nJob description: " + post.getDescription();
        return "Rewrite this candidate's CV content clearly and professionally. " +
                "Use only facts present in the supplied information; never invent employers, dates, qualifications, skills, " +
                "results, or projects. Keep names, contact details, links and education facts unchanged. " +
                "Tailor wording to the target role only where supported by the candidate's facts. " +
                "Return a JSON object with exactly these keys: experiences, projects, skills, education. " +
                "experiences and projects must be arrays of objects preserving their original keys; skills an array of strings; " +
                "education an array of objects preserving their original keys. Empty input stays empty.\n" + target +
                "\nCandidate facts:\n" + "name: " + data.getName() + "\nemail: " + data.getEmail() +
                "\nphone: " + data.getPhone() + "\naddress: " + data.getAddress() +
                "\nLinkedIn: " + data.getLinkedin() + "\nGitHub: " + data.getGithub() +
                "\nexperiences: " + nullToEmpty(data.getExperiences()) +
                "\nprojects: " + nullToEmpty(data.getProjects()) +
                "\nskills: " + nullToEmpty(data.getSkills()) +
                "\neducation: " + nullToEmpty(data.getEducation());
    }

    private String asJson(JsonNode generated, String fallback) throws Exception {
        return generated == null || generated.isMissingNode() || generated.isNull()
                ? nullToEmpty(fallback) : objectMapper.writeValueAsString(generated);
    }

    private String nullToEmpty(String value) {
        return value == null ? "[]" : value;
    }

    private CvDataDto toDto(CvData data) {
        CvDataDto dto = new CvDataDto();
        dto.setName(data.getName());
        dto.setEmail(data.getEmail());
        dto.setPhone(data.getPhone());
        dto.setAddress(data.getAddress());
        dto.setLinkedin(data.getLinkedin());
        dto.setGithub(data.getGithub());
        dto.setPhotoPath(data.getPhotoPath());
        dto.setCvPath(data.getCvPath());
        dto.setExperiences(data.getExperiences());
        dto.setProjects(data.getProjects());
        dto.setSkills(data.getSkills());
        dto.setEducation(data.getEducation());
        dto.setIsValidated(data.isIsValidated());
        return dto;
    }
}
