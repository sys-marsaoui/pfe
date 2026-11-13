package com.example.hiring.services;

import com.example.hiring.entities.AtsMatch;
import com.example.hiring.entities.CvData;
import com.example.hiring.entities.Post;
import com.example.hiring.entities.Users;
import com.example.hiring.repositories.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AtsService {

    private final CvDataRepository cvDataRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final AtsMatchRepository atsMatchRepository;
    private final ObjectMapper objectMapper;
    private final JavaMailSender mailSender;
    private final UserProgressService userProgressService;

    private static final double SKILL_WEIGHT      = 0.50;
    private static final double EXPERIENCE_WEIGHT = 0.35;
    private static final double EDUCATION_WEIGHT  = 0.15;
    private static final double MATCH_THRESHOLD   = 30.0;

    // --- Mots vides français + anglais ---
    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "de", "et", "la", "le", "les", "un", "une", "pour", "dans", "avec",
            "sur", "en", "a", "au", "aux", "des", "du", "par", "qui", "que",
            "nous", "vous", "ils", "elles", "est", "sont", "ont", "avoir",
            "recrutons", "obligatoire", "candidat", "doit", "responsabilites",
            "the", "and", "for", "with", "our", "you", "are", "will", "has",
            "have", "this", "that", "from", "your", "its", "but", "not"
    ));

    // --- Synonymes / alias techniques ---
    private static final Map<String, String> TECH_ALIASES = new HashMap<>();
    static {
        TECH_ALIASES.put("springboot",   "spring boot");
        TECH_ALIASES.put("spring-boot",  "spring boot");
        TECH_ALIASES.put("reactjs",      "react");
        TECH_ALIASES.put("react.js",     "react");
        TECH_ALIASES.put("angularjs",    "angular");
        TECH_ALIASES.put("nodejs",       "node");
        TECH_ALIASES.put("node.js",      "node");
        TECH_ALIASES.put("postgresql",   "postgres");
        TECH_ALIASES.put("ci/cd",        "cicd");
        TECH_ALIASES.put("ci-cd",        "cicd");
        TECH_ALIASES.put("devops",       "devops");
        TECH_ALIASES.put("microservice", "microservices");
    }

    // =========================================================
    //  PUBLIC API
    // =========================================================

    public List<AtsMatch> findAll() {
        return atsMatchRepository.findAll();
    }

    public Optional<AtsMatch> findById(Integer id) {
        return atsMatchRepository.findById(id);
    }

    public List<AtsMatch> findByUserId(Integer userId) {
        return atsMatchRepository.findByUserId(userId);
    }

    public List<AtsMatchResult> compareCvToPost(Integer userId) {
        Users candidate = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        String postId = candidate.getPostId();
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found: " + postId));

        List<AtsMatchResult> results = new ArrayList<>();

        Optional<AtsMatch> existing = atsMatchRepository.findByUserIdAndPostId(userId, postId);
        if (existing.isPresent()) {
            results.add(toResult(existing.get()));
        } else {
            CvData cvData = cvDataRepository.findByUser(candidate);
            if (cvData != null && cvData.isIsValidated()) {
                results.add(processAndSave(candidate, cvData, post, postId));
            }
        }

        results.sort((a, b) -> Double.compare(b.getMatchScore(), a.getMatchScore()));
        return results;
    }

    public List<AtsMatchResult> compareCvsToPostByPostId(String postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found: " + postId));
        List<Users> candidates = userRepository.findByPostId(postId);
        List<AtsMatchResult> results = new ArrayList<>();
        int newCount = 0, existingCount = 0;

        for (Users candidate : candidates) {
            Optional<AtsMatch> existing = atsMatchRepository.findByUserIdAndPostId(candidate.getId(), postId);
            if (existing.isPresent()) {
                existingCount++;
                results.add(toResult(existing.get()));
            } else {
                CvData cvData = cvDataRepository.findByUser(candidate);
                if (cvData != null && cvData.isIsValidated()) {
                    newCount++;
                    results.add(processAndSave(candidate, cvData, post, postId));
                }
            }
        }

        System.out.printf("ATS [%s] — Total: %d | New: %d | Existing: %d%n",
                postId, candidates.size(), newCount, existingCount);

        results.sort((a, b) -> Double.compare(b.getMatchScore(), a.getMatchScore()));
        return results;
    }

    public List<AtsMatchResult> getAtsResultsByPostId(String postId) {
        return atsMatchRepository.findByPostId(postId).stream()
                .map(this::toResult)
                .sorted((a, b) -> Double.compare(b.getMatchScore(), a.getMatchScore()))
                .collect(Collectors.toList());
    }

    public void processCvValidation(Users user, CvData cvData) {
        if (cvData == null) return;
        String event = cvData.isIsValidated() ? "CV_VERIFIED" : "CV_UPLOADED";
        userProgressService.updateProgress(user, event);
    }

    // =========================================================
    //  PRIVATE HELPERS
    // =========================================================

    /** Orchestre le calcul + sauvegarde + email pour un candidat. */
    private AtsMatchResult processAndSave(Users candidate, CvData cvData, Post post, String postId) {
        double matchScore             = calculateMatchScore(cvData, post);
        Map<String, Object> details   = getMatchDetails(cvData, post);

        AtsMatch atsMatch = new AtsMatch();
        atsMatch.setUser(candidate);
        atsMatch.setPostId(postId);
        atsMatch.setMatchScore(matchScore);
        try {
            atsMatch.setMatchDetails(objectMapper.writeValueAsString(details));
        } catch (Exception e) {
            atsMatch.setMatchDetails("{\"error\":\"serialization failed\"}");
        }
        atsMatchRepository.save(atsMatch);

        if (matchScore >= MATCH_THRESHOLD) {
            userProgressService.updateProgress(candidate, "QUIZ_ACCEPTED");
            try {
                sendQuizInvitationEmail(candidate, post, matchScore);
            } catch (MessagingException e) {
                System.err.println("Email failed for " + candidate.getEmail() + ": " + e.getMessage());
            }
        }

        return new AtsMatchResult(
                candidate.getId(),
                candidate.getFirstName() + " " + candidate.getLastName(),
                matchScore,
                details
        );
    }

    /** Convertit un AtsMatch sauvegardé en AtsMatchResult. */
    private AtsMatchResult toResult(AtsMatch atsMatch) {
        Map<String, Object> details;
        try {
            details = objectMapper.readValue(
                    atsMatch.getMatchDetails(),
                    new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            details = Map.of("error", "Failed to parse match details");
        }
        return new AtsMatchResult(
                atsMatch.getUser().getId(),
                atsMatch.getUser().getFirstName() + " " + atsMatch.getUser().getLastName(),
                atsMatch.getMatchScore(),
                details
        );
    }

    // =========================================================
    //  SCORING
    // =========================================================

    private double calculateMatchScore(CvData cvData, Post post) {
        double skill = calculateSkillMatch(cvData, post);
        double exp   = calculateExperienceMatch(cvData, post);
        double edu   = calculateEducationMatch(cvData, post);
        double total = (skill * SKILL_WEIGHT) + (exp * EXPERIENCE_WEIGHT) + (edu * EDUCATION_WEIGHT);

        System.out.printf("ATS Score — Skill: %.1f%% | Exp: %.1f%% | Edu: %.1f%% | Total: %.1f%%%n",
                skill, exp, edu, total);
        return total;
    }

    /** FIX: matching bidirectionnel — CV skills vs Post ET Post keywords vs CV skills */
    private double calculateSkillMatch(CvData cvData, Post post) {
        try {
            List<String> rawSkills = objectMapper.readValue(
                    cvData.getSkills(), new TypeReference<List<String>>() {});

            Set<String> cvKeywords   = rawSkills.stream()
                    .flatMap(s -> extractKeywords(s).stream())
                    .collect(Collectors.toSet());

            String postText          = buildPostText(post);
            Set<String> postKeywords = extractKeywords(postText);

            if (postKeywords.isEmpty()) return 0.0;

            // Intersection CV ∩ Post
            long matched = cvKeywords.stream().filter(postKeywords::contains).count();

            // Normaliser sur min(postKeywords, cap) pour éviter dilution
            int cap   = Math.max(postKeywords.size(), 1);
            double score = Math.min(((double) matched / Math.min(cap, 15)) * 100.0, 100.0);

            System.out.printf("Skill Match: %d/%d = %.1f%%%n", matched, Math.min(cap, 15), score);
            return score;
        } catch (Exception e) {
            System.err.println("Skill match error: " + e.getMessage());
            return 0.0;
        }
    }

    private double calculateExperienceMatch(CvData cvData, Post post) {
        try {
            List<Map<String, String>> experiences = objectMapper.readValue(
                    cvData.getExperiences(), new TypeReference<List<Map<String, String>>>() {});

            String postText          = buildPostText(post);
            Set<String> postKeywords = extractKeywords(postText);
            if (postKeywords.isEmpty() || experiences.isEmpty()) return 0.0;

            double totalScore = 0.0;
            for (Map<String, String> exp : experiences) {
                // FIX: combiner title + description + company pour plus de signal
                String expText = String.join(" ",
                        exp.getOrDefault("title", ""),
                        exp.getOrDefault("description", ""),
                        exp.getOrDefault("company", ""));

                Set<String> expKeywords = extractKeywords(expText);
                long matched = expKeywords.stream().filter(postKeywords::contains).count();
                int cap      = Math.min(postKeywords.size(), 15);
                double score = Math.min(((double) matched / cap) * 100.0, 100.0);
                totalScore  += score;
                System.out.printf("  Exp [%s]: %d/%d = %.1f%%%n",
                        exp.getOrDefault("title", "?"), matched, cap, score);
            }

            double avg = totalScore / experiences.size();
            System.out.printf("Avg Exp Score: %.1f%%%n", avg);
            return avg;
        } catch (Exception e) {
            System.err.println("Experience match error: " + e.getMessage());
            return 0.0;
        }
    }

    /**
     * FIX: Si aucun mot-clé éducation dans le post, on accorde un score de base
     * (50%) pour tout diplôme en informatique/ingénierie — évite le score 0 systématique.
     */
    private double calculateEducationMatch(CvData cvData, Post post) {
        try {
            List<Map<String, String>> education = objectMapper.readValue(
                    cvData.getEducation(), new TypeReference<List<Map<String, String>>>() {});
            if (education.isEmpty()) return 0.0;

            String postText    = post.getDescription() != null ? post.getDescription() : "";
            Set<String> eduKeywordsInPost = extractKeywords(postText).stream()
                    .filter(k -> Arrays.asList(
                            "licence", "master", "informatique", "genie",
                            "ingenierie", "logiciel", "bachelor", "engineer",
                            "software", "computer", "science"
                    ).contains(k))
                    .collect(Collectors.toSet());

            // FIX: Si le post ne précise aucun critère éducation → score de base selon diplôme CV
            if (eduKeywordsInPost.isEmpty()) {
                boolean hasRelevantDegree = education.stream()
                        .map(edu -> normalize(edu.getOrDefault("degree", "") + " " +
                                edu.getOrDefault("field", "")))
                        .anyMatch(d -> d.contains("master") || d.contains("ingenieur") ||
                                d.contains("informatique") || d.contains("architecture") ||
                                d.contains("logiciel") || d.contains("gestion"));
                double baseScore = hasRelevantDegree ? 50.0 : 20.0;
                System.out.printf("Edu Match (no post criteria): base score = %.1f%%%n", baseScore);
                return baseScore;
            }

            long matchedDegrees = education.stream()
                    .map(edu -> edu.getOrDefault("degree", "") + " " + edu.getOrDefault("field", ""))
                    .map(this::extractKeywords)
                    .flatMap(Set::stream)
                    .filter(eduKeywordsInPost::contains)
                    .count();

            double score = ((double) matchedDegrees / eduKeywordsInPost.size()) * 100.0;
            System.out.printf("Edu Match: %d/%d = %.1f%%%n",
                    matchedDegrees, eduKeywordsInPost.size(), score);
            return Math.min(score, 100.0);
        } catch (Exception e) {
            System.err.println("Education match error: " + e.getMessage());
            return 0.0;
        }
    }

    // =========================================================
    //  KEYWORD EXTRACTION (FIXED)
    // =========================================================

    /**
     * FIX principal : suppression du i++ qui faisait sauter des mots.
     * Ajout : normalisation Unicode (accents), gestion alias techniques,
     * bigrams construits séparément sans skip.
     */
    private Set<String> extractKeywords(String text) {
        Set<String> keywords = new HashSet<>();
        if (text == null || text.isBlank()) return keywords;

        String[] words = normalize(text).split("[\\s,;|/.()+\\-]+");

        // Unigrams + alias
        for (String word : words) {
            if (word.length() > 2 && !STOP_WORDS.contains(word)) {
                String canonical = TECH_ALIASES.getOrDefault(word, word);
                keywords.add(canonical);
            }
        }

        // Bigrams (sans skip i++) — fenêtre glissante sur tous les mots valides
        List<String> validWords = Arrays.stream(words)
                .filter(w -> w.length() > 2 && !STOP_WORDS.contains(w))
                .map(w -> TECH_ALIASES.getOrDefault(w, w))
                .collect(Collectors.toList());

        for (int i = 0; i < validWords.size() - 1; i++) {
            keywords.add(validWords.get(i) + " " + validWords.get(i + 1));
        }

        return keywords;
    }

    /** Normalise : minuscules + suppression des accents. */
    private String normalize(String text) {
        if (text == null) return "";
        String lower = text.toLowerCase();
        String nfd   = Normalizer.normalize(lower, Normalizer.Form.NFD);
        return nfd.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    /** Construit le texte du poste (description + responsabilités). */
    private String buildPostText(Post post) {
        return (post.getDescription() != null ? post.getDescription() : "") + " " +
                (post.getResponsabilites() != null ? String.join(" ", post.getResponsabilites()) : "");
    }

    // =========================================================
    //  MATCH DETAILS (FIXED)
    // =========================================================

    /**
     * FIX: matchedSkills retourne maintenant l'INTERSECTION réelle
     * CV skills ∩ Post keywords, et non plus tous les keywords du post.
     */
    private Map<String, Object> getMatchDetails(CvData cvData, Post post) {
        Map<String, Object> details = new LinkedHashMap<>();
        try {
            List<String> cvSkills = objectMapper.readValue(
                    cvData.getSkills(), new TypeReference<List<String>>() {});

            Set<String> cvKeywords = cvSkills.stream()
                    .flatMap(s -> extractKeywords(s).stream())
                    .collect(Collectors.toSet());

            Set<String> postKeywords = extractKeywords(buildPostText(post));

            // FIX: vraie intersection
            List<String> matchedSkills = cvKeywords.stream()
                    .filter(postKeywords::contains)
                    .sorted()
                    .collect(Collectors.toList());

            List<String> missingSkills = postKeywords.stream()
                    .filter(k -> !cvKeywords.contains(k) && k.length() > 3)
                    .sorted()
                    .limit(20)
                    .collect(Collectors.toList());

            details.put("cvSkills",      cvSkills);
            details.put("matchedSkills", matchedSkills);
            details.put("missingSkills", missingSkills);
            details.put("experiences",   objectMapper.readValue(
                    cvData.getExperiences(), new TypeReference<List<Map<String, String>>>() {}));
            details.put("education",     objectMapper.readValue(
                    cvData.getEducation(),   new TypeReference<List<Map<String, String>>>() {}));
        } catch (Exception e) {
            details.put("error", "Error parsing CV data: " + e.getMessage());
        }
        return details;
    }

    // =========================================================
    //  EMAIL
    // =========================================================

    private void sendQuizInvitationEmail(Users candidate, Post post, double matchScore)
            throws MessagingException {
        // FIX: expression ternaire redondante corrigée
        String postName = (post.getTitle() != null && !post.getTitle().isBlank())
                ? post.getTitle() : "the position";

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(candidate.getEmail());
        helper.setFrom("no-reply@company.com");
        helper.setSubject("Invitation au Quiz - " + postName);
        helper.setText(buildEmailBody(candidate, postName, matchScore), true);

        mailSender.send(message);
        System.out.println("Quiz invitation sent to: " + candidate.getEmail());
    }

    private String buildEmailBody(Users candidate, String postName, double matchScore) {
        return "<html><body style='font-family:Arial,sans-serif;color:#333'>" +
                "<h2 style='color:#2563eb'>Invitation au Quiz</h2>" +
                "<p>Bonjour <strong>" + candidate.getFirstName() + " " + candidate.getLastName() + "</strong>,</p>" +
                "<p>Votre candidature pour le poste de <strong>" + postName + "</strong> " +
                "a obtenu un score de correspondance de <strong>" +
                String.format("%.1f", matchScore) + "%</strong>.</p>" +
                "<p>Vous etes invite(e) a passer un quiz en ligne pour la suite du processus. " +
                "Veuillez contacter l'equipe RH pour obtenir le lien d'acces.</p>" +
                "<p>Cordialement,<br><strong>L'equipe de recrutement</strong></p>" +
                "</body></html>";
    }

    // =========================================================
    //  INNER CLASS
    // =========================================================

    public static class AtsMatchResult {
        private Integer userId;
        private String candidateName;
        private double matchScore;
        private Map<String, Object> matchDetails;

        public AtsMatchResult(Integer userId, String candidateName,
                              double matchScore, Map<String, Object> matchDetails) {
            this.userId       = userId;
            this.candidateName = candidateName;
            this.matchScore   = matchScore;
            this.matchDetails = matchDetails;
        }

        public Integer getUserId()                       { return userId; }
        public void setUserId(Integer userId)            { this.userId = userId; }
        public String getCandidateName()                 { return candidateName; }
        public void setCandidateName(String n)           { this.candidateName = n; }
        public double getMatchScore()                    { return matchScore; }
        public void setMatchScore(double s)              { this.matchScore = s; }
        public Map<String, Object> getMatchDetails()     { return matchDetails; }
        public void setMatchDetails(Map<String, Object> d){ this.matchDetails = d; }
    }
}
