package com.example.hiring.repositories;

import com.example.hiring.entities.UserProgress;
import com.example.hiring.entities.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserProgressRepository extends JpaRepository<UserProgress, Integer> {
    Optional<UserProgress> findByUser(Users user);
    Optional<UserProgress> findByUserId(Integer userId);
    List<UserProgress> findByUserIdIn(List<Integer> userIds);
}