package com.rgi.hiring.accountManagement.payload;

import lombok.Data;

@Data
public class SingnUpDto {
    private String firtName ;
    private String lastName ;
    private String username ;
    private String email;
    private String password;
}
