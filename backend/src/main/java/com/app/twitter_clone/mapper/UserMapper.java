package com.app.twitter_clone.mapper;

import com.app.twitter_clone.dto.user.UserResponse;
import com.app.twitter_clone.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserResponse toResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setDisplayName(user.getDisplayName());
        response.setBio(user.getBio());
        response.setProfilePicture(user.getProfilePicture());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }
}