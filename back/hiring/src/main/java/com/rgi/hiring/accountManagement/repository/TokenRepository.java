package com.rgi.hiring.accountManagement.repository;

import com.rgi.hiring.accountManagement.modals.Token;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TokenRepository extends MongoRepository<Token, String> {

//    @Query("""
//select t from Token t inner join User u on t.user.id = u.id
//where t.user.id = :userId and t.loggedOut = false
//""")


    @Query("{ 'user.id' : ?0, 'loggedOut' : false }")
    List<Token> findAllAccessTokensByUser(String userId);

    Optional<Token> findByAccessToken(String token);

    Optional<Token > findByRefreshToken(String token);

}
