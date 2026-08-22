package com.app.twitter_clone.repository;

import com.app.twitter_clone.model.Follow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    // Used to fetch a specific follow row, e.g. before deleting it on unfollow
    Optional<Follow> findByFollowerIdAndFollowingId(Long followerId, Long followingId);

    // Used to check follow status quickly (e.g. for isFollowedByCurrentUser in profile view)
    boolean existsByFollowerIdAndFollowingId(Long followerId, Long followingId);

    // Used when a user clicks "unfollow"
    void deleteByFollowerIdAndFollowingId(Long followerId, Long followingId);

    // Used for the "following" list on a profile page: users that :userId follows
    List<Follow> findFollowingIdsByFollowerId(Long userId);

    // Used for the "followers" list on a profile page: users that follow :userId
    List<Follow> findFollowerIdsByFollowingId(Long userId);

    // Used to show the "following" count on a profile page
    long countByFollowerId(Long userId);

    // Used to show the "followers" count on a profile page
    long countByFollowingId(Long userId);
}