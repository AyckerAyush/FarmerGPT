package com.farmer.ai.service;

import com.farmer.ai.model.User;
import com.farmer.ai.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void registerUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }

    public void updateResetToken(String token, String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user != null) {
            user.setResetToken(token);
            userRepository.save(user);
        }
    }

    public User getByResetToken(String token) {
        return userRepository.findByResetToken(token).orElse(null);
    }

    public void updatePassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        userRepository.save(user);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    public void updateUser(User user) {
        userRepository.save(user);
    }

    public boolean checkSecurityAnswer(User user, String answer) {
        // Simple string comparison for now, can be hashed later
        return user.getSecurityAnswer() != null && user.getSecurityAnswer().equalsIgnoreCase(answer);
    }
}
