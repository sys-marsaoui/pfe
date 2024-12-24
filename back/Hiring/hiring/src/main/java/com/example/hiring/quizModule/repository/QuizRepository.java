package com.example.hiring.quizModule.repository;

import com.example.hiring.quizModule.entity.QuizModal;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface QuizRepository extends MongoRepository<QuizModal, String> {
}
