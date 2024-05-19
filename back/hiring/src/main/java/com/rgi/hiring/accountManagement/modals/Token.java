package com.rgi.hiring.accountManagement.modals;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Token {

    @Id
    private String id;

    private String accessToken;

    private String refreshToken;

    private boolean loggedOut;

    @DBRef
    private User user;

}
