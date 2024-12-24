package com.example.hiring.authModule.repository;

import com.example.hiring.authModule.dto.AddUserDto;
import com.example.hiring.authModule.dto.RecoveryPassword;
import com.example.hiring.authModule.dto.Register;
import com.example.hiring.authModule.entity.Users;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;



@Repository
public interface UserRepository extends JpaRepository<Users, Integer> {

    @Modifying
    @Transactional
    @Query("INSERT INTO Users u (u.address, u.email, u.firstName, u.lastName, u.password, u.tel) " +
            "VALUES (:#{#userDto.address}, :#{#userDto.email}, " +
            ":#{#userDto.firstName}, :#{#userDto.lastName}, :#{#userDto.password}, :#{#userDto.tel})")
    void addUser(@Param("userDto") AddUserDto userDto);


    @Query("SELECT u from Users u WHERE u.email=:#{#email}")
    Users findByEmailUser (String email);




    @Query("select u from Users u where u.email=:#{#email}")
    Users findByEmail(String email);

    @Modifying
    @Transactional
    @Query("update Users u set u.password=:#{#recoveryPassword.password} where u.email=:#{#recoveryPassword.email}")
     void  updatePassword(@Param("recoveryPassword") RecoveryPassword recoveryPassword);

    @Modifying
    @Transactional
    @Query("insert into Users u (u.firstName,u.lastName,u.tel,u.email,u.address,u.password) Values(:#{#register.firstName},:#{#register.lastName},:#{#register.tel},:#{#register.email},:#{#register.address},:#{#register.password})")
    void register(@Param("register") Register register);

}
