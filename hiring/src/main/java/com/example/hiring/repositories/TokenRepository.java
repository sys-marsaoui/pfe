package com.example.hiring.repositories;

import com.example.hiring.entities.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {
    @Query("SELECT t FROM Token t WHERE t.accessToken = :token OR t.refreshToken = :token")
    Token findByToken(String token);

    @Query("SELECT t FROM Token t WHERE t.user.id = :userId AND t.revoked = false")
    Token findByUserId(int userId);


    @Modifying
    @Transactional
    @Query("UPDATE Token t SET t.revoked = true WHERE t.user.id = :userId")
    void revokeTokensByUserId(Integer userId);
}