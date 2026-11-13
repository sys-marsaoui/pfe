package com.example.hiring.controllers;

import com.example.hiring.entities.Candidature;
import com.example.hiring.entities.dto.CandidateStatsDTO;
import com.example.hiring.entities.dto.*;
import com.example.hiring.entities.dto.StatsFilter;
import com.example.hiring.repositories.CandidatureRepository;
import com.example.hiring.services.CandidatureService;
import com.example.hiring.services.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class StatisticsController {

    private final StatisticsService statsService;
    private final CandidatureRepository candidatureRepo;

    @PostMapping("/candidates")
    public List<CandidateStatsDTO> getCandidates(@RequestBody StatsFilter filter) {
        return statsService.getCandidateStats(filter);
    }

    @PostMapping("/jobs")
    public List<JobStatsDTO> getJobStats(@RequestBody StatsFilter filter) {
        return statsService.getJobStats(filter);
    }

    @GetMapping("/funnel")
    public List<FunnelStatsDTO> getFunnel() {
        return statsService.getFunnelStats();
    }

    @PostMapping("/funnel")
    public List<FunnelStatsDTO> getFilteredFunnel(@RequestBody StatsFilter filter) {
        return statsService.getFunnelStats(filter);
    }

    @GetMapping("/distribution")
    public List<ScoreDistributionDTO> getDistribution() {
        return statsService.getScoreDistribution();
    }

    @PostMapping("/distribution")
    public List<ScoreDistributionDTO> getFilteredDistribution(@RequestBody StatsFilter filter) {
        return statsService.getScoreDistribution(filter);
    }

    @GetMapping("/kpis")
    public Map<String, Object> getKPIs() {
        return calculateKpis(candidatureRepo.findAll());
    }

    @PostMapping("/kpis")
    public Map<String, Object> getFilteredKPIs(@RequestBody StatsFilter filter) {
        return calculateKpis(statsService.getFilteredCandidatures(filter));
    }

    private Map<String, Object> calculateKpis(List<Candidature> all) {
        long accepted = all.stream().filter(c -> "ACCEPTED".equals(c.getStatus())).count();
        long rejected = all.stream().filter(c -> "REJECTED".equals(c.getStatus())).count();
        long pending = all.size() - accepted - rejected;
        return Map.of(
                "total", all.size(),
                "accepted", accepted,
                "rejected", rejected,
                "pending", pending,
                "acceptanceRate", all.isEmpty() ? 0 : (double) accepted / all.size() * 100
        );
    }
}
