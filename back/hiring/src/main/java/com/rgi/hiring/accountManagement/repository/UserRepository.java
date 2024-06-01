package com.rgi.hiring.accountManagement.repository;

import com.rgi.hiring.accountManagement.modals.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByUserNameOrEmail(String email , String userName);
    Boolean existsByUserName(String UserName);

    Optional<User> findByUserName(String username);

    Boolean existsByEmail(String email);
}
