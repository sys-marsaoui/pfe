package com.example.hiring.repositories;

import com.example.hiring.entities.quiz.Quiz;
import com.example.hiring.entities.quiz.UserAnswer;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAnswerRepository extends MongoRepository<UserAnswer, String> {

    List<UserAnswer> findAllByPostId(String postId);

    UserAnswer findAllByPostIdAndUserId(String id, int userId);

    Optional<UserAnswer> findByUserIdAndPostId(Integer userId, String postId);}
