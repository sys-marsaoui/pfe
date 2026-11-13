package com.example.hiring.entities.dto;


public class AuthenticationResponse {
    private String token;
    private String refreshToken;
    private String firstName;
    private String lastName;
    private String code;

    public AuthenticationResponse(String token, String refreshToken, String firstName, String lastName, String code) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.firstName = firstName;
        this.lastName = lastName;
        this.code = code;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}