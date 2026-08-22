package com.app.twitter_clone.controller;

import com.app.twitter_clone.dto.auth.AuthResponse;
import com.app.twitter_clone.dto.auth.LoginRequest;
import com.app.twitter_clone.dto.auth.RegisterRequest;
import com.app.twitter_clone.model.User;
import com.app.twitter_clone.security.JwtService;
import com.app.twitter_clone.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;

    public AuthController(
            UserService userService,
            JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        User user = userService.register(request);

        String token = jwtService.generateToken(user.getId(), user.getUsername());

        AuthResponse response = new AuthResponse(token, user.getId(), user.getUsername());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {

        User user = userService.authenticate(request.getUsernameOrEmail(), request.getPassword());

        String token = jwtService.generateToken(user.getId(), user.getUsername());

        AuthResponse response = new AuthResponse(token, user.getId(), user.getUsername());

        return ResponseEntity.ok(response);
    }
}