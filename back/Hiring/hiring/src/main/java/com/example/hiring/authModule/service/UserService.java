package com.example.hiring.authModule.service;

import com.example.hiring.authModule.dto.AddUserDto;
import com.example.hiring.authModule.dto.RecoveryPassword;
import com.example.hiring.authModule.dto.Register;
import com.example.hiring.authModule.entity.Users;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public  interface UserService {
     List<Users> findAll();

     void createUser(AddUserDto addUserDto);


      void resetPassword(String token, String newPassword) ;
      void sendPasswordResetEmail(String email) ;

      Users getUserByEmail(String email);

     void updatePassword(RecoveryPassword recoveryPassword);

     void register(Register register);

    Users registerCandidateForPost(String postId, Users candidate);
}
