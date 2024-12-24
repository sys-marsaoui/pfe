package com.example.hiring.authModule.dto;


public class AddUserDto {


    private String address;
    private String email;
    private String firstName;
    private String lastName;
    private String tel;

    private String password;

    public AddUserDto( String address, String email, String firstName, String lastName, String tel, String password) {
        this.address = address;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.tel = tel;
        this.password = password;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
