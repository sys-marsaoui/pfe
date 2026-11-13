package com.example.hiring.services;

import com.example.hiring.entities.CvData;
import com.example.hiring.entities.dto.AddUserDto;
import com.example.hiring.entities.dto.RecoveryPassword;
import com.example.hiring.entities.dto.Register;
import com.example.hiring.entities.Users;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public  interface UserService {
     List<Users> findAll();

     void createUser(AddUserDto addUserDto, String post);


      void resetPassword(String token, String newPassword) ;

    void createUser(AddUserDto addUserDto);

    void createAdminUser(AddUserDto addUserDto);

    void sendPasswordResetEmail(String email) ;

      Users getUserByEmail(String email);
      Users getUserById(int id);

     void updatePassword(RecoveryPassword recoveryPassword);

     void register(Register register);

    void createPasswordResetTokenForUser(Users user, String token);

    Users registerCandidateForPost(String postId, Users candidate);

    List<Users> getUserByPostId(String postId);

    List<Users> findAllCandidates();

    List<Users> findAllAdmins();

    void saveCvData(CvData cvData);
}
