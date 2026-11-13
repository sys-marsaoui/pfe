package com.example.hiring.services;

import com.example.hiring.entities.CvData;
import com.example.hiring.entities.Role;
import com.example.hiring.entities.dto.AddUserDto;
import com.example.hiring.entities.dto.RecoveryPassword;
import com.example.hiring.entities.dto.Register;
import com.example.hiring.entities.PasswordResetToken;
import com.example.hiring.entities.Users;
import com.example.hiring.repositories.CvDataRepository;
import com.example.hiring.repositories.PasswordResetTokenRepository;
import com.example.hiring.repositories.PostRepository;
import com.example.hiring.repositories.RoleRepository;
import com.example.hiring.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private EmailServiceImpl emailService;
    @Autowired
    private PasswordResetTokenRepository tokenRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CandidatureService candidatureService;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private CvDataRepository cvDataRepository;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public List<Users> findAll() {
        return userRepository.findAll();
    }

    @Override
    public void createUser(AddUserDto addUserDto, String post) {
        if (userRepository.findByEmail(addUserDto.getEmail()) != null) {
            throw new RuntimeException("Email already exists");
        }

        Users user = new Users();
        user.setFirstName(addUserDto.getFirstName());
        user.setLastName(addUserDto.getLastName());
        user.setEmail(addUserDto.getEmail());
        user.setTel(addUserDto.getTel());
        user.setAddress(addUserDto.getAddress());
        user.setPassword(passwordEncoder.encode(addUserDto.getPassword()));

        if (post != null && !post.trim().isEmpty()) {
            addUserDto.setPostId(post);
            addUserDto.setRole("CANDIDATE");
            Role role = roleRepository.findByName("CANDIDATE");
            if (role == null) {
                throw new RuntimeException("Role not found: CANDIDATE");
            }
            user.setRole(role);
            user.setPostId(post);
        } else {
            String roleName = addUserDto.getRole() != null ? addUserDto.getRole() : "CANDIDATE";
            Role role = roleRepository.findByName(roleName);
            if (role == null) {
                throw new RuntimeException("Role not found: " + roleName);
            }
            user.setRole(role);
        }

        Users savedUser = userRepository.save(user);

        if (post != null && !post.trim().isEmpty()) {
            candidatureService.postuler(post, savedUser); // Use CandidatureService
        }
    }

    @Override
    public List<Users> getUserByPostId(String postId) {
        return userRepository.findByPostId(postId);
    }

    @Override
    public Users getUserById(int id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public void createUser(AddUserDto addUserDto) {
        if (userRepository.findByEmail(addUserDto.getEmail()) != null) {
            throw new RuntimeException("Email already exists");
        }
        Users user = new Users();
        user.setFirstName(addUserDto.getFirstName());
        user.setLastName(addUserDto.getLastName());
        user.setEmail(addUserDto.getEmail());
        user.setTel(addUserDto.getTel());
        user.setAddress(addUserDto.getAddress());
        user.setPassword(passwordEncoder.encode(addUserDto.getPassword()));
        Role role = roleRepository.findByName(addUserDto.getRole() != null ? addUserDto.getRole() : "CANDIDATE");
        if (role == null) {
            throw new RuntimeException("Role not found: " + addUserDto.getRole());
        }
        user.setRole(role);
        userRepository.save(user);
    }

    @Override
    public void createAdminUser(AddUserDto addUserDto) {
        if (userRepository.findByEmail(addUserDto.getEmail()) != null) {
            throw new RuntimeException("Email already exists");
        }
        Users user = new Users();
        user.setFirstName(addUserDto.getFirstName());
        user.setLastName(addUserDto.getLastName());
        user.setEmail(addUserDto.getEmail());
        user.setTel(addUserDto.getTel());
        user.setAddress(addUserDto.getAddress());
        user.setPassword(passwordEncoder.encode(addUserDto.getPassword()));
        Role role = roleRepository.findByName("ADMIN");
        if (role == null) {
            role = roleRepository.save(new Role("ADMIN"));
        }
        user.setRole(role);
        userRepository.save(user);
    }

    @Override
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

    @Override
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token);
        if (resetToken == null || resetToken.getExpiryDate().before(new Date())) {
            throw new RuntimeException("Token is invalid or expired");
        }
        Users user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public void createPasswordResetTokenForUser(Users user, String token) {
        PasswordResetToken myToken = tokenRepository.findByUser(user);
        if (myToken == null) {
            myToken = new PasswordResetToken(token, user, new Date(System.currentTimeMillis() + 1000 * 60 * 60));
            tokenRepository.save(myToken);
        } else {
            myToken.setToken(token);
            myToken.setExpiryDate(new Date(System.currentTimeMillis() + 1000 * 60 * 60));
            tokenRepository.save(myToken);
        }
    }

    @Override
    public Users registerCandidateForPost(String postId, Users candidate) {
        if (postId == null || postId.isBlank() || !postRepository.existsById(postId)) {
            throw new RuntimeException("The selected job offer does not exist");
        }
        if (candidate.getEmail() == null || candidate.getEmail().isBlank()
                || candidate.getPassword() == null || candidate.getPassword().isBlank()) {
            throw new RuntimeException("Email and password are required");
        }
        if (userRepository.findByEmail(candidate.getEmail()) != null) {
            throw new RuntimeException("Email already exists");
        }
        candidate.setPassword(passwordEncoder.encode(candidate.getPassword()));
        candidate.setPostId(postId);
        Role role = roleRepository.findByName("CANDIDATE");
        if (role == null) {
            role = roleRepository.save(new Role("CANDIDATE"));
        }
        candidate.setRole(role);
        Users savedCandidate = userRepository.save(candidate);
        candidatureService.postuler(postId, savedCandidate); // Use CandidatureService
        return savedCandidate;
    }

    @Override
    public Users getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public void updatePassword(RecoveryPassword recoveryPassword) {
        userRepository.updatePassword(recoveryPassword);
    }

    @Override
    public void register(Register register) {
        if (userRepository.findByEmail(register.getEmail()) != null) {
            throw new RuntimeException("Email already exists");
        }
        Users user = new Users();
        user.setFirstName(register.getFirstName());
        user.setLastName(register.getLastName());
        user.setEmail(register.getEmail());
        user.setTel(String.valueOf(register.getTel()));
        user.setAddress(register.getAddress());
        user.setPassword(passwordEncoder.encode(register.getPassword()));
        Role role = roleRepository.findByName(register.getRole() != null ? register.getRole() : "CANDIDATE");
        if (role == null) {
            throw new RuntimeException("Role not found: " + register.getRole());
        }
        user.setRole(role);
        userRepository.save(user);
    }

    @Override
    public List<Users> findAllCandidates() {
        return userRepository.findAllCandidates();
    }

    @Override
    public List<Users> findAllAdmins() {
        return userRepository.findAllAdmins();
    }

    @Override
    public void saveCvData(CvData cvData) {
        cvDataRepository.save(cvData);
    }
}
