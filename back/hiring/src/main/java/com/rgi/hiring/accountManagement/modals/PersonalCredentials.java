package com.rgi.hiring.accountManagement.modals;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

// Annotations
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document("personalCredentials")
public class PersonalCredentials {
    @Id
    private String id;

    private String userName;
    private String email;
    private String password;
    private String repeatPassword;

}