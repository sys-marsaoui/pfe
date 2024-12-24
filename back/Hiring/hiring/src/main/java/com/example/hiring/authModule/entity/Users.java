package com.example.hiring.authModule.entity;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Entity
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "tel")
    private String tel;

    @Column(name = "email")
    private String email;

    @Column(name = "address")
    private String address;

    @Column(name = "password")
    private String password;

    @Column(name = "postId")
    private String postId;

    public Users() {
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public Users(int id, String firstName, String lastName, String tel, String email, String address, String password, String postId) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.tel = tel;
        this.email = email;
        this.address = address;
        this.password = password;
        this.postId = postId;
    }

    public Users(int id, String firstName, String lastName, String tel, String email, String address, String password) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.tel = tel;
        this.email = email;
        this.address = address;
        this.password = password;

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }



    public UserDetails get() {
        UserDetails user=new UserDetails() {
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return null;
            }

            @Override
            public String getPassword() {
                return getPassword();
            }

            @Override
            public String getUsername() {
                return getEmail();
            }


            @Override
            public boolean isAccountNonExpired() {
                return false;
            }

            @Override
            public boolean isAccountNonLocked() {
                return false;
            }

            @Override
            public boolean isCredentialsNonExpired() {
                return false;
            }

            @Override
            public boolean isEnabled() {
                return get().isEnabled();
            }
        };
        return user;
    }
}
