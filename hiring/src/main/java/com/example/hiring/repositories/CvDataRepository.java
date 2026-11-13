package com.example.hiring.repositories;

import com.example.hiring.entities.CvData;
import com.example.hiring.entities.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CvDataRepository extends JpaRepository<CvData, Integer> {
    CvData findByUser(Users user);
    List<CvData> findByUserIdIn(List<Integer> userIds);
}