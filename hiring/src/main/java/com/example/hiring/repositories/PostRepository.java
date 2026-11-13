package com.example.hiring.repositories;

import com.example.hiring.entities.Post;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PostRepository extends MongoRepository<Post, String> {
    // PostRepository.java
    List<Post> findByQuizId(String quizId);
}
