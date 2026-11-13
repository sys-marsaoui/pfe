package com.example.hiring.entities.dto;


public class Register {
    private String firstName;
    private String lastName;
    private String email;
    private Integer tel;
    private String address;
    private String password;
    private String role;

    public Register() {}

    public Register(String firstName, String lastName, String email, Integer tel, String address, String password, String role) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.tel = tel;
        this.address = address;
        this.password = password;
        this.role = role;
    }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Integer getTel() { return tel; }
    public void setTel(Integer tel) { this.tel = tel; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}