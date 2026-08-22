package com.app.twitter_clone.dto.comment;

import jakarta.validation.constraints.NotBlank;

public class CommentRequest {

    private Long userId;
    private Long postId;

    @NotBlank(message = "Comment cannot be empty")
    private String content;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}