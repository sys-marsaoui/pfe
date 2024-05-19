package com.rgi.hiring.accountManagement.controllers;


import com.rgi.hiring.accountManagement.modals.User;
import com.rgi.hiring.accountManagement.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {


    @Autowired
    private UserService userService;

    @GetMapping("/all")
    public List<User> getAllUser(){
        System.out.println("ahawaaaaaaa");
        return userService.getUsers();
    }

    @PostMapping("/insert")
    public User insert (@RequestBody User user){
        return userService.addUser(user);
    }

}
