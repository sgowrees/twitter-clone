package com.app.twitter_clone.controller;

import com.app.twitter_clone.dto.user.UpdateProfileRequest;
import com.app.twitter_clone.dto.user.UserResponse;
import com.app.twitter_clone.mapper.UserMapper;
import com.app.twitter_clone.model.User;
import com.app.twitter_clone.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    public UserController(
            UserService userService,
            UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserResponse>> searchUsers(@RequestParam String q) {
        List<UserResponse> response = userService.search(q).stream()
                .map(userMapper::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<UserResponse> getUser(@PathVariable Long userId) {
        User user = userService.findById(userId);
        return ResponseEntity.ok(userMapper.toResponse(user));
    }

    @PutMapping("/update")
    public ResponseEntity<UserResponse> updateProfile(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateProfileRequest request) {
        User user = userService.updateProfile(userId, request);
        return ResponseEntity.ok(userMapper.toResponse(user));
    }
}