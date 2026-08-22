package com.app.twitter_clone.dto.post;

public class PostRequest {
    
    private Long userId;
    private String content;

    public Long getUserId() {
        return userId;
    }

    public String getContent() {
        return content;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setContent(String content) {
        this.content = content;
    }

    
}
