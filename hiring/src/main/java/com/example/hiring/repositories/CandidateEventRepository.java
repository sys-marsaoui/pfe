package com.example.hiring.repositories;

import com.example.hiring.entities.CandidateEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CandidateEventRepository extends JpaRepository<CandidateEvent, Integer> {
    List<CandidateEvent> findByUserIdOrderByCreatedAtDesc(Integer userId);
}
