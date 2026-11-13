package com.example.hiring.repositories;

import com.example.hiring.entities.AtsMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AtsMatchRepository extends JpaRepository<AtsMatch, Integer> {
    List<AtsMatch> findByPostId(String postId);

    @Query(value = "SELECT * FROM ats_matches WHERE user_id = :userId", nativeQuery = true)
    List<AtsMatch> findByUserId(@Param("userId") Integer userId);
    Optional<AtsMatch> findByUserIdAndPostId(Integer userId, String postId);
}
