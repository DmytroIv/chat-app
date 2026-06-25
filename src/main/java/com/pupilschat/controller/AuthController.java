package com.pupilschat.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pupilschat.config.JwtUtil;
import com.pupilschat.service.DatabaseManager;

import org.springframework.security.crypto.password.PasswordEncoder;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public String register(@RequestParam String username, @RequestParam String password) {
        if (DatabaseManager.getUserPassword(username) != null) {
            return "ERROR: Username already exists!";
        }

        String hashedPassword = passwordEncoder.encode(password);
        boolean success = DatabaseManager.createUser(username, hashedPassword);

        if (success) {
            return "SUCCESS: User registered! You can now log in.";
        }
        return "ERROR: Database error during registration.";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password) {

        String storedHash = DatabaseManager.getUserPassword(username);

        if (storedHash != null && passwordEncoder.matches(password, storedHash)) {
            String token = jwtUtil.generateToken(username);
            return "SUCCESS! Your JWT Token is: \n" + token;
        }

        return "ERROR: Invalid Credentials!";
    }
}
