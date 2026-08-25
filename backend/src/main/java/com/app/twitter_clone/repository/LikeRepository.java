package com.app.twitter_clone.repository;

import com.app.twitter_clone.model.Like;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {

    // Used to fetch a specific like row, e.g. before deleting it on unlike
    Optional<Like> findByUserIdAndPostId(Long userId, Long postId);

    // Used to check like status quickly (e.g. for isLikedByCurrentUser in PostResponse)
    boolean existsByUserIdAndPostId(Long userId, Long postId);

    // Used to show the like count on a post card
    long countByPostId(Long postId);

    // Used when a user clicks "unlike" on a post
    void deleteByUserIdAndPostId(Long userId, Long postId);

    // Used when a post is deleted, so its likes don't become orphaned rows
    void deleteByPostId(Long postId);
}