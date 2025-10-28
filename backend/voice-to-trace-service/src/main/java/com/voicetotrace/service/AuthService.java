package com.voicetotrace.service;

import com.voicetotrace.model.User;
import com.voicetotrace.repository.UserRepository;
import com.voicetotrace.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    private final Set<String> blacklistedTokens = new HashSet<>();

    public String register(String username, String password, String role) {
        if (userRepository.findByUsername(username) != null) {
            throw new RuntimeException("Username already exists");
        }

        String encodedPassword = passwordEncoder.encode(password);
        User user = new User(username, encodedPassword, role, username);
        userRepository.save(user);

        return "User registered successfully. Please log in.";
    }

    public String login(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Bad credentials");
        }

        return jwtUtil.generateToken(username);
    }

    public void logout(String token) {
        blacklistedTokens.add(token);
    }

    public boolean isTokenInvalid(String token) {
        return blacklistedTokens.contains(token);
    }
}