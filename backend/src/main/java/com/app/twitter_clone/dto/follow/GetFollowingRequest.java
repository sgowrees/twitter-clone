package com.app.twitter_clone.dto.follow;

public class GetFollowingRequest {

    private Long userId;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}