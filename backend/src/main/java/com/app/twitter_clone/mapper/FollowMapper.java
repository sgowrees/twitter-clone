package com.app.twitter_clone.mapper;

import com.app.twitter_clone.dto.follow.FollowResponse;
import com.app.twitter_clone.model.Follow;
import com.app.twitter_clone.model.User;
import org.springframework.stereotype.Component;

@Component
public class FollowMapper {

    private final UserMapper userMapper;

    public FollowMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    // otherUser is passed in separately because which side of the Follow row
    // is "the other person" depends on whether the caller is building a
    // followers list (otherUser = follow.getFollower())
    // or a following list (otherUser = follow.getFollowing()).
    public FollowResponse toResponse(Follow follow, User otherUser) {
        FollowResponse response = new FollowResponse();
        response.setId(follow.getId());
        response.setUser(userMapper.toResponse(otherUser));
        response.setCreatedAt(follow.getCreatedAt());
        return response;
    }

    
}