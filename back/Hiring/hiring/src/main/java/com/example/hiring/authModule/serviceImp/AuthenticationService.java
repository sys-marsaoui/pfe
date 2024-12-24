package com.example.hiring.authModule.serviceImp;


import com.example.hiring.authModule.dto.AuthenticationRequest;
import com.example.hiring.authModule.dto.AuthenticationResponse;
import com.example.hiring.authModule.repository.JwtService;
import com.example.hiring.authModule.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;



@Service
@ComponentScan
public class AuthenticationService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    JwtService jwtService;


    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        var user = userRepository.findByEmailUser(request.getEmail());
        if(user!=null) {
            if(request.getPassword().equals(user.getPassword())){

            var jwtToken = jwtService.generateToken(user.get(),user);
            AuthenticationResponse j = new AuthenticationResponse(jwtToken,user.getFirstName(),user.getLastName(),"200");

            return j;}
        }else {
            AuthenticationResponse j = new AuthenticationResponse(null,"400");
            return j;
        }
return null;}


}
