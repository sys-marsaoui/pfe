package com.rgi.hiring.accountManagement.repository;

import com.rgi.hiring.accountManagement.modals.PersonalCredentials;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonalCredentialsRepository extends MongoRepository<PersonalCredentials, String> {

}