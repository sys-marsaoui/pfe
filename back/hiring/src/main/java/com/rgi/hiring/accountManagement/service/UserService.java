package com.rgi.hiring.accountManagement.service;

import com.rgi.hiring.accountManagement.modals.User;

import java.util.List;

public interface UserService {


    public List<User> getUsers();

    public User addUser (User user);

}
