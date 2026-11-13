package com.example.hiring.repositories;

import com.example.hiring.entities.Candidature;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CandidatureRepository extends MongoRepository<Candidature, String> {

    List<Candidature> findByInterviewDateTimeNotNull();
    List<Candidature> findByPostId(String postId);
    Optional<Candidature> findByUserIdAndPostId(Integer userId, String postId);

    // CORRECT: Use MongoDB JSON query syntax
    @Query("{"
            + "  '?0': { $exists: false },"
            + "  '?1': { $exists: false },"
            + "  'postId': { $in: ?2 },"
            + "  'startTime': { $gte: ?3, $lte: ?4 },"
            + "  'status': { $in: ?5 }"
            + "}")
    List<Candidature> findAllWithDetails(
            boolean ignorePostIds,
            boolean ignoreDates,
            List<String> postIds,
            LocalDateTime from,
            LocalDateTime to,
            List<String> statuses);
}