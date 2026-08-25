package com.app.twitter_clone.service;

import com.app.twitter_clone.dto.auth.RegisterRequest;
import com.app.twitter_clone.dto.user.UpdateProfileRequest;
import com.app.twitter_clone.model.User;
import com.app.twitter_clone.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already taken");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already taken");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setDisplayName(request.getDisplayName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        return userRepository.save(user);
    }

    public User authenticate(String usernameOrEmail, String rawPassword) {
        Optional<User> userResult =
                userRepository.findByUsername(usernameOrEmail);

        if (userResult.isEmpty()) {
            userResult =
                    userRepository.findByEmail(usernameOrEmail);
        }

        if (userResult.isEmpty()) {
            throw new RuntimeException(
                    "Invalid username/email or password"
            );
        }

        User user = userResult.get();

        if (!passwordEncoder.matches(
                rawPassword,
                user.getPassword())) {

            throw new RuntimeException(
                    "Invalid username/email or password"
            );
        }

        return user;
    }

    public User findById(Long userId) {
        Optional<User> userResult =
                userRepository.findById(userId);

        if (userResult.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        return userResult.get();
    }

    public List<User> search(String query) {
        String normalized = query == null ? "" : query.trim();
        if (normalized.isEmpty()) {
            return List.of();
        }
        return userRepository.findByUsernameContainingIgnoreCaseOrDisplayNameContainingIgnoreCase(
                normalized, normalized);
    }

    public User updateProfile(
            Long userId,
            UpdateProfileRequest request) {

        Optional<User> userResult =
                userRepository.findById(userId);

        if (userResult.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        User user = userResult.get();

        if (request.getDisplayName() != null) {
            user.setDisplayName(request.getDisplayName());
        }

        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }

        if (request.getProfilePicture() != null) {
            user.setProfilePicture(request.getProfilePicture());
        }

        return userRepository.save(user);
    }
}