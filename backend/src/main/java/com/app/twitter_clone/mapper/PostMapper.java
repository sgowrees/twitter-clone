package com.app.twitter_clone.mapper;

import com.app.twitter_clone.dto.post.PostResponse;
import com.app.twitter_clone.model.Post;
import org.springframework.stereotype.Component;

@Component
public class PostMapper {

    private final UserMapper userMapper;

    public PostMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public PostResponse toResponse(Post post, long likeCount, long commentCount, boolean isLikedByCurrentUser) {
        PostResponse response = new PostResponse();
        response.setId(post.getId());
        response.setContent(post.getContent());
        response.setAuthor(userMapper.toResponse(post.getUser()));
        response.setLikeCount(likeCount);
        response.setCommentCount(commentCount);
        response.setLikedByCurrentUser(isLikedByCurrentUser);
        response.setCreatedAt(post.getCreatedAt());
        response.setUpdatedAt(post.getUpdatedAt());
        return response;
    }
}