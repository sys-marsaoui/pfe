package com.example.hiring.repositories;

import com.example.hiring.entities.PasswordResetToken;
import com.example.hiring.entities.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken,Integer> {
    @Query("select p from PasswordResetToken p where p.token=:#{#token}")
    PasswordResetToken findByToken(String token);

    @Query("select p from PasswordResetToken p where p.user=:#{#users}")
    PasswordResetToken findByUser(Users users);

}
