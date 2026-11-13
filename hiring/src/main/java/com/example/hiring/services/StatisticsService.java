package com.example.hiring.services;

import com.example.hiring.entities.*;
import com.example.hiring.entities.dto.*;
import com.example.hiring.repositories.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final CandidatureRepository candidatureRepo;
    private final PostRepository postRepo;
    private final UserRepository usersRepo;
    private final CvDataRepository cvDataRepo;
    private final UserProgressRepository progressRepo;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<CandidateStatsDTO> getCandidateStats(StatsFilter filter) {
        List<Candidature> cands = filterCandidatures(filter);

        Set<String> postIds = cands.stream().map(Candidature::getPostId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Integer> userIds = cands.stream().map(Candidature::getUserId).filter(Objects::nonNull).collect(Collectors.toSet());

        Map<String, Post> postMap = postRepo.findAllById(postIds).stream()
                .collect(Collectors.toMap(Post::getId, p -> p));

        Map<Integer, Users> userMap = usersRepo.findAllById(userIds).stream()
                .collect(Collectors.toMap(Users::getId, u -> u));

        Map<Integer, CvData> cvMap = cvDataRepo.findByUserIdIn(new ArrayList<>(userIds)).stream()
                .filter(cv -> cv.getUser() != null)
                .collect(Collectors.toMap(cv -> cv.getUser().getId(), cv -> cv, (left, right) -> left));

        Map<Integer, UserProgress> progressMap = progressRepo.findByUserIdIn(new ArrayList<>(userIds)).stream()
                .filter(up -> up.getUser() != null)
                .collect(Collectors.toMap(up -> up.getUser().getId(), up -> up, (left, right) -> left));

        return cands.stream().map(c -> {
            Users user = userMap.get(c.getUserId());
            Post post = postMap.get(c.getPostId());
            CvData cv = cvMap.get(c.getUserId());
            UserProgress progress = progressMap.get(c.getUserId());

            double quizPct = c.getTotalQuizScore() == 0 ? 0 :
                    (c.getQuizScore() * 100.0 / c.getTotalQuizScore());

            List<String> skills = cv != null && cv.getSkills() != null
                    ? parseJsonList(cv.getSkills())
                    : List.of();

            return new CandidateStatsDTO(
                    String.valueOf(c.getUserId()),
                    user == null ? "Candidat inconnu" : user.getFirstName() + " " + user.getLastName(),
                    user == null ? "" : user.getEmail(),
                    post != null ? post.getTitle() : "Unknown",
                    c.getStatus(),
                    c.getQuizScore(),
                    c.getTotalQuizScore(),
                    quizPct,
                    c.getAtsScore(),
                    c.getDurationSeconds(),
                    c.getTargetAchievement(),
                    progress != null ? progress.getStage() : "UNKNOWN",
                    progress != null ? progress.getPoints() : 0,
                    c.getStartTime(),
                    c.getInterviewDateTime(),
                    cv != null && cv.isIsValidated(),
                    skills
            );
        }).toList();
    }

    public List<JobStatsDTO> getJobStats(StatsFilter filter) {
        List<Post> posts = postRepo.findAll();
        if (filter.postIds() != null && !filter.postIds().isEmpty()) {
            posts = posts.stream().filter(p -> filter.postIds().contains(p.getId())).toList();
        }

        return posts.stream().map(post -> {
            List<Candidature> cands = filterCandidatures(filter).stream()
                    .filter(c -> post.getId().equals(c.getPostId())).toList();
            long accepted = cands.stream().filter(c -> "ACCEPTED".equals(c.getStatus())).count();
            long rejected = cands.stream().filter(c -> "REJECTED".equals(c.getStatus())).count();

            double avgQuiz = cands.stream()
                    .filter(c -> c.getTotalQuizScore() > 0)
                    .mapToDouble(c -> c.getQuizScore() * 100.0 / c.getTotalQuizScore())
                    .average().orElse(0.0);

            double avgAts = cands.stream().mapToDouble(Candidature::getAtsScore).average().orElse(0.0);

            double avgTTH = cands.stream()
                    .filter(c -> c.getInterviewDateTime() != null && c.getStartTime() != null)
                    .mapToDouble(c -> Duration.between(c.getStartTime(), c.getInterviewDateTime()).toDays())
                    .average().orElse(0.0);

            return new JobStatsDTO(
                    post.getId(),
                    post.getTitle(),
                    post.getContractType(),
                    cands.size(),
                    (int) accepted,
                    (int) rejected,
                    cands.isEmpty() ? 0 : (double) accepted / cands.size() * 100,
                    avgQuiz,
                    avgAts,
                    avgTTH,
                    post.getNbrOpenPost()
            );
        }).toList();
    }

    public List<FunnelStatsDTO> getFunnelStats() {
        return getFunnelStats(null);
    }

    public List<FunnelStatsDTO> getFunnelStats(StatsFilter filter) {
        List<Candidature> filteredCandidatures = filter == null
                ? candidatureRepo.findAll()
                : filterCandidatures(filter);
        Set<Integer> filteredUserIds = filteredCandidatures.stream()
                .map(Candidature::getUserId).filter(Objects::nonNull).collect(Collectors.toSet());
        boolean hasFilter = filter != null && (
                (filter.postIds() != null && !filter.postIds().isEmpty()) ||
                (filter.statuses() != null && !filter.statuses().isEmpty()) ||
                filter.fromDate() != null || filter.toDate() != null);
        List<Users> candidates = !hasFilter
                ? usersRepo.findAllCandidates()
                : usersRepo.findAllById(filteredUserIds).stream().toList();
        Map<Integer, String> currentStages = progressRepo.findAll().stream()
                .filter(progress -> progress.getUser() != null)
                .collect(Collectors.toMap(progress -> progress.getUser().getId(), UserProgress::getStage, (left, right) -> left));
        List<String> stages = List.of("REGISTERED", "CV_UPLOADED", "CV_VERIFIED",
                "QUIZ_ACCEPTED", "QUIZ_PASSED", "ENTRETIEN");
        long total = candidates.size();
        List<FunnelStatsDTO> result = new ArrayList<>();
        long prev = 0;

        for (String stage : stages) {
            int milestone = stages.indexOf(stage);
            long count = candidates.stream()
                    .filter(candidate -> stages.indexOf(currentStages.getOrDefault(candidate.getId(), "REGISTERED")) >= milestone)
                    .count();
            double fromPrev = milestone == 0 ? 100 : prev == 0 ? 0 : count * 100.0 / prev;
            double fromTotal = total == 0 ? 0 : count * 100.0 / total;
            result.add(new FunnelStatsDTO(stage, (int) count, fromPrev, fromTotal));
            prev = count;
        }
        return result;
    }

    public List<ScoreDistributionDTO> getScoreDistribution() {
        return getScoreDistribution(null);
    }

    public List<ScoreDistributionDTO> getScoreDistribution(StatsFilter filter) {
        List<Candidature> cands = filter == null ? candidatureRepo.findAll() : filterCandidatures(filter);

        Map<String, Long> quizMap = new HashMap<>();
        Map<String, Long> atsMap = new HashMap<>();
        Map<String, Long> acceptedMap = new HashMap<>();

        for (Candidature c : cands) {
            double quizPct = c.getTotalQuizScore() == 0 ? 0 : c.getQuizScore() * 100.0 / c.getTotalQuizScore();
            String quizRange = bucket(quizPct);
            String atsRange = bucket(c.getAtsScore());

            if (c.getTotalQuizScore() > 0) quizMap.merge(quizRange, 1L, Long::sum);
            if (c.getAtsScore() > 0) atsMap.merge(atsRange, 1L, Long::sum);

            if ("ACCEPTED".equals(c.getStatus()) && c.getTotalQuizScore() > 0) {
                acceptedMap.merge(quizRange + "|" + atsRange, 1L, Long::sum);
            }
        }

        return List.of("0-20%", "20-40%", "40-60%", "60-80%", "80-100%").stream()
                .map(range -> new ScoreDistributionDTO(
                        range,
                        Math.toIntExact(quizMap.getOrDefault(range, 0L)),
                        Math.toIntExact(atsMap.getOrDefault(range, 0L)),
                        Math.toIntExact(acceptedMap.entrySet().stream()
                                .filter(entry -> entry.getKey().startsWith(range + "|"))
                                .mapToLong(Map.Entry::getValue).sum())
                ))
                .toList();
    }

    private String bucket(double value) {
        int b = Math.min(4, Math.max(0, (int) (value / 20)));
        return (b * 20) + "-" + Math.min(100, (b + 1) * 20) + "%";
    }

    private List<Candidature> filterCandidatures(StatsFilter filter) {
        return candidatureRepo.findAll().stream()
                .filter(c -> filter.postIds() == null || filter.postIds().isEmpty() || filter.postIds().contains(c.getPostId()))
                .filter(c -> filter.statuses() == null || filter.statuses().isEmpty() || filter.statuses().contains(c.getStatus()))
                .filter(c -> filter.fromDate() == null || (c.getStartTime() != null && !c.getStartTime().isBefore(filter.fromDate())))
                .filter(c -> filter.toDate() == null || (c.getStartTime() != null && !c.getStartTime().isAfter(filter.toDate())))
                .filter(c -> c.getUserId() != null)
                .toList();
    }

    public List<Candidature> getFilteredCandidatures(StatsFilter filter) {
        return filterCandidatures(filter);
    }

    private List<String> parseJsonList(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }
}
