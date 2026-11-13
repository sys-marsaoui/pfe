package com.example.hiring.services;

import com.example.hiring.entities.CandidateEvent;
import com.example.hiring.entities.Users;
import com.example.hiring.entities.dto.CandidateEventDto;
import com.example.hiring.repositories.CandidateEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CandidateHistoryService {
    private final CandidateEventRepository repository;

    public void record(Users user, String stage, String description) {
        if (user == null || user.getId() == null || stage == null || description == null) return;
        repository.save(new CandidateEvent(user, user.getPostId(), stage, description));
    }

    public List<CandidateEventDto> getForUser(Integer userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(event -> new CandidateEventDto(event.getId(), event.getPostId(), event.getStage(),
                        event.getDescription(), event.getCreatedAt()))
                .toList();
    }
}
