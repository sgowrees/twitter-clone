package com.app.twitter_clone.controller;

import com.app.twitter_clone.dto.user.UserResponse;
import com.app.twitter_clone.mapper.UserMapper;
import com.app.twitter_clone.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/search")
public class UserSearchController {
    private final UserService userService;
    private final UserMapper userMapper;

    public UserSearchController(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> searchUsers(@RequestParam(name = "q") String query) {
        return ResponseEntity.ok(userService.search(query).stream()
                .map(userMapper::toResponse)
                .toList());
    }
}
