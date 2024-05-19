package com.rgi.hiring.accountManagement.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordEncoderGenerator {

    public static void main(String[] args){
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        System.out.println(passwordEncoder.encode("marsaoui"));
        //$2a$10$AYkpEenN/zhTrgV6iNAW4ud3UB/V/8tI2I91/F.BIn1tqUeoTvir6

    }
}
