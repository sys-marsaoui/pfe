package com.example.hiring.services;


    import com.example.hiring.entities.Candidature;
    import com.example.hiring.repositories.CandidatureRepository;
    import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

    @Service
    public class PlanificationService {
        private final CandidatureRepository candidatureRepository;


        public PlanificationService(CandidatureRepository candidatureRepository) {
            this.candidatureRepository = candidatureRepository;
        }

        public Candidature scheduleInterview(String candidatId, LocalDateTime interviewDateTime) {
            Candidature candidature = candidatureRepository.findById(candidatId)
                    .orElseThrow(() -> new RuntimeException("Candidat not found"));
            candidature.setInterviewDateTime(interviewDateTime);
          //  candidature .setStatut("Entretien planifié");
            return candidatureRepository.save(candidature);
        }

        public List<Candidature> getCandidatesWithScheduledInterviews() {
            return candidatureRepository.findByInterviewDateTimeNotNull();
        }

        public Candidature updateCandidatStatus(String candidatId, String statut) {
            Candidature candidature = candidatureRepository.findById(candidatId)
                    .orElseThrow(() -> new RuntimeException("Candidat not found"));
           // candidature.setStatut(statut);
            return candidatureRepository.save(candidature);
        }
    }

