package com.example.hiring.authModule.dto;


public class AuthenticationResponse {
    private String token;
    private String firstName;
    private String lastName;
    private String code;
    public AuthenticationResponse(String token) {
        this.token = token;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public AuthenticationResponse(String token, String code) {
        this.token = token;
        this.code = code;
    }

    public AuthenticationResponse(String token, String firstName, String lastName) {
        this.token = token;
        this.firstName=firstName;
        this.lastName=lastName;
    }

    public AuthenticationResponse(String token, String firstName, String lastName, String code) {
        this.token = token;
        this.firstName = firstName;
        this.lastName = lastName;
        this.code = code;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}