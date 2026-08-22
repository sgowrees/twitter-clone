package com.app.twitter_clone.repository;

import com.app.twitter_clone.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    // Used for a single user's profile page (their own post history)
    List<Post> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Powers the home feed. The service layer first asks FollowRepository
    // for the list of user IDs the current user follows, then passes that
    // list here to get their posts, newest first.
    List<Post> findByUserIdInOrderByCreatedAtDesc(List<Long> userIds);

    // Used by the global Chirp timeline
    List<Post> findAllByOrderByCreatedAtDesc();

    // Used on the profile page to show total post count
    long countByUserId(Long userId);
}

