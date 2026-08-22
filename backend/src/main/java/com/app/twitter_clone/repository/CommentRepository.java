package com.app.twitter_clone.repository;

import com.app.twitter_clone.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // Loads all comments under a post, oldest first
    List<Comment> findByPostIdOrderByCreatedAtAsc(Long postId);

    // Counts how many comments a post has
    long countByPostId(Long postId);

    // Deletes all comments belonging to a post
    void deleteByPostId(Long postId);

    // Finds comments made by a specific user on a specific post
    List<Comment> findByPostIdAndUserId(Long postId, Long userId);
}