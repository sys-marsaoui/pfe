package com.example.hiring.authModule.repository;

import com.example.hiring.authModule.entity.PasswordResetToken;
import com.example.hiring.authModule.entity.Users;
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
