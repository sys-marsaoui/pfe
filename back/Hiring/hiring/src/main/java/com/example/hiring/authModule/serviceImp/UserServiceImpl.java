package com.example.hiring.authModule.serviceImp;

import com.example.hiring.authModule.dto.AddUserDto;
import com.example.hiring.authModule.dto.RecoveryPassword;
import com.example.hiring.authModule.dto.Register;
import com.example.hiring.authModule.entity.PasswordResetToken;
import com.example.hiring.authModule.entity.Users;
import com.example.hiring.authModule.repository.PasswordResetTokenRepository;
import com.example.hiring.authModule.repository.UserRepository;
import com.example.hiring.authModule.service.EmailServiceImpl;
import com.example.hiring.authModule.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    EmailServiceImpl emailService;
    @Autowired
    private PasswordResetTokenRepository tokenRepository;
    @Autowired
    UserRepository userRepository;

    @Override
    public List<Users> findAll() {
        return userRepository.findAll();
    }

    @Override
    public void createUser(AddUserDto addUserDto) {
        userRepository.addUser(addUserDto);
    }




    public void sendPasswordResetEmail(String email) {
        Users user = userRepository.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        String token = UUID.randomUUID().toString();
        createPasswordResetTokenForUser(user, token);

        String resetUrl = "http://localhost:4200/nouveau_mdp?token=" + token;
        emailService.sendEmail(email, "Password Reset Request", "To reset your password, click the link below:\n" + resetUrl);
    }
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token);
        if (resetToken == null || resetToken.getExpiryDate().before(new Date())) {
            throw new RuntimeException("Token is invalid or expired");
        }

        Users user = resetToken.getUser();
        user.setPassword(newPassword); // Ensure this is properly hashed
        userRepository.save(user);
    }
    public void createPasswordResetTokenForUser(Users user, String token) {
        PasswordResetToken myToken = tokenRepository.findByUser(user);
        if (myToken == null) {
            myToken = new PasswordResetToken(token, user, new Date(System.currentTimeMillis() + 1000 * 60 * 60)); // 1 hour expiry
            tokenRepository.save(myToken);
        }
        else {
        myToken.setToken(token);
        myToken.setUser(user);
        myToken.setExpiryDate(new Date(System.currentTimeMillis() + 1000 * 60 * 60)); // 1 hour expiry
        tokenRepository.save(myToken);}
    }

    public Users registerCandidateForPost(String postId, Users candidate) {
        candidate.setPostId(postId);
        return userRepository.save(candidate);
    }

    @Override
    public Users getUserByEmail(String email) {
      return   userRepository.findByEmail(email);
    }

    @Override
    public void updatePassword(RecoveryPassword recoveryPassword) {
        userRepository.updatePassword(recoveryPassword);
    }

    @Override
    public void register(Register register) {
        userRepository.register(register);
    }

}
