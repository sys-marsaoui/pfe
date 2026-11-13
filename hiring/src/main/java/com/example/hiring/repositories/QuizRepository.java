package com.example.hiring.repositories;

import com.example.hiring.entities.quiz.Quiz;
import com.example.hiring.entities.quiz.UserAnswer;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizRepository extends MongoRepository<Quiz, String> {
}
