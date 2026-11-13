package com.example.hiring.services;

import com.example.hiring.entities.quiz.Technology;
import com.example.hiring.repositories.TechnologyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TechnologyServiceImpl {
    private final TechnologyRepository technologyRepository;

    public Technology create(Technology technology){
        return technologyRepository.save(technology);
    }

    public List<Technology> getAll(String search) {
        return technologyRepository.findAll();
    }
}
