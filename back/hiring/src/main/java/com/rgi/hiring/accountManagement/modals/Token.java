package com.rgi.hiring.accountManagement.modals;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Token {

    @Id
    private String id;

    @Field(name="access_token")
    private String accessToken;

    @Field(name="refresh_token")
    private String refreshToken;

    @Field(name="is_logged_out")
    private boolean loggedOut;

    @DBRef
    private User user;

}
