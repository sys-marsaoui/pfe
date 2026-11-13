package com.example.hiring.repositories;

import com.example.hiring.entities.dto.AddUserDto;
import com.example.hiring.entities.dto.RecoveryPassword;
import com.example.hiring.entities.dto.Register;
import com.example.hiring.entities.Users;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface UserRepository extends JpaRepository<Users, Integer> {

    @Modifying
    @Transactional
    @Query("INSERT INTO Users (address, email, firstName, lastName, password, tel, postId, role) " +
            "VALUES (:#{#userDto.address}, :#{#userDto.email}, " +
            ":#{#userDto.firstName}, :#{#userDto.lastName}, :#{#userDto.password}, :#{#userDto.tel}, :#{#userDto.postId}, :#{#userDto.role})")
    int addUser(@Param("userDto") AddUserDto userDto);

    @Modifying
    @Transactional
    @Query("update Users u set u.password=:#{#recoveryPassword.password} where u.email=:#{#recoveryPassword.email}")
     void  updatePassword(@Param("recoveryPassword") RecoveryPassword recoveryPassword);

    @Modifying
    @Transactional
    @Query("insert into Users u (u.firstName,u.lastName,u.tel,u.email,u.address,u.password) Values(:#{#register.firstName},:#{#register.lastName},:#{#register.tel},:#{#register.email},:#{#register.address},:#{#register.password})")
    void register(@Param("register") Register register);

   // @Query("select u from Users u where u.postId=:#{#postId}")
    List<Users> findByPostId(String postId);

    @Query("SELECT u FROM Users u WHERE u.email = :#{#email}")
    Users findByEmailUser(@Param("email") String email);

    @Query("SELECT u FROM Users u WHERE u.email = :#{#email}")
    Users findByEmail(@Param("email") String email);
    @Query("SELECT u FROM Users u WHERE u.role.name = 'CANDIDATE'")
    List<Users> findAllCandidates();

    @Query("SELECT u FROM Users u WHERE u.role.name = 'ADMIN'")
    List<Users> findAllAdmins();

}
