package com.example.hiring.entities.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AddUserDto {
    private String address;
    private String email;
    private String firstName;
    private String lastName;
    private String tel;
    private String password;
    private String postId;
    private String role;
}
