package com.example.hiring.repositories;

import com.example.hiring.entities.quiz.Technology;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TechnologyRepository extends MongoRepository<Technology, String> {
}
