package com.rgi.hiring.accountManagement.controllers;


import com.rgi.hiring.accountManagement.modals.User;
import com.rgi.hiring.accountManagement.payload.AuthenticationResponse;
import com.rgi.hiring.accountManagement.service.impl.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import org.springframework.web.bind.annotation.RestController;

@RestController
//@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
     AuthenticationService authService;



    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @RequestBody User request
    ) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(
            @RequestBody User request
    ) {
        return ResponseEntity.ok(authService.authenticate(request));
    }










    //    @Autowired
//    private AuthenticationManager authenticationManager;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private RoleRepository roleRepository;
//
//    @Autowired
//    private PasswordEncoder passwordEncoder;
//
//    @Autowired
//    private JwtTokenProvider tokenProvider;

//    @PostMapping("/signin")
//    public ResponseEntity<JWTAuthResponse> authenticateUser(@RequestBody LoginDto loginDto){
//        Authentication authentication =  authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
//                loginDto.getUserNameOrEmail(), loginDto.getPassword()));
//
//        SecurityContextHolder.getContext().setAuthentication(authentication);
//
//
//        // get token from tokenProvider
//        String token = tokenProvider.generateToken(authentication);
//        //return new ResponseEntity<>("User signin Successfull !! ", HttpStatus.OK);
//        return  ResponseEntity.ok(new JWTAuthResponse(token));
//    }
//
//    @PostMapping("/signup")
//    public ResponseEntity<?> registerUser (@RequestBody SingnUpDto singnUpDto) {
//        if (userRepository.existsByUserName(singnUpDto.getUsername())){
//            return new ResponseEntity <> ("Username is already taken !", HttpStatus.BAD_REQUEST);
//        }
//        if (userRepository.existsByEmail(singnUpDto.getEmail())){
//            return new ResponseEntity <> ("Email is already taken !",HttpStatus.BAD_REQUEST);
//        }
//
//        User user = new User();
//
//
//        user.setPassword(passwordEncoder.encode(singnUpDto.getPassword()));
//        user.setFirstName(singnUpDto.getFirtName());
//        user.setLastName(singnUpDto.getLastName());
//        user.setUserName(singnUpDto.getUsername());
//        user.setEmail(singnUpDto.getEmail());
//
//
//        Role roles = roleRepository.findByName("ROLE_ADMIN").get();
//        user.setRoles(Collections.singleton(roles));
//
//        userRepository.save(user);
//        return new ResponseEntity<> ("User registered successfiuly ",HttpStatus.OK);
//    }



}
