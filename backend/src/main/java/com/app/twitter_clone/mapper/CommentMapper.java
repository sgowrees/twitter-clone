package com.app.twitter_clone.mapper;

import com.app.twitter_clone.dto.comment.CommentResponse;
import com.app.twitter_clone.model.Comment;
import org.springframework.stereotype.Component;

@Component
public class CommentMapper {

    public CommentResponse toResponse(Comment comment) {

        CommentResponse response = new CommentResponse();

        response.setId(comment.getId());
        response.setUserId(comment.getUser().getId());
        response.setPostId(comment.getPost().getId());
        response.setUsername(comment.getUser().getUsername());
        response.setContent(comment.getContent());
        response.setCreatedAt(comment.getCreatedAt());

        return response;
    }
}